/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MessageListener.java — Observer contract for new-message events
//
// Subscribers are notified when a message is delivered to a chat room they
// care about. Examples: a console display, a push-notification dispatcher, a
// desktop toast, an email digest. NotificationManager fans out events;
// listeners decide how to render them.

interface MessageListener {
    /** A new message was delivered to the room. */
    void onMessage(Message message, ChatRoom room);
}
