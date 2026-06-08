package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.ConversationDTOIn;
import com.example.capstone3.DTO.Out.ConversationDTOOut;
import com.example.capstone3.DTO.Out.MessageDTOOut;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Conversation;
import com.example.capstone3.Models.Message;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.ConversationRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ApartmentRepository apartmentRepository;

    public List<ConversationDTOOut> getAll() {
        List<ConversationDTOOut> conversationDTOOuts = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findAll()) {
            conversationDTOOuts.add(convertToDTO(conversation));
        }
        return conversationDTOOuts;
    }

    public ConversationDTOOut getConversation(Integer id) {
        Conversation conversation = conversationRepository.findConversationById(id);
        if (conversation == null) {
            throw new ApiException("Conversation not found");
        }
        return convertToDTO(conversation);
    }

    public List<ConversationDTOOut> getConversationsByUserId(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        List<ConversationDTOOut> conversations = new ArrayList<>();
        for (Conversation conversation :
                conversationRepository.findConversationsByUser_IdOrderByCreatedAtDesc(userId)) {
            conversations.add(convertToDTO(conversation));
        }
        return conversations;
    }

    public List<ConversationDTOOut> getConversationsByOwnerId(Integer ownerId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }

        List<ConversationDTOOut> conversations = new ArrayList<>();
        for (Conversation conversation :
                conversationRepository.findConversationsByOwner_IdOrderByCreatedAtDesc(ownerId)) {
            conversations.add(convertToDTO(conversation));
        }
        return conversations;
    }

    public void addConversation(ConversationDTOIn conversationDTOIn) {
        throw new ApiException("Conversation is created or reused when the first message is sent");
    }

    public void updateConversation(Integer id, ConversationDTOIn conversationDTOIn) {
        Conversation conversation = conversationRepository.findConversationById(id);
        if (conversation == null) {
            throw new ApiException("Conversation not found");
        }
        User user = userRepository.findUserById(conversationDTOIn.getUserId());
        if (user == null) {
            throw new ApiException("User not found");
        }
        Owner owner = ownerRepository.findOwnerById(conversationDTOIn.getOwnerId());
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        Apartment apartment = apartmentRepository.findApartmentById(conversationDTOIn.getApartmentId());
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        if (!apartment.getOwner().getId().equals(owner.getId())) {
            throw new ApiException("Owner does not own this apartment");
        }
        Conversation duplicate = conversationRepository.findByUser_IdAndOwner_IdAndApartment_Id(
                user.getId(), owner.getId(), apartment.getId());
        if (duplicate != null && !duplicate.getId().equals(id)) {
            throw new ApiException("Conversation already exists for this user, owner, and apartment");
        }
        conversation.setUser(user);
        conversation.setOwner(owner);
        conversation.setApartment(apartment);
        conversationRepository.save(conversation);
    }

    public void deleteConversation(Integer id) {
        Conversation conversation = conversationRepository.findConversationById(id);
        if (conversation == null) {
            throw new ApiException("Conversation not found");
        }
        conversationRepository.deleteById(id);
    }

    public ConversationDTOOut convertToDTO(Conversation conversation) {
        ConversationDTOOut conversationDTOOut = new ConversationDTOOut();
        conversationDTOOut.setId(conversation.getId());
        conversationDTOOut.setUserId(conversation.getUser().getId());
        conversationDTOOut.setOwnerId(conversation.getOwner().getId());
        conversationDTOOut.setApartmentId(conversation.getApartment().getId());
        conversationDTOOut.setCreatedAt(conversation.getCreatedAt());

        List<MessageDTOOut> messageDTOOuts = new ArrayList<>();
        for (Message message : conversation.getMessages()) {
            MessageDTOOut messageDTOOut = new MessageDTOOut();
            messageDTOOut.setId(message.getId());
            messageDTOOut.setConversationId(conversation.getId());
            messageDTOOut.setSenderId(message.getSenderId());
            messageDTOOut.setSenderRole(message.getSenderRole());
            messageDTOOut.setContent(message.getContent());
            messageDTOOut.setSentAt(message.getSentAt());
            messageDTOOuts.add(messageDTOOut);
        }
        conversationDTOOut.setMessages(messageDTOOuts);
        return conversationDTOOut;
    }


    //^^^^^^^CRUD^^^^^^^^


}
