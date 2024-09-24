package com.turtlecoin.auctionservice.domain.auction.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.turtlecoin.auctionservice.domain.auction.dto.AuctionResponseDTO;
import com.turtlecoin.auctionservice.domain.auction.dto.RegisterAuctionDTO;
import com.turtlecoin.auctionservice.domain.auction.entity.Auction;
import com.turtlecoin.auctionservice.domain.auction.entity.AuctionPhoto;
import com.turtlecoin.auctionservice.domain.auction.entity.AuctionProgress;
import com.turtlecoin.auctionservice.domain.auction.entity.QAuction;
import com.turtlecoin.auctionservice.domain.auction.repository.AuctionRepository;
import com.turtlecoin.auctionservice.domain.s3.service.ImageUploadService;
import com.turtlecoin.auctionservice.feign.dto.TurtleResponseDTO;
import com.turtlecoin.auctionservice.domain.turtle.entity.Gender;
import com.turtlecoin.auctionservice.feign.MainClient;
import com.turtlecoin.auctionservice.global.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String AUCTION_BID_KEY = "auction_bid_";

    private final AuctionRepository auctionRepository;
    private final ImageUploadService imageUploadService;  // ImageUploadService도 주입합니다.
    private final MainClient mainClient;
    private final JPAQueryFactory queryFactory;

    // 경매 등록
    @Transactional
    public Auction registerAuction(RegisterAuctionDTO registerAuctionDTO, List<MultipartFile> images) throws IOException {
        log.info("경매 등록 시작 - 사용자 ID: {}, 거북이 ID: {}", registerAuctionDTO.getUserId(), registerAuctionDTO.getTurtleId());

        validateUserOwnsTurtle(registerAuctionDTO.getUserId(), registerAuctionDTO.getTurtleId());
        validateTurtleNotAlreadyRegistered(registerAuctionDTO.getTurtleId());

        Auction auction = auctionRepository.save(registerAuctionDTO.toEntity());

        if (images != null && !images.isEmpty()) {
            auction.getAuctionPhotos().addAll(uploadImages(images, auction));
        }

        return auctionRepository.save(auction);
    }

    // 이미지 업로드 처리 메서드
    private List<AuctionPhoto> uploadImages(List<MultipartFile> images, Auction auction) throws IOException {
        List<AuctionPhoto> photos = new ArrayList<>();
        for (MultipartFile image : images) {
            String imagePath = imageUploadService.upload(image, "auctionImages");
            photos.add(AuctionPhoto.builder().imageAddress(imagePath).auction(auction).build());
        }
        return photos;
    }

    // 사용자가 소유한 거북이인지 검증 메서드
    private void validateUserOwnsTurtle(Long userId, Long turtleId) {
        List<TurtleResponseDTO> userTurtles = mainClient.getTurtlesByUserId(userId);
        if (userTurtles.isEmpty()) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        boolean isUserTurtle = userTurtles.stream().anyMatch(turtle -> turtle.getId().equals(turtleId));
        if (!isUserTurtle) {
            throw new TurtleNotFoundException("해당 거북이는 사용자가 소유한 거북이가 아닙니다.");
        }
    }

    private void validateTurtleNotAlreadyRegistered(Long turtleId) {
        if (auctionRepository.existsByTurtleId(turtleId)) {
            throw new TurtleAlreadyRegisteredException("이미 등록된 거북이는 등록할 수 없습니다.");
        }
    }

    // 거북이 정보를 가져와서 RegisterAuctionDTO에 설정하는 메서드
    private RegisterAuctionDTO updateAuctionWithTurtleInfo(RegisterAuctionDTO registerAuctionDTO) {
        TurtleResponseDTO turtleInfo = mainClient.getTurtle(registerAuctionDTO.getTurtleId());

        return RegisterAuctionDTO.builder()
                .turtleId(registerAuctionDTO.getTurtleId())
                .userId(registerAuctionDTO.getUserId())
                .startTime(registerAuctionDTO.getStartTime())
                .minBid(registerAuctionDTO.getMinBid())
                .content(registerAuctionDTO.getContent())
                .title(registerAuctionDTO.getTitle())
                .weight(turtleInfo.getWeight())  // 거북이 무게 설정
                .gender(turtleInfo.getGender())  // 거북이 성별 설정
                .build();
    }

    // 경매 저장 처리 메서드
    private Auction saveAuction(RegisterAuctionDTO registerAuctionDTO) {
        Auction auction = registerAuctionDTO.toEntity();
        log.info("auction: {}", auction);
        log.info("빌더를 이용해서 저장 성공");
        return auctionRepository.save(auction);
    }

    // 업로드된 이미지 삭제 메서드
    public void deleteUploadedImages(List<AuctionPhoto> auctionPhotos) {
        for (AuctionPhoto photo : auctionPhotos) {
            imageUploadService.deleteS3(photo.getImageAddress());
        }
    }

    // 경매 ID로 경매 조회
    public AuctionResponseDTO getAuctionById(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("경매를 찾을 수 없습니다: " + auctionId));

        log.info("경매 ID로 경매 조회");

        TurtleResponseDTO turtle = mainClient.getTurtle(auctionId);

        // 경매 정보를 빌더 패턴을 사용해 DTO로 변환
        return AuctionResponseDTO.from(auction, turtle);
    }

    // 경매 필터링 후 조회
    public List<Auction> getFilteredAuctions(Gender gender, Double minSize, Double maxSize, Double minPrice, Double maxPrice, AuctionProgress progress, int page) {
        QAuction auction = QAuction.auction;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 가격 필터 (minPrice ~ maxPrice)
        if (minPrice != null) {
            if (maxPrice != null) {
                whereClause.and(auction.minBid.between(minPrice, maxPrice));
            } else {
                whereClause.and(auction.minBid.goe(minPrice));
            }
        } else if (maxPrice != null) {
            whereClause.and(auction.minBid.loe(maxPrice));
        }

        // 경매 진행 상태 필터
        if (progress != null) {
            whereClause.and(auction.auctionProgress.eq(progress));
        }

        // main-service에서 필터링 엔드포인트 열어둘 것
        List<TurtleResponseDTO> filteredTurtles = mainClient.getFilteredTurtles(gender, minSize, maxSize);

        return queryFactory.selectFrom(auction)
                .where(whereClause.and(auction.turtleId.in(
                        filteredTurtles.stream().map(TurtleResponseDTO::getId).toList())))
                .offset((page-1L) * 10)
                .limit(10)
                .fetch();
    }

//    // 거북이 정보를 받아와서 경매정보를 DTO로 변환
//    // 수정, 테스트 필요
    public AuctionResponseDTO convertToDTO(Auction auction) {
        log.info("Turtle ID: {}", auction.getTurtleId());
        TurtleResponseDTO turtleInfo = mainClient.getTurtle(auction.getTurtleId());

        if (turtleInfo == null) {
            throw new TurtleNotFoundException("Main-service에서 거북이를 가져올 수 없습니다.");
        }

        log.info("Turtle info retrieved: {}", turtleInfo);
        return AuctionResponseDTO.from(auction, turtleInfo);
    }

    // 입찰 가격 갱신
    @Transactional
    public void processBid(Long auctionId, Long userId, Double bidAmount) {
        String redisKey = AUCTION_BID_KEY + auctionId;

        Map<String, Object> bidData = new HashMap<>();
        bidData.put("userId", userId);
        bidData.put("bidAmount", bidAmount);

        Map<Object, Object> currentBidData = getCurrentBid(auctionId);
        Double currentBid = (Double) currentBidData.get("nowBid");
        Long currentUserId = (Long) currentBidData.get("userId");

        if (currentBid == null) {
            currentBid = getMinBid(auctionId);
        }

        if ((currentBid == null || bidAmount > currentBid) &&
                (currentUserId == null || !currentUserId.equals(userId))) {

            if (currentBid == null) {
                currentBid = getMinBid(auctionId);
            }

            Long bidIncrement = calculateBidIncrement(currentBid);
            Double newBidAmount = currentBid + bidIncrement;

            bidData.put("bidAmount", newBidAmount);
            redisTemplate.opsForHash().putAll(redisKey, bidData);

            String bidHistory = redisKey + ":history";
            String bidRecord = "userId: " + userId + ", bidAmount: " + newBidAmount;
            redisTemplate.opsForList().rightPush(bidHistory, bidRecord);

            log.info("입찰 갱신 : auctionID = {}, userId = {}, newBidAmount = {}", auctionId, userId, newBidAmount);
        } else {
            if (currentUserId != null && currentUserId.equals(userId)) {
                throw new SameUserBidException("자신의 입찰에 재입찰할 수 없습니다");
            }
            throw new WrongBidAmountException("현재 입찰가보다 낮은 금액으로 입찰할 수 없습니다");
        }
    }

    public Map<Object, Object> getCurrentBid (Long auctionId) {
        String redisKey = AUCTION_BID_KEY + auctionId;
        return redisTemplate.opsForHash().entries(redisKey);
    }

    public Double getMinBid(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("경매를 찾을 수 없습니다."));
        return auction.getMinBid();
    }

    private Long calculateBidIncrement(Double currentBid) {
        // 경매 가격에 따라 구분 필요
        if (currentBid >= 0 && currentBid <= 10000) {
            return 500L; // 0 ~ 10000 : 500
        } else if (currentBid >= 10001 && currentBid <= 100000) {
            return 2000L; // 10001 ~ 100000 : 2000
        } else if (currentBid >= 100001 && currentBid <= 200000) {
            return 5000L; // 100001 ~ 200000 : 5000
        } else {
            return 10000L; // 그 외 : 10000 (기본 값)
        }
    }


}