package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.MessageDTOIn;
import com.example.capstone3.DTO.Out.MessageDTOOut;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Conversation;
import com.example.capstone3.Models.Message;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.ConversationRepository;
import com.example.capstone3.Repository.MessageRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ApartmentRepository apartmentRepository;

    public List<MessageDTOOut> getAll() {
        List<MessageDTOOut> messageDTOOuts = new ArrayList<>();
        for (Message message : messageRepository.findAll()) {
            messageDTOOuts.add(convertToDTO(message));
        }
        return messageDTOOuts;
    }

    public MessageDTOOut getMessage(Integer id) {
        Message message = messageRepository.findMessageById(id);
        if (message == null) {
            throw new ApiException("Message not found");
        }
        return convertToDTO(message);
    }

    public List<MessageDTOOut> getMessagesByConversationId(Integer conversationId) {
        Conversation conversation = conversationRepository.findConversationById(conversationId);
        if (conversation == null) {
            throw new ApiException("Conversation not found");
        }

        List<MessageDTOOut> messages = new ArrayList<>();
        for (Message message :
                messageRepository.findMessagesByConversation_IdOrderBySentAtAsc(conversationId)) {
            messages.add(convertToDTO(message));
        }
        return messages;
    }

    // User sends a message. senderId is set to userId.
    @Transactional
    public void addUserMessage(Integer userId, Integer ownerId, Integer apartmentId,
                               MessageDTOIn messageDTOIn) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) throw new ApiException("Owner not found");

        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) throw new ApiException("Apartment not found");

        if (!apartment.getOwner().getId().equals(ownerId)) {
            throw new ApiException("Owner does not own this apartment");
        }

        Conversation conversation = resolveConversation(user, owner, apartment);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(userId);
        message.setSenderRole("USER");
        message.setContent(messageDTOIn.getContent());
        messageRepository.save(message);
    }

    // Owner sends a message. senderId is set to ownerId.
    @Transactional
    public void addOwnerMessage(Integer ownerId, Integer userId, Integer apartmentId,
                                MessageDTOIn messageDTOIn) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) throw new ApiException("Owner not found");

        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) throw new ApiException("Apartment not found");

        if (!apartment.getOwner().getId().equals(ownerId)) {
            throw new ApiException("Owner does not own this apartment");
        }

        Conversation conversation = resolveConversation(user, owner, apartment);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(ownerId);
        message.setSenderRole("OWNER");
        message.setContent(messageDTOIn.getContent());
        messageRepository.save(message);
    }

    public void updateMessage(Integer id, Integer senderId, String senderRole, MessageDTOIn messageDTOIn) {
        Message message = messageRepository.findMessageById(id);
        if (message == null) {
            throw new ApiException("Message not found");
        }
        String normalizedRole = senderRole == null
                ? "" : senderRole.trim().toUpperCase();
        if (!"USER".equals(normalizedRole) && !"OWNER".equals(normalizedRole)) {
            throw new ApiException("Sender role must be USER or OWNER");
        }
        if ("USER".equals(normalizedRole)
                && userRepository.findUserById(senderId) == null) {
            throw new ApiException("User not found");
        }
        if ("OWNER".equals(normalizedRole)
                && ownerRepository.findOwnerById(senderId) == null) {
            throw new ApiException("Owner not found");
        }
        if (!senderId.equals(message.getSenderId())
                || !normalizedRole.equalsIgnoreCase(message.getSenderRole())) {
            throw new ApiException(
                    "Only the original sender can modify this message");
        }
        message.setContent(messageDTOIn.getContent());
        messageRepository.save(message);
    }

    public void deleteMessage(Integer id, Integer senderId, String senderRole) {
        Message message = messageRepository.findMessageById(id);
        if (message == null) {
            throw new ApiException("Message not found");
        }
        String normalizedRole = senderRole == null
                ? "" : senderRole.trim().toUpperCase();
        if (!"USER".equals(normalizedRole) && !"OWNER".equals(normalizedRole)) {
            throw new ApiException("Sender role must be USER or OWNER");
        }
        if ("USER".equals(normalizedRole)
                && userRepository.findUserById(senderId) == null) {
            throw new ApiException("User not found");
        }
        if ("OWNER".equals(normalizedRole)
                && ownerRepository.findOwnerById(senderId) == null) {
            throw new ApiException("Owner not found");
        }
        if (!senderId.equals(message.getSenderId())
                || !normalizedRole.equalsIgnoreCase(message.getSenderRole())) {
            throw new ApiException(
                    "Only the original sender can modify this message");
        }
        messageRepository.deleteById(id);
    }

    public MessageDTOOut convertToDTO(Message message) {
        MessageDTOOut messageDTOOut = new MessageDTOOut();
        messageDTOOut.setId(message.getId());
        messageDTOOut.setConversationId(message.getConversation().getId());
        messageDTOOut.setSenderId(message.getSenderId());
        messageDTOOut.setSenderRole(message.getSenderRole());
        messageDTOOut.setContent(message.getContent());
        messageDTOOut.setSentAt(message.getSentAt());
        return messageDTOOut;
    }

    // Reuse existing conversation or create a new one for this user/owner/apartment triple.
    private Conversation resolveConversation(User user, Owner owner, Apartment apartment) {
        Conversation existing = conversationRepository.findByUser_IdAndOwner_IdAndApartment_Id(
                user.getId(), owner.getId(), apartment.getId());
        if (existing != null) {
            return existing;
        }
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setOwner(owner);
        conversation.setApartment(apartment);
        return conversationRepository.save(conversation);
    }

    //^^^^^^^CRUD^^^^^^^^

}
