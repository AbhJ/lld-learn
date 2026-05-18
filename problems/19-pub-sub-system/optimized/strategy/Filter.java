/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Filter.java — Interchangeable message filtering rules for subscribers

public interface Filter {                         // interface = pluggable message filtering contract
    boolean matches(Message message);
    String getDescription();
}

class PayloadContainsFilter implements Filter {
    private String keyword;

    public PayloadContainsFilter(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean matches(Message message) {
        return message.getPayload().contains(keyword);
    }

    @Override
    public String getDescription() {
        return "payload contains '" + keyword + "'";
    }
}

class HeaderFilter implements Filter {
    private String headerKey;
    private String headerValue;

    public HeaderFilter(String key, String value) {
        this.headerKey = key;
        this.headerValue = value;
    }

    @Override
    public boolean matches(Message message) {
        String val = message.getHeader(headerKey);
        return headerValue.equals(val);
    }

    @Override
    public String getDescription() {
        return "header[" + headerKey + "]=" + headerValue;
    }
}

class AllPassFilter implements Filter {
    @Override
    public boolean matches(Message message) { return true; }

    @Override
    public String getDescription() { return "all"; }
}
