package com.crazydesert.racing.service;

import com.crazydesert.racing.ChatConversation;
import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.ChatConversationType;
import com.crazydesert.racing.enums.ChatSupportTopic;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.ChatAccessDeniedException;
import com.crazydesert.racing.repository.ChatBlockRepository;
import com.crazydesert.racing.repository.ChatConversationRepository;
import com.crazydesert.racing.repository.ChatMessageRepository;
import com.crazydesert.racing.repository.ChatReadStateRepository;
import com.crazydesert.racing.repository.ChatReportRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServicePrivacyTest {

    private static final String TEST_KEY =
            "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatConversationRepository conversationRepository;
    @Mock
    private ChatMessageRepository messageRepository;
    @Mock
    private ChatReadStateRepository readStateRepository;
    @Mock
    private ChatBlockRepository blockRepository;
    @Mock
    private ChatReportRepository reportRepository;
    @Mock
    private ImageUploadValidator imageUploadValidator;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                userRepository,
                conversationRepository,
                messageRepository,
                readStateRepository,
                blockRepository,
                reportRepository,
                imageUploadValidator,
                new ChatCipher(TEST_KEY)
        );
    }

    @Test
    void administratorCannotOpenUnreportedDirectConversation() {
        User first = user(1L, "first@example.com", Role.USER);
        User second = user(2L, "second@example.com", Role.USER);
        User admin = user(3L, "admin@example.com", Role.ADMIN);
        ChatConversation conversation = directConversation(10L, first, second);

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));
        when(conversationRepository.findById(10L))
                .thenReturn(Optional.of(conversation));

        assertThrows(
                ChatAccessDeniedException.class,
                () -> chatService.getMessages(
                        "admin@example.com",
                        10L,
                        null
                )
        );
        verify(messageRepository, never())
                .findTop100ByConversationIdOrderByIdDesc(10L);
    }

    @Test
    void everyAdministratorCanOpenSupportConversation() {
        User owner = user(1L, "owner@example.com", Role.USER);
        User admin = user(3L, "admin@example.com", Role.ADMIN);
        ChatConversation conversation = supportConversation(20L, owner);

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));
        when(conversationRepository.findById(20L))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findTop100ByConversationIdOrderByIdDesc(20L))
                .thenReturn(List.of());

        assertEquals(
                List.of(),
                chatService.getMessages("admin@example.com", 20L, null)
        );
    }

    @Test
    void supportOwnerCanSetTopicVisibleToAdministrators() {
        User owner = user(1L, "owner@example.com", Role.USER);
        ChatConversation conversation = supportConversation(20L, owner);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(conversationRepository.findByTypeAndSupportOwnerId(ChatConversationType.SUPPORT, 1L))
                .thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        assertEquals(ChatSupportTopic.MARKETPLACE,
                chatService.updateSupportTopic("owner@example.com", ChatSupportTopic.MARKETPLACE).supportTopic());
        assertEquals(ChatSupportTopic.MARKETPLACE, conversation.getSupportTopic());
    }

    @Test
    void blockInEitherDirectionStopsNewMessages() {
        User sender = user(1L, "sender@example.com", Role.USER);
        User recipient = user(2L, "recipient@example.com", Role.USER);
        ChatConversation conversation = directConversation(
                10L,
                sender,
                recipient
        );

        when(userRepository.findByEmail("sender@example.com"))
                .thenReturn(Optional.of(sender));
        when(conversationRepository.findById(10L))
                .thenReturn(Optional.of(conversation));
        when(blockRepository.existsByBlockerIdAndBlockedUserId(1L, 2L))
                .thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedUserId(2L, 1L))
                .thenReturn(true);

        assertThrows(
                ChatAccessDeniedException.class,
                () -> chatService.sendMessage(
                        "sender@example.com",
                        10L,
                        "Hello",
                        null
                )
        );
        verify(messageRepository, never())
                .countBySenderIdAndCreatedAtAfter(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    private User user(Long id, String email, Role role) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("User " + id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private ChatConversation directConversation(
            Long id,
            User first,
            User second) {

        ChatConversation conversation = new ChatConversation();
        ReflectionTestUtils.setField(conversation, "id", id);
        conversation.setType(ChatConversationType.DIRECT);
        conversation.setUserOne(first);
        conversation.setUserTwo(second);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversation;
    }

    private ChatConversation supportConversation(Long id, User owner) {
        ChatConversation conversation = new ChatConversation();
        ReflectionTestUtils.setField(conversation, "id", id);
        conversation.setType(ChatConversationType.SUPPORT);
        conversation.setSupportOwner(owner);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversation;
    }
}
