package com.example.capstone3.Repository;

import com.example.capstone3.Models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    Conversation findConversationById(Integer id);

    Conversation findByUser_IdAndOwner_IdAndApartment_Id(Integer userId, Integer ownerId, Integer apartmentId);

    List<Conversation> findConversationsByUser_IdOrderByCreatedAtDesc(Integer userId);

    List<Conversation> findConversationsByOwner_IdOrderByCreatedAtDesc(Integer ownerId);
}
