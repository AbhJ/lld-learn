/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Subscriber.java — Observer pattern interface for message consumers
//
// WHO IMPLEMENTS THIS? → ConcreteSubscriber (in ConcreteSubscriber.java)
// WHO CALLS IT? → Topic.publish() delivers messages to subscribers
// WHY? → Decouples "message published" from "how subscriber processes it".
//         Different subscriber types can handle messages differently.

interface Subscriber {                // interface = contract for any message consumer
    String getId();
    String getName();
    void onMessage(Message message);
    Filter getFilter();
}
