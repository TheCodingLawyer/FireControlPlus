package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerChatHistoryData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class PlayerChatHistoryStorage extends BaseDaoImpl<PlayerChatHistoryData, Integer> {

  public PlayerChatHistoryStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerChatHistoryData>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerChatHistory"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }
    }
  }

  public PlayerChatHistoryStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerChatHistoryData>) table);
  }

  public List<PlayerChatHistoryData> getRecentMessages(PlayerData player, int limit) throws SQLException {
    return queryBuilder()
        .where().eq("player_id", player)
        .query()
        .stream()
        .sorted((a, b) -> Long.compare(b.getCreated(), a.getCreated()))
        .limit(limit)
        .collect(java.util.stream.Collectors.toList());
  }

  public void cleanupOldMessages(long cutoffTime) throws SQLException {
    DeleteBuilder<PlayerChatHistoryData, Integer> builder = deleteBuilder();
    builder.where().lt("created", cutoffTime);
    builder.delete();
  }

}
