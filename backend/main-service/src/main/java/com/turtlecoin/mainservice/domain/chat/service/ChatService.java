package com.turtlecoin.mainservice.domain.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.turtlecoin.mainservice.domain.chat.entity.Chat;
import com.turtlecoin.mainservice.domain.chat.entity.ChatMessage;
import com.turtlecoin.mainservice.domain.chat.entity.ChatTextMessage;
import com.turtlecoin.mainservice.domain.chat.entity.ChatTurtleMessage;
import com.turtlecoin.mainservice.domain.chat.repository.ChatRepository;
import com.turtlecoin.mainservice.domain.transaction.entity.Transaction;
import com.turtlecoin.mainservice.domain.transaction.service.TransactionService;
import com.turtlecoin.mainservice.domain.user.dto.UserResponseDTO;
import com.turtlecoin.mainservice.domain.user.service.UserService;
import com.turtlecoin.mainservice.global.exception.TransactionNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatRepository chatRepository;
	private final UserService userService;
	private final TransactionService transactionService;

	public ObjectId createChat(Long smallUserId, Long bigUserId) throws Exception{
		// 호출 해보면서 없는 아이디인지 확인
		UserResponseDTO smallUser = userService.getByUserId(smallUserId);
		UserResponseDTO bigUser = userService.getByUserId(bigUserId);

		Chat chat = Chat.builder()
			.smallUser(smallUserId)
			.bigUser(bigUserId)
			.recentTime(null)
			.messages(new ArrayList<>())
			.build();

		chatRepository.save(chat);

		return chat.getId();
	}

	public boolean isChatExists(Long smallUserId, Long bigUserId) {
		Optional<Chat> chatOptional = chatRepository.findBySmallUserAndBigUser(smallUserId, bigUserId);
		return chatOptional.isPresent();
	}

	public void addChatTextMessage(Long smallUserId, Long bigUserId, Long sender, String message) throws Exception{
		ChatTextMessage chatTextMessage = ChatTextMessage.builder()
			.id(new ObjectId())
			.sender(sender)
			.text(message)
			.registerTime(LocalDateTime.now())
			.build();
		chatRepository.insertBySmallUserAndBigUser(smallUserId, bigUserId, chatTextMessage);
	}

	public void addChatTurtleMessage(Long smallUserId, Long bigUserId, String title, Double price, String image) throws Exception{
		ChatTurtleMessage chatTurtleMessage = ChatTurtleMessage.builder()
			.id(new ObjectId())
			.title(title)
			.price(price)
			.image(image)
			.build();
		chatRepository.insertBySmallUserAndBigUser(smallUserId, bigUserId, chatTurtleMessage);
	}

	// 채팅 리스트에서 넘어와서 채팅 목록을 조회하는 경우
	public List<ChatMessage> getChatListFromUser(Long smallUserId, Long bigUserId, Pageable pageable) throws Exception {
		return chatRepository.getChatByPage(smallUserId, bigUserId, pageable.getPageNumber(), pageable.getPageSize());
	}

	public List<ChatMessage> getChatListFromTransaction(Long userId, Long transactionId, Pageable pageable) throws Exception {
		Transaction transaction = transactionService.findTransactionById(transactionId);
		if(transaction == null){
			throw new TransactionNotFoundException("거래를 찾을 수 없습니다.");
		}

		Long opponentId = transaction.getTurtle().getUser().getId();
		// 더 작은 쪽이 왼쪽 매개변수로 들어가게 해야 함
		Long left = Math.min(opponentId, userId);
		Long right = Math.max(opponentId, userId);

		Chat chat = chatRepository.getChat(left, right);
		if(chat == null){
			createChat(left, right);
		}
		addChatTurtleMessage(left, right, transaction.getTitle(), transaction.getPrice(), transaction.getTransactionPhotos().get(0).getImageAddress());

		return chatRepository.getChatByPage(left, right, pageable.getPageNumber(), pageable.getPageSize());
	}

}
