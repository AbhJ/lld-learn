/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Message.java — Represents a chat message with sender, content, and timestamp
import java.time.LocalDateTime;

public abstract class Message {
    private String id;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;          // indexed by TreeMap in MessageStore for range queries
    private MessageType type;

    public enum MessageType {
        TEXT, IMAGE, FILE
    }

    public Message(String id, String senderId, String content, MessageType type) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public MessageType getType() { return type; }

    public abstract String getDisplayText();

    public static Message createTextMessage(String id, String senderId, String text) {
        return new TextMessage(id, senderId, text);
    }

    public static Message createImageMessage(String id, String senderId, String url) {
        return new ImageMessage(id, senderId, url);
    }

    public static Message createFileMessage(String id, String senderId, String fileName) {
        return new FileMessage(id, senderId, fileName);
    }
}

class TextMessage extends Message {
    public TextMessage(String id, String senderId, String text) {
        super(id, senderId, text, MessageType.TEXT);
    }

    @Override
    public String getDisplayText() {
        return getContent();
    }
}

class ImageMessage extends Message {
    public ImageMessage(String id, String senderId, String imageUrl) {
        super(id, senderId, imageUrl, MessageType.IMAGE);
    }

    @Override
    public String getDisplayText() {
        return "[Image: " + getContent() + "]";
    }
}

class FileMessage extends Message {
    public FileMessage(String id, String senderId, String fileName) {
        super(id, senderId, fileName, MessageType.FILE);
    }

    @Override
    public String getDisplayText() {
        return "[File: " + getContent() + "]";
    }
}
