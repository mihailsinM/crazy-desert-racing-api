package com.crazydesert.racing.service;

import com.crazydesert.racing.ChatBlock;
import com.crazydesert.racing.ChatConversation;
import com.crazydesert.racing.ChatMessage;
import com.crazydesert.racing.ChatReadState;
import com.crazydesert.racing.ChatReport;
import com.crazydesert.racing.User;
import com.crazydesert.racing.UserPhoto;
import com.crazydesert.racing.dto.ChatConversationResponse;
import com.crazydesert.racing.dto.ChatImageResponse;
import com.crazydesert.racing.dto.ChatMessageResponse;
import com.crazydesert.racing.dto.ChatReportRequest;
import com.crazydesert.racing.dto.ChatReportResponse;
import com.crazydesert.racing.dto.ChatReportReviewRequest;
import com.crazydesert.racing.dto.ChatUnreadResponse;
import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.enums.ChatConversationType;
import com.crazydesert.racing.enums.ChatReportStatus;
import com.crazydesert.racing.exception.ChatAccessDeniedException;
import com.crazydesert.racing.exception.ChatNotFoundException;
import com.crazydesert.racing.exception.DuplicateChatReportException;
import com.crazydesert.racing.exception.InvalidChatRequestException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.ChatBlockRepository;
import com.crazydesert.racing.repository.ChatConversationRepository;
import com.crazydesert.racing.repository.ChatMessageRepository;
import com.crazydesert.racing.repository.ChatReadStateRepository;
import com.crazydesert.racing.repository.ChatReportRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH = 2_000;
    private static final int MAX_MESSAGES_PER_MINUTE = 30;

    private final UserRepository userRepository;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatReadStateRepository readStateRepository;
    private final ChatBlockRepository blockRepository;
    private final ChatReportRepository reportRepository;
    private final ImageUploadValidator imageUploadValidator;
    private final ChatCipher chatCipher;

    public ChatService(
            UserRepository userRepository,
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            ChatReadStateRepository readStateRepository,
            ChatBlockRepository blockRepository,
            ChatReportRepository reportRepository,
            ImageUploadValidator imageUploadValidator,
            ChatCipher chatCipher) {

        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.readStateRepository = readStateRepository;
        this.blockRepository = blockRepository;
        this.reportRepository = reportRepository;
        this.imageUploadValidator = imageUploadValidator;
        this.chatCipher = chatCipher;
    }

    public ChatConversationResponse openDirectConversation(
            String currentEmail,
            Long recipientId) {

        User currentUser = requireUser(currentEmail);
        User recipient = requireUser(recipientId);

        if (Objects.equals(currentUser.getId(), recipient.getId())) {
            throw new InvalidChatRequestException(
                    "You cannot start a chat with yourself"
            );
        }

        String conversationKey = directConversationKey(
                currentUser.getId(),
                recipient.getId()
        );
        ChatConversation conversation = conversationRepository
                .findByConversationKey(conversationKey)
                .orElseGet(() -> createDirectConversation(
                        conversationKey,
                        currentUser,
                        recipient
                ));

        return toConversationResponse(conversation, currentUser);
    }

    public ChatConversationResponse openSupportConversation(
            String currentEmail) {

        User currentUser = requireUser(currentEmail);
        ChatConversation conversation = conversationRepository
                .findByTypeAndSupportOwnerId(
                        ChatConversationType.SUPPORT,
                        currentUser.getId()
                )
                .orElseGet(() -> createSupportConversation(currentUser));

        return toConversationResponse(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getConversations(
            String currentEmail) {

        User currentUser = requireUser(currentEmail);
        return accessibleConversations(currentUser).stream()
                .sorted(Comparator.comparing(
                        ChatConversation::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(conversation ->
                        toConversationResponse(conversation, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(
            String currentEmail,
            Long conversationId,
            Long afterId) {

        User currentUser = requireUser(currentEmail);
        ChatConversation conversation = requireConversation(conversationId);
        requireConversationAccess(conversation, currentUser);

        List<ChatMessage> messages;

        if (afterId == null) {
            messages = new ArrayList<>(
                    messageRepository
                            .findTop100ByConversationIdOrderByIdDesc(
                                    conversationId
                            )
            );
            messages.sort(Comparator.comparing(ChatMessage::getId));
        } else {
            if (afterId < 0) {
                throw new InvalidChatRequestException(
                        "afterId must not be negative"
                );
            }

            messages = messageRepository
                    .findTop100ByConversationIdAndIdGreaterThanOrderByIdAsc(
                            conversationId,
                            afterId
                    );
        }

        return messages.stream()
                .map(message -> toMessageResponse(message, currentUser))
                .toList();
    }

    public ChatMessageResponse sendMessage(
            String currentEmail,
            Long conversationId,
            String body,
            MultipartFile image) {

        User currentUser = requireUser(currentEmail);
        ChatConversation conversation = requireConversation(conversationId);
        requireConversationAccess(conversation, currentUser);
        requireMessagingAllowed(conversation, currentUser);
        enforceRateLimit(currentUser);

        String normalizedBody = normalizeBody(body);
        boolean hasImage = image != null && !image.isEmpty();

        if (normalizedBody == null && !hasImage) {
            throw new InvalidChatRequestException(
                    "Write a message or attach an image"
            );
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setBodyCiphertext(chatCipher.encryptText(normalizedBody));
        message.setCreatedAt(LocalDateTime.now());

        if (hasImage) {
            byte[] sanitizedImage = imageUploadValidator.validateAndRead(image);
            message.setImageCiphertext(
                    chatCipher.encryptBytes(sanitizedImage)
            );
            message.setImageContentType(image.getContentType());
            message.setImageOriginalName(
                    sanitizeOriginalFilename(image.getOriginalFilename())
            );
        }

        ChatMessage savedMessage = messageRepository.save(message);
        conversation.setUpdatedAt(savedMessage.getCreatedAt());
        conversationRepository.save(conversation);
        markConversationRead(conversation, currentUser);

        return toMessageResponse(savedMessage, currentUser);
    }

    public void markRead(String currentEmail, Long conversationId) {
        User currentUser = requireUser(currentEmail);
        ChatConversation conversation = requireConversation(conversationId);
        requireConversationAccess(conversation, currentUser);
        markConversationRead(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public ChatUnreadResponse getUnreadCount(String currentEmail) {
        User currentUser = requireUser(currentEmail);
        long unread = accessibleConversations(currentUser).stream()
                .mapToLong(conversation -> unreadCount(
                        conversation,
                        currentUser
                ))
                .sum();

        return new ChatUnreadResponse(unread);
    }

    public void blockUser(String currentEmail, Long blockedUserId) {
        User currentUser = requireUser(currentEmail);
        User blockedUser = requireUser(blockedUserId);

        if (Objects.equals(currentUser.getId(), blockedUser.getId())) {
            throw new InvalidChatRequestException(
                    "You cannot block yourself"
            );
        }

        if (blockRepository.existsByBlockerIdAndBlockedUserId(
                currentUser.getId(),
                blockedUser.getId())) {
            return;
        }

        ChatBlock block = new ChatBlock();
        block.setBlocker(currentUser);
        block.setBlockedUser(blockedUser);
        block.setCreatedAt(LocalDateTime.now());
        blockRepository.save(block);
    }

    public void unblockUser(String currentEmail, Long blockedUserId) {
        User currentUser = requireUser(currentEmail);
        blockRepository.findByBlockerIdAndBlockedUserId(
                        currentUser.getId(),
                        blockedUserId
                )
                .ifPresent(blockRepository::delete);
    }

    public ChatReportResponse reportMessage(
            String currentEmail,
            Long messageId,
            ChatReportRequest request) {

        User reporter = requireUser(currentEmail);
        ChatMessage message = requireMessage(messageId);
        requireConversationAccess(message.getConversation(), reporter);

        if (Objects.equals(message.getSender().getId(), reporter.getId())) {
            throw new InvalidChatRequestException(
                    "You cannot report your own message"
            );
        }

        if (reportRepository.existsByMessageIdAndReporterId(
                messageId,
                reporter.getId())) {
            throw new DuplicateChatReportException(
                    "You have already reported this message"
            );
        }

        ChatReport report = new ChatReport();
        report.setMessage(message);
        report.setReporter(reporter);
        report.setReason(request.reason);
        report.setDetails(trimToNull(request.details));
        report.setStatus(ChatReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());

        return toReportResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ChatReportResponse> getOpenReports(String adminEmail) {
        requireAdmin(adminEmail);
        return reportRepository
                .findByStatusOrderByCreatedAtAsc(ChatReportStatus.OPEN)
                .stream()
                .map(this::toReportResponse)
                .toList();
    }

    public ChatReportResponse reviewReport(
            String adminEmail,
            Long reportId,
            ChatReportReviewRequest request) {

        User admin = requireAdmin(adminEmail);

        if (request.status == null
                || request.status == ChatReportStatus.OPEN) {
            throw new InvalidChatRequestException(
                    "Reviewed reports must be resolved or dismissed"
            );
        }

        ChatReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ChatNotFoundException(
                        "Chat report with id " + reportId + " not found"
                ));
        report.setStatus(request.status);
        report.setReviewer(admin);
        report.setReviewedAt(LocalDateTime.now());

        return toReportResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public ChatImageResponse getMessageImage(
            String currentEmail,
            Long messageId) {

        User currentUser = requireUser(currentEmail);
        ChatMessage message = requireMessage(messageId);

        boolean canAccessConversation = canAccessConversation(
                message.getConversation(),
                currentUser
        );
        boolean canReviewReportedMessage = currentUser.getRole().hasAdminAccess()
                && reportRepository.existsByMessageId(messageId);

        if (!canAccessConversation && !canReviewReportedMessage) {
            throw new ChatAccessDeniedException(
                    "You do not have permission to view this attachment"
            );
        }

        if (!message.hasImage()) {
            throw new ChatNotFoundException(
                    "This message does not contain an image"
            );
        }

        return new ChatImageResponse(
                chatCipher.decryptBytes(message.getImageCiphertext()),
                message.getImageContentType()
        );
    }

    private ChatConversation createDirectConversation(
            String conversationKey,
            User firstUser,
            User secondUser) {

        User userOne = firstUser.getId() < secondUser.getId()
                ? firstUser
                : secondUser;
        User userTwo = Objects.equals(userOne.getId(), firstUser.getId())
                ? secondUser
                : firstUser;
        LocalDateTime now = LocalDateTime.now();

        ChatConversation conversation = new ChatConversation();
        conversation.setType(ChatConversationType.DIRECT);
        conversation.setConversationKey(conversationKey);
        conversation.setUserOne(userOne);
        conversation.setUserTwo(userTwo);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        ChatConversation savedConversation = conversationRepository.save(
                conversation
        );

        createInitialReadState(savedConversation, userOne, now);
        createInitialReadState(savedConversation, userTwo, now);
        return savedConversation;
    }

    private ChatConversation createSupportConversation(User owner) {
        LocalDateTime now = LocalDateTime.now();
        ChatConversation conversation = new ChatConversation();
        conversation.setType(ChatConversationType.SUPPORT);
        conversation.setConversationKey("SUPPORT:" + owner.getId());
        conversation.setSupportOwner(owner);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        ChatConversation savedConversation = conversationRepository.save(
                conversation
        );

        createInitialReadState(savedConversation, owner, now);
        return savedConversation;
    }

    private void createInitialReadState(
            ChatConversation conversation,
            User user,
            LocalDateTime lastReadAt) {

        ChatReadState state = new ChatReadState();
        state.setConversation(conversation);
        state.setUser(user);
        state.setLastReadAt(lastReadAt);
        readStateRepository.save(state);
    }

    private void markConversationRead(
            ChatConversation conversation,
            User user) {

        ChatReadState state = readStateRepository
                .findByConversationIdAndUserId(
                        conversation.getId(),
                        user.getId()
                )
                .orElseGet(() -> {
                    ChatReadState newState = new ChatReadState();
                    newState.setConversation(conversation);
                    newState.setUser(user);
                    return newState;
                });
        state.setLastReadAt(LocalDateTime.now());
        readStateRepository.save(state);
    }

    private List<ChatConversation> accessibleConversations(User user) {
        Map<Long, ChatConversation> conversations = new LinkedHashMap<>();
        conversationRepository.findDirectConversationsForUser(user.getId())
                .forEach(conversation ->
                        conversations.put(conversation.getId(), conversation));

        if (user.getRole().hasAdminAccess()) {
            conversationRepository.findByTypeOrderByUpdatedAtDesc(
                            ChatConversationType.SUPPORT
                    )
                    .forEach(conversation -> conversations.put(
                            conversation.getId(),
                            conversation
                    ));
        } else {
            conversationRepository.findByTypeAndSupportOwnerId(
                            ChatConversationType.SUPPORT,
                            user.getId()
                    )
                    .ifPresent(conversation -> conversations.put(
                            conversation.getId(),
                            conversation
                    ));
        }

        return new ArrayList<>(conversations.values());
    }

    private ChatConversationResponse toConversationResponse(
            ChatConversation conversation,
            User viewer) {

        User otherUser = resolveOtherUser(conversation, viewer);
        String title = resolveConversationTitle(
                conversation,
                viewer,
                otherUser
        );
        ChatMessage lastMessage = messageRepository
                .findFirstByConversationIdOrderByIdDesc(conversation.getId())
                .orElse(null);

        return new ChatConversationResponse(
                conversation.getId(),
                conversation.getType(),
                title,
                otherUser == null ? null : otherUser.getId(),
                buildAvatarUrl(otherUser),
                getAvatarFraming(otherUser),
                buildMessagePreview(lastMessage),
                lastMessage == null
                        ? conversation.getUpdatedAt()
                        : lastMessage.getCreatedAt(),
                unreadCount(conversation, viewer),
                otherUser != null && isBlockedBy(viewer, otherUser),
                otherUser != null && isBlockedBy(otherUser, viewer)
        );
    }

    private ChatMessageResponse toMessageResponse(
            ChatMessage message,
            User viewer) {

        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                Objects.equals(message.getSender().getId(), viewer.getId()),
                chatCipher.decryptText(message.getBodyCiphertext()),
                message.hasImage()
                        ? "/chat/messages/" + message.getId() + "/image"
                        : null,
                message.getImageOriginalName(),
                message.getCreatedAt()
        );
    }

    private ChatReportResponse toReportResponse(ChatReport report) {
        ChatMessage message = report.getMessage();
        return new ChatReportResponse(
                report.getId(),
                message.getId(),
                message.getConversation().getId(),
                report.getReporter().getId(),
                report.getReporter().getName(),
                message.getSender().getId(),
                message.getSender().getName(),
                report.getReason(),
                report.getDetails(),
                chatCipher.decryptText(message.getBodyCiphertext()),
                message.hasImage()
                        ? "/chat/messages/" + message.getId() + "/image"
                        : null,
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }

    private long unreadCount(
            ChatConversation conversation,
            User viewer) {

        return readStateRepository.findByConversationIdAndUserId(
                        conversation.getId(),
                        viewer.getId()
                )
                .map(state -> state.getLastReadAt() == null
                        ? messageRepository
                                .countByConversationIdAndSenderIdNot(
                                        conversation.getId(),
                                        viewer.getId()
                                )
                        : messageRepository
                                .countByConversationIdAndCreatedAtAfterAndSenderIdNot(
                                        conversation.getId(),
                                        state.getLastReadAt(),
                                        viewer.getId()
                                ))
                .orElseGet(() -> messageRepository
                        .countByConversationIdAndSenderIdNot(
                                conversation.getId(),
                                viewer.getId()
                        ));
    }

    private void enforceRateLimit(User sender) {
        long recentMessages = messageRepository
                .countBySenderIdAndCreatedAtAfter(
                        sender.getId(),
                        LocalDateTime.now().minusMinutes(1)
                );

        if (recentMessages >= MAX_MESSAGES_PER_MINUTE) {
            throw new InvalidChatRequestException(
                    "Too many messages. Wait a moment and try again"
            );
        }
    }

    private void requireMessagingAllowed(
            ChatConversation conversation,
            User sender) {

        if (conversation.getType() != ChatConversationType.DIRECT) {
            return;
        }

        User recipient = resolveOtherUser(conversation, sender);

        if (recipient != null && (isBlockedBy(sender, recipient)
                || isBlockedBy(recipient, sender))) {
            throw new ChatAccessDeniedException(
                    "Messages cannot be sent in this chat"
            );
        }
    }

    private User resolveOtherUser(
            ChatConversation conversation,
            User viewer) {

        if (conversation.getType() == ChatConversationType.DIRECT) {
            return Objects.equals(
                    conversation.getUserOne().getId(),
                    viewer.getId()
            ) ? conversation.getUserTwo() : conversation.getUserOne();
        }

        if (Objects.equals(
                conversation.getSupportOwner().getId(),
                viewer.getId()
        )) {
            return null;
        }

        return conversation.getSupportOwner();
    }

    private String resolveConversationTitle(
            ChatConversation conversation,
            User viewer,
            User otherUser) {

        if (conversation.getType() == ChatConversationType.DIRECT) {
            return otherUser == null ? "Direct Chat" : otherUser.getName();
        }

        if (Objects.equals(
                conversation.getSupportOwner().getId(),
                viewer.getId()
        )) {
            return "Admins";
        }

        return conversation.getSupportOwner().getName() + " · Site Problems";
    }

    private String buildMessagePreview(ChatMessage message) {
        if (message == null) {
            return "No messages yet";
        }

        String body = chatCipher.decryptText(message.getBodyCiphertext());
        String preview = body == null ? "Photo" : body.replaceAll("\\s+", " ");

        if (preview.length() <= 80) {
            return preview;
        }

        return preview.substring(0, 77) + "...";
    }

    private String buildAvatarUrl(User user) {
        if (user == null) {
            return null;
        }

        UserPhoto profilePhoto = user.getProfilePhoto();

        if (profilePhoto != null) {
            return "/driver-photos/"
                    + profilePhoto.getId()
                    + "/image?v="
                    + profilePhoto.getImageVersion();
        }

        if (user.getAvatarData() == null) {
            return null;
        }

        return "/avatars/"
                + user.getId()
                + "?v="
                + user.getAvatarVersion();
    }

    private ImageFramingProfilesResponse getAvatarFraming(User user) {
        if (user == null) {
            return null;
        }

        UserPhoto profilePhoto = user.getProfilePhoto();
        return profilePhoto == null
                ? user.getAvatarImageFraming()
                : profilePhoto.getImageFraming();
    }

    private boolean isBlockedBy(User blocker, User blockedUser) {
        return blockRepository.existsByBlockerIdAndBlockedUserId(
                blocker.getId(),
                blockedUser.getId()
        );
    }

    private void requireConversationAccess(
            ChatConversation conversation,
            User viewer) {

        if (!canAccessConversation(conversation, viewer)) {
            throw new ChatAccessDeniedException(
                    "You do not have permission to open this chat"
            );
        }
    }

    private boolean canAccessConversation(
            ChatConversation conversation,
            User viewer) {

        if (conversation.getType() == ChatConversationType.DIRECT) {
            return Objects.equals(
                    conversation.getUserOne().getId(),
                    viewer.getId()
            ) || Objects.equals(
                    conversation.getUserTwo().getId(),
                    viewer.getId()
            );
        }

        return Objects.equals(
                conversation.getSupportOwner().getId(),
                viewer.getId()
        ) || viewer.getRole().hasAdminAccess();
    }

    private ChatConversation requireConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ChatNotFoundException(
                        "Chat with id " + conversationId + " not found"
                ));
    }

    private ChatMessage requireMessage(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ChatNotFoundException(
                        "Chat message with id " + messageId + " not found"
                ));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with email " + email + " not found"
                ));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with id " + userId + " not found"
                ));
    }

    private User requireAdmin(String email) {
        User user = requireUser(email);

        if (!user.getRole().hasAdminAccess()) {
            throw new ChatAccessDeniedException(
                    "Administrator access is required"
            );
        }

        return user;
    }

    private String normalizeBody(String body) {
        String normalized = trimToNull(body);

        if (normalized != null && normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidChatRequestException(
                    "Messages must be at most 2000 characters"
            );
        }

        return normalized;
    }

    private String sanitizeOriginalFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "chat-image";
        }

        String normalized = filename
                .replace('\\', '/')
                .replaceAll("[\\r\\n]", "");
        int finalSlash = normalized.lastIndexOf('/');
        String basename = finalSlash >= 0
                ? normalized.substring(finalSlash + 1)
                : normalized;

        if (basename.length() <= 180) {
            return basename;
        }

        return basename.substring(basename.length() - 180);
    }

    private String directConversationKey(Long firstId, Long secondId) {
        long lowerId = Math.min(firstId, secondId);
        long higherId = Math.max(firstId, secondId);
        return "DIRECT:" + lowerId + ":" + higherId;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
