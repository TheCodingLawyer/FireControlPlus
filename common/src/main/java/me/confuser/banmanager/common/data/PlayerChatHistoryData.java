package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerChatHistoryData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @DatabaseField(canBeNull = false, columnDefinition = "TEXT NOT NULL")
  @Getter
  private String message;

  @DatabaseField(canBeNull = false, columnDefinition = "VARCHAR(255) NOT NULL")
  @Getter
  private String world;

  @DatabaseField(canBeNull = false)
  @Getter
  private int x;

  @DatabaseField(canBeNull = false)
  @Getter
  private int y;

  @DatabaseField(canBeNull = false)
  @Getter
  private int z;

  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  PlayerChatHistoryData() {

  }

  public PlayerChatHistoryData(PlayerData player, String message, String world, int x, int y, int z) {
    this.player = player;
    this.message = message;
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public PlayerChatHistoryData(PlayerData player, String message, String world, int x, int y, int z, long created) {
    this(player, message, world, x, y, z);
    this.created = created;
  }

}
