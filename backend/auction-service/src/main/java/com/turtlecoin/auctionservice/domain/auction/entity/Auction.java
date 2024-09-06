package com.turtlecoin.auctionservice.domain.auction.entity;

import com.turtlecoin.auctionservice.domain.auctionphoto.entity.AuctionPhoto;
import com.turtlecoin.auctionservice.domain.auctiontag.entity.AuctionTag;
import com.turtlecoin.auctionservice.domain.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {
    @Id @GeneratedValue
    @Column(unique=true, nullable=false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Double minBid;

    private Double winningBid;

    // 현재 입찰 회원
    private Long buyerId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private AuctionProgress auctionProgress;

    private LocalDateTime endTime;
    // 거북이
    @Column(nullable = false)
    private Long turtleId;

    @OneToMany(mappedBy = "auction")
    private List<AuctionPhoto> auctionPhotos = new ArrayList<>();

    @OneToMany(mappedBy = "auction")
    private List<AuctionTag> tags = new ArrayList<>();
}