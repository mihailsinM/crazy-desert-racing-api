package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatConversation;
import com.crazydesert.racing.ChatMessage;
import com.crazydesert.racing.ChatReadState;
import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.ChatConversationType;
import com.crazydesert.racing.enums.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "crazy.super-admin.enabled=false",
        "crazy.demo-seed.enabled=false",
        "crazy.jwt.secret=test-only-jwt-secret-with-at-least-32-bytes",
        "crazy.chat.encryption-key=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8="
})
@Transactional
class ChatUnreadCountRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Test
    void countsOnlyAccessibleUnreadMessagesForMemberAndAdministrator() {
        User member = user(Role.USER);
        User friend = user(Role.USER);
        User outsider = user(Role.USER);
        User admin = user(Role.ADMIN);

        long adminBaseline = messageRepository.countUnreadAccessible(admin.getId(), true);
        LocalDateTime readAt = LocalDateTime.now().minusMinutes(2);
        ChatConversation direct = directConversation(member, friend, readAt);
        ChatConversation inaccessible = directConversation(friend, outsider, readAt);
        ChatConversation support = supportConversation(member, readAt);

        readState(direct, member, readAt);
        readState(support, member, readAt);
        readState(support, admin, readAt);
        message(direct, friend, readAt.minusSeconds(1));
        message(direct, friend, readAt.plusSeconds(1));
        message(direct, member, readAt.plusSeconds(2));
        message(inaccessible, friend, readAt.plusSeconds(3));
        message(support, member, readAt.plusSeconds(4));
        message(support, admin, readAt.plusSeconds(5));
        entityManager.flush();
        entityManager.clear();

        assertEquals(2L, messageRepository.countUnreadAccessible(member.getId(), false));
        assertEquals(adminBaseline + 1L,
                messageRepository.countUnreadAccessible(admin.getId(), true));
    }

    private User user(Role role) {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.test");
        user.setName("Chat test user");
        user.setRole(role);
        entityManager.persist(user);
        return user;
    }

    private ChatConversation directConversation(
            User first, User second, LocalDateTime createdAt) {
        ChatConversation conversation = conversation(ChatConversationType.DIRECT, createdAt);
        conversation.setUserOne(first);
        conversation.setUserTwo(second);
        entityManager.persist(conversation);
        return conversation;
    }

    private ChatConversation supportConversation(User owner, LocalDateTime createdAt) {
        ChatConversation conversation = conversation(ChatConversationType.SUPPORT, createdAt);
        conversation.setSupportOwner(owner);
        entityManager.persist(conversation);
        return conversation;
    }

    private ChatConversation conversation(
            ChatConversationType type, LocalDateTime createdAt) {
        ChatConversation conversation = new ChatConversation();
        conversation.setType(type);
        conversation.setConversationKey(UUID.randomUUID().toString());
        conversation.setCreatedAt(createdAt);
        conversation.setUpdatedAt(createdAt);
        return conversation;
    }

    private void readState(
            ChatConversation conversation, User user, LocalDateTime lastReadAt) {
        ChatReadState state = new ChatReadState();
        state.setConversation(conversation);
        state.setUser(user);
        state.setLastReadAt(lastReadAt);
        entityManager.persist(state);
    }

    private void message(
            ChatConversation conversation, User sender, LocalDateTime createdAt) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setBodyCiphertext("test ciphertext");
        message.setCreatedAt(createdAt);
        entityManager.persist(message);
    }
}
