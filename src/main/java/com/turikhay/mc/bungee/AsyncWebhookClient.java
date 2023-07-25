package com.turikhay.mc.bungee;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncWebhookClient implements WebhookClient {
    private final Executor executor;
    private final Runnable cleanUp;
    private final WebhookClient delegate;

    public AsyncWebhookClient(Executor executor, Runnable cleanUp, WebhookClient delegate) {
        this.executor = executor;
        this.cleanUp = cleanUp;
        this.delegate = delegate;
    }

    @Override
    public void postEvent(String name, Map<String, String> data) {
        executor.execute(() -> delegate.postEvent(name, data));
    }

    @Override
    public void cleanUp() {
        Exception suppressed = null;
        try {
            cleanUp.run();
        } catch (Exception e0) {
            suppressed = e0;
        }
        try {
            delegate.cleanUp();
        } catch (Exception e1) {
            if (suppressed != null) {
                e1.addSuppressed(suppressed);
            }
            throw e1;
        }
    }

    public static AsyncWebhookClient of(ExecutorService service, WebhookClient delegate) {
        return new AsyncWebhookClient(service, service::shutdown, delegate);
    }

    public static AsyncWebhookClient of(WebhookClient delegate) {
        return of(
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                        .setNameFormat(AsyncWebhookClient.class.getSimpleName())
                        .build()
                ),
                delegate
        );
    }
}
