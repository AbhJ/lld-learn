/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Topic.java — Named message channel for publisher/subscriber routing

import java.util.*;

public class Topic {
    private String name;
    private List<Message> messageHistory;

    public Topic(String name) {
        this.name = name;
        this.messageHistory = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messageHistory.add(message);
    }

    public String getName() { return name; }
    public List<Message> getMessageHistory() { return Collections.unmodifiableList(messageHistory); }
    public int getMessageCount() { return messageHistory.size(); }

    @Override
    public String toString() { return "Topic[" + name + "]"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        return name.equals(((Topic) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}
