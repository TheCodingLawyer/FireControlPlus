package me.confuser.banmanager.bungee.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.UUID;

public final class RedisKickSubscriber {
  private final Plugin plugin;
  private final String redisUrl;
  private final String channel;
  private Thread subThread;

  public RedisKickSubscriber(Plugin plugin) {
    this.plugin = plugin;
    this.redisUrl = System.getProperty("REDIS_URL", System.getenv().getOrDefault("REDIS_URL", "redis://127.0.0.1:6379"));
    this.channel = System.getProperty("BM_CHANNEL", System.getenv().getOrDefault("BM_CHANNEL", "bm:kick"));
  }

  public void start() {
    subThread = new Thread(() -> {
      try (Jedis jedis = new Jedis(URI.create(redisUrl))) {
        jedis.subscribe(new JedisPubSub() {
          @Override
          public void onMessage(String ch, String message) {
            try {
              JsonObject json = JsonParser.parseString(message).getAsJsonObject();
              UUID uuid = UUID.fromString(json.get("playerId").getAsString());
              String reason = json.get("reason").getAsString();
              ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(uuid);
                if (p != null) {
                  p.disconnect(TextComponent.fromLegacyText(reason));
                }
              });
            } catch (Throwable ignored) {}
          }
        }, channel);
      } catch (Throwable ignored) {}
    }, "bm-redis-kick-bungee");
    subThread.setDaemon(true);
    subThread.start();
  }

  public void stop() {
    try { if (subThread != null) subThread.interrupt(); } catch (Throwable ignored) {}
  }
} 