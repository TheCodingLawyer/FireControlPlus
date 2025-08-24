package me.confuser.banmanager.bungee.util;

import java.lang.reflect.Method;
import java.util.UUID;

public final class FloodgateUtil {
  private FloodgateUtil() {}

  public static class FloodgateIdentity {
    public final UUID uuid;
    public final String name;

    public FloodgateIdentity(UUID uuid, String name) {
      this.uuid = uuid;
      this.name = name;
    }
  }

  public static FloodgateIdentity remapIdentity(UUID originalUuid, String originalName) {
    try {
      Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
      Method getInstance = apiClass.getMethod("getInstance");
      Object api = getInstance.invoke(null);
      if (api == null) return null;

      Method isFloodgatePlayer = apiClass.getMethod("isFloodgatePlayer", UUID.class);
      Boolean bedrock = (Boolean) isFloodgatePlayer.invoke(api, originalUuid);
      if (bedrock == null || !bedrock) return null;

      Method getPlayer = apiClass.getMethod("getPlayer", UUID.class);
      Object fgPlayer = getPlayer.invoke(api, originalUuid);
      if (fgPlayer == null) return null;

      Method getCorrectUniqueId = fgPlayer.getClass().getMethod("getCorrectUniqueId");
      Method getUsername = fgPlayer.getClass().getMethod("getUsername");

      UUID newUuid = (UUID) getCorrectUniqueId.invoke(fgPlayer);
      String newName = (String) getUsername.invoke(fgPlayer);

      if (newUuid == null) newUuid = originalUuid;
      if (newName == null || newName.isEmpty()) newName = originalName;

      return new FloodgateIdentity(newUuid, newName);
    } catch (ClassNotFoundException ignored) {
      return null;
    } catch (Throwable ignored) {
      return null;
    }
  }
} 