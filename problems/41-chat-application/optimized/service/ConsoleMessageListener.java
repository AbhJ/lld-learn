/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConsoleMessageListener.java — Console listener; demonstrates observing message events
//
// One listener per recipient: receives onMessage callbacks for any room the
// recipient participates in. NotificationManager filters by room membership
// and online status before invoking us.

class ConsoleMessageListener implements MessageListener {
    private final String recipientName;

    public ConsoleMessageListener(String recipientName) {
        this.recipientName = recipientName;
    }

    @Override
    public void onMessage(Message message, ChatRoom room) {
        String label = (room instanceof GroupChat)
                ? "new message in " + room.getName()
                : "new direct message";
        System.out.println("  [event] " + recipientName + " <- " + label
                + ": " + message.getDisplayText());
    }
}
