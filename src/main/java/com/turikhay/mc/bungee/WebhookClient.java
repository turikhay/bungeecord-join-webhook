package com.turikhay.mc.bungee;

import java.util.Map;

public interface WebhookClient {
    void postEvent(String name, Map<String, String> data);
    void cleanUp();
}
