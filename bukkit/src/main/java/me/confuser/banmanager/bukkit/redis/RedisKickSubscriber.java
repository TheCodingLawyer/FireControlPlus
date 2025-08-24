package me.confuser.banmanager.bukkit.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.UUID;

public final class RedisKickSubscriber {
  private final Plugin plugin;
  private final String redisUrl;
  private final String channel;
  private Thread subscriberThread;

  public RedisKickSubscriber(Plugin plugin) {
    this.plugin = plugin;
    this.redisUrl = System.getProperty("REDIS_URL", System.getenv().getOrDefault("REDIS_URL", "redis://127.0.0.1:6379"));
    this.channel = System.getProperty("BM_CHANNEL", System.getenv().getOrDefault("BM_CHANNEL", "bm:kick"));
  }

  public void start() {
    subscriberThread = new Thread(() -> {
      try (Jedis jedis = new Jedis(URI.create(redisUrl))) {
        jedis.subscribe(new JedisPubSub() {
          @Override
          public void onMessage(String ch, String message) {
            try {
              JsonObject json = JsonParser.parseString(message).getAsJsonObject();
              String playerId = json.get("playerId").getAsString();
              String reason = json.get("reason").getAsString();
              UUID uuid = UUID.fromString(playerId);

              Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                  try {
                    p.kick();
                  } catch (Throwable ignored) {
                    p.kickPlayer(reason);
                  }
                }
              });
            } catch (Throwable ignored) {}
          }
        }, channel);
      } catch (Throwable t) {
        plugin.getLogger().warning("Redis subscriber stopped: " + t.getMessage());
      }
    }, "bm-redis-kick-bukkit");
    subscriberThread.setDaemon(true);
    subscriberThread.start();
  }

  public void stop() {
    try {
      if (subscriberThread != null) subscriberThread.interrupt();
    } catch (Throwable ignored) {}
  }
} 