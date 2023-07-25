package com.turikhay.mc.bungee;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class BungeeCordJoinWebhook extends Plugin implements Listener {

    private WebhookClient client;

    @Override
    public void onEnable() {
        try {
            this.client = createClient();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Couldn't create webhook client");
            throw new RuntimeException("Couldn't initialize the client", e);
        }
        getProxy().getPluginManager().registerListener(this, this);
    }

    private WebhookClient createClient() throws Exception {
        Configuration config = initConfig();
        URL webhookUrl = new URL(config.getString("webhookUrl"));
        return AsyncWebhookClient.of(new SimpleWebhookClient(webhookUrl));
    }

    private Configuration initConfig() throws Exception {
        if (!getDataFolder().isDirectory()) {
            if (!getDataFolder().mkdirs()) {
                throw new IOException("couldn't create data folder: " + getDataFolder().getAbsolutePath());
            }
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.isFile()) {
            try (
                    InputStream defaultConfig = Objects.requireNonNull(getResourceAsStream("config.yml"), "defaultConfig");
                    FileOutputStream targetConfig = new FileOutputStream(configFile)
            ) {
                ByteStreams.copy(defaultConfig, targetConfig);
            }
        }
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
    }

    @Override
    public void onDisable() {
        if (client != null) {
            client.cleanUp();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(LoginEvent event) {
        if (event.isCancelled()) {
            return;
        }
        postEvent("join", event.getConnection());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        postEvent("quit", event.getPlayer());
    }

    private void postEvent(String name, Connection connection) {
        if (client != null) {
            client.postEvent(name, extract(connection));
        }
    }

    private static Map<String, String> extract(Connection c) {
        ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
        b.put("address", formatAddress(c.getSocketAddress()));
        if (c instanceof PendingConnection) {
            b.put("username", ((PendingConnection) c).getName());
        } else if (c instanceof ProxiedPlayer) {
            b.put("username", ((ProxiedPlayer) c).getName());
        }
        return b.build();
    }

    private static String formatAddress(SocketAddress sa) {
        if (sa instanceof InetSocketAddress) {
            InetSocketAddress inet = (InetSocketAddress) sa;
            return inet.getHostString() + ':' + inet.getPort();
        }
        return sa.toString();
    }
}
