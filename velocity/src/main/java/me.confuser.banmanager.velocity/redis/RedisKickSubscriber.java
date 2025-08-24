package me.confuser.banmanager.velocity.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public final class RedisKickSubscriber {
  private final ProxyServer server;
  private final String redisUrl;
  private final String channel;
  private Thread subThread;

  public RedisKickSubscriber(ProxyServer server) {
    this.server = server;
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
              server.getScheduler().buildTask(server, () -> {
                Optional<com.velocitypowered.api.proxy.Player> opt = server.getPlayer(uuid);
                opt.ifPresent(p -> p.disconnect(Component.text(reason)));
              }).schedule();
            } catch (Throwable ignored) {}
          }
        }, channel);
      } catch (Throwable ignored) {}
    }, "bm-redis-kick-velocity");
    subThread.setDaemon(true);
    subThread.start();
  }

  public void stop() {
    try { if (subThread != null) subThread.interrupt(); } catch (Throwable ignored) {}
  }
} 