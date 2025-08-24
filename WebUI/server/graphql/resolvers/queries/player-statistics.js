const { tables: defaultTables } = require('../../../data/tables')

module.exports = async function playerStatistics (obj, { player }, { state }) {
  const servers = await Promise.all(Array.from(state.serversPool.values()).map(async ({ config, pool }) => {
    const t = { ...defaultTables, ...config.tables }

    const { totalActiveBans = 0 } = await pool(t.playerBans)
      .select(pool.raw('COUNT(*) AS `totalActiveBans`'))
      .where('player_id', player)
      .first() || {}
    const { totalActiveMutes = 0 } = await pool(t.playerMutes)
      .select(pool.raw('COUNT(*) AS `totalActiveMutes`'))
      .where('player_id', player)
      .first() || {}
    const { totalBans = 0 } = await pool(t.playerBanRecords)
      .select(pool.raw('COUNT(*) AS `totalBans`'))
      .where('player_id', player)
      .first() || {}
    const { totalMutes = 0 } = await pool(t.playerMuteRecords)
      .select(pool.raw('COUNT(*) AS `totalMutes`'))
      .where('player_id', player)
      .first() || {}
    const { totalWarnings = 0 } = await pool(t.playerWarnings)
      .select(pool.raw('COUNT(*) AS `totalWarnings`'))
      .where('player_id', player)
      .first() || {}

    // Reports: BanManager only
    let totalReports = 0
    try {
      const row = await pool(t.playerReports)
        .select(pool.raw('COUNT(*) AS `totalReports`'))
        .where('player_id', player)
        .first() || {}
      totalReports = row.totalReports || 0
    } catch {}

    const { totalKicks = 0 } = await pool(t.playerKicks)
      .select(pool.raw('COUNT(*) AS `totalKicks`'))
      .where('player_id', player)
      .first() || {}

    return { totalActiveBans, totalActiveMutes, totalBans, totalMutes, totalWarnings, totalReports, totalKicks }
  }))

  return servers.reduce((prev, server) => {
    prev.totalActiveBans += server.totalActiveBans
    prev.totalActiveMutes += server.totalActiveMutes
    prev.totalBans += server.totalBans
    prev.totalMutes += server.totalMutes
    prev.totalWarnings += server.totalWarnings
    prev.totalReports += server.totalReports
    prev.totalKicks += server.totalKicks

    return prev
  }, { totalActiveBans: 0, totalActiveMutes: 0, totalBans: 0, totalMutes: 0, totalWarnings: 0, totalReports: 0, totalKicks: 0 })
}
