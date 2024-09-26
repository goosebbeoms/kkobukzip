package com.turtlecoin.mainservice.domain.chat.entity;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Document
@AllArgsConstructor
@Getter
@Builder
public class ChatTextMessage implements ChatMessage {
	@Id
	private ObjectId id;
	private Long sender;
	private LocalDateTime registerTime;
	private String text;
}