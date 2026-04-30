package com.clinicalsystem.chat.repository;

import com.clinicalsystem.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByRoomIdAndDeletedFalseOrderBySentAtDesc(
            String roomId, Pageable pageable);

    long countByRoomIdAndReadFalseAndSenderIdNot(String roomId, String senderId);
}
