/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Message.java — Represents a chat message with sender, content, and timestamp
import java.time.LocalDateTime;

public abstract class Message {              // abstract = can't create Message directly; must use subclass
    private String id;                        // private = only Message class can access this field
    private String senderId;                  // private = hides internal data from outside classes
    private String content;                   // private = forces access through getter methods
    private LocalDateTime timestamp;          // private = internal state hidden from external code
    private MessageType type;                 // private = controlled via constructor only

    public enum MessageType {                 // enum = fixed set of message types; compile-time safe
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

    public abstract String getDisplayText();   // abstract = each subclass formats display differently

    public static Message createTextMessage(String id, String senderId, String text) { // static = factory method; no instance needed
        return new TextMessage(id, senderId, text);
    }

    public static Message createImageMessage(String id, String senderId, String url) {
        return new ImageMessage(id, senderId, url);
    }

    public static Message createFileMessage(String id, String senderId, String fileName) {
        return new FileMessage(id, senderId, fileName);
    }
}

class TextMessage extends Message {           // extends = inherits from Message; IS-A relationship
    public TextMessage(String id, String senderId, String text) {
        super(id, senderId, text, MessageType.TEXT);
    }

    @Override                                   // @Override = ensures we correctly override parent method
    public String getDisplayText() {
        return getContent();
    }
}

class ImageMessage extends Message {          // extends = inherits Message behavior for images
    public ImageMessage(String id, String senderId, String imageUrl) {
        super(id, senderId, imageUrl, MessageType.IMAGE);
    }

    @Override
    public String getDisplayText() {
        return "[Image: " + getContent() + "]";
    }
}

class FileMessage extends Message {           // extends = inherits Message behavior for files
    public FileMessage(String id, String senderId, String fileName) {
        super(id, senderId, fileName, MessageType.FILE);
    }

    @Override
    public String getDisplayText() {
        return "[File: " + getContent() + "]";
    }
}
