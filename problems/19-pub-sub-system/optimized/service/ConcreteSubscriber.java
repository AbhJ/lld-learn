/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConcreteSubscriber.java — Concrete message consumer that prints received messages
//
// This class IMPLEMENTS Subscriber (defined in Subscriber.java).

class ConcreteSubscriber implements Subscriber { // implements = fulfills the Subscriber interface
    private String id;                           // private = encapsulates subscriber identity
    private String name;
    private Filter filter;                       // private = each subscriber has its own message filter
    private int messagesReceived;                // private = internal counter for stats

    public ConcreteSubscriber(String id, String name) {
        this.id = id;
        this.name = name;
        this.filter = new AllPassFilter();
        this.messagesReceived = 0;
    }

    public ConcreteSubscriber(String id, String name, Filter filter) {
        this(id, name);
        this.filter = filter;
    }

    @Override
    public void onMessage(Message message) {
        messagesReceived++;
        System.out.printf("  [%s] Received: %s%n", name, message.getPayload());
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public Filter getFilter() { return filter; }

    public int getMessagesReceived() { return messagesReceived; }
}
