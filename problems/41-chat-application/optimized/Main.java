/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the optimized chat application
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Chat Application Demo (Optimized) ===");

        ChatService chatService = new ChatService();

        User alice = chatService.registerUser("Alice");
        User bob = chatService.registerUser("Bob");
        User charlie = chatService.registerUser("Charlie");
        System.out.println("Users created: Alice, Bob, Charlie");

        chatService.setUserStatus(alice.getId(), OnlineStatus.ONLINE);
        chatService.setUserStatus(bob.getId(), OnlineStatus.ONLINE);
        chatService.setUserStatus(charlie.getId(), OnlineStatus.ONLINE);

        System.out.println("\n--- Direct Chat: Alice & Bob ---");
        ChatRoom directChat = chatService.createDirectChat(alice.getId(), bob.getId());
        chatService.sendMessage(directChat.getId(), alice.getId(), "Hey Bob, how are you?");
        chatService.sendMessage(directChat.getId(), bob.getId(), "I'm great! Thanks for asking.");

        System.out.println("\n--- Group Chat: Project Team ---");
        ChatRoom groupChat = chatService.createGroupChat("Project Team", alice.getId(),
            Arrays.asList(alice.getId(), bob.getId(), charlie.getId()));
        chatService.sendMessage(groupChat.getId(), alice.getId(), "Meeting at 3pm today");
        chatService.sendMessage(groupChat.getId(), charlie.getId(), "Got it, thanks!");

        System.out.println("\n--- Typing Indicators ---");
        chatService.startTyping(groupChat.getId(), alice.getId());
        chatService.stopTyping(groupChat.getId(), alice.getId());

        System.out.println("\n--- Message History ---");
        System.out.println("Direct chat history (Alice & Bob):");
        List<Message> directHistory = chatService.getMessageHistory(directChat.getId());
        for (Message msg : directHistory) {
            User sender = chatService.getUser(msg.getSenderId());
            System.out.println("  [" + sender.getName() + "]: " + msg.getDisplayText());
        }

        System.out.println("Group chat history (Project Team):");
        List<Message> groupHistory = chatService.getMessageHistory(groupChat.getId());
        for (Message msg : groupHistory) {
            User sender = chatService.getUser(msg.getSenderId());
            System.out.println("  [" + sender.getName() + "]: " + msg.getDisplayText());
        }

        System.out.println("\n--- Different Message Types ---");
        chatService.sendMessage(directChat.getId(), alice.getId(), "photo.jpg", Message.MessageType.IMAGE);
        chatService.sendMessage(directChat.getId(), bob.getId(), "report.pdf", Message.MessageType.FILE);

        System.out.println("\n--- Status Changes ---");
        chatService.setUserStatus(bob.getId(), OnlineStatus.AWAY);
        chatService.setUserStatus(charlie.getId(), OnlineStatus.OFFLINE);

        System.out.println("\n=== Chat Application Demo Complete ===");
    }
}
