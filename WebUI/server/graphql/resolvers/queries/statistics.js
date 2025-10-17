const ExposedError = require('../../../data/exposed-error')

// Inline provider to avoid module loading issues
async function getReportsProvider ({ pool, config }) {
  try {
    const result = await pool.raw('SHOW TABLES LIKE \'tigerreports_reports\'')
    if (result && result[0] && result[0].length > 0) {
      return 'tigerreports'
    }
  } catch (error) {
    // Table doesn't exist, fall back to default
  }
  return 'banmanager'
}

module.exports = async function statistics (obj, args, { state }) {
  const data = {
    totalActiveBans: 0,
    totalActiveMutes: 0,
    totalPlayers: 0,
    totalAppeals: 0,
    totalKicks: 0,
    totalReports: 0,
    totalWarnings: 0
  }

  for (const { pool, config } of state.serversPool.values()) {
    // All Bans (Active + Past)
    try {
      const activeBansRow = await pool(config.tables.playerBans).count({ total: '*' }).first()
      const pastBansRow = await pool(config.tables.playerBanRecords).count({ total: '*' }).first()
      data.totalActiveBans += Number(activeBansRow?.total || 0) + Number(pastBansRow?.total || 0)
    } catch {}

    // All Mutes (Active + Past)
    try {
      const activeMutesRow = await pool(config.tables.playerMutes).count({ total: '*' }).first()
      const pastMutesRow = await pool(config.tables.playerMuteRecords).count({ total: '*' }).first()
      data.totalActiveMutes += Number(activeMutesRow?.total || 0) + Number(pastMutesRow?.total || 0)
    } catch {}

    // Kicks
    try {
      const kicksRow = await pool(config.tables.playerKicks).count({ total: '*' }).first()
      data.totalKicks += Number(kicksRow?.total || 0)
    } catch {}

    // Reports: support TigerReports
    try {
      const provider = await getReportsProvider({ pool, config })
      if (provider === 'tigerreports') {
        const row = await pool('tigerreports_reports').where('archived', 0).count({ total: 'report_id' }).first()
        data.totalReports += Number(row?.total || 0)
      } else {
        const reportsRow = await pool(config.tables.playerReports).count({ total: '*' }).first()
        data.totalReports += Number(reportsRow?.total || 0)
      }
    } catch {}

    // Warnings
    try {
      const warningsRow = await pool(config.tables.playerWarnings).count({ total: '*' }).first()
      data.totalWarnings += Number(warningsRow?.total || 0)
    } catch {}

    // Players
    try {
      const playersRow = await pool(config.tables.players).count({ total: '*' }).first()
      data.totalPlayers += Number(playersRow?.total || 0)
    } catch {}

    // Appeals
    try {
      const appealsRow = await pool('bm_web_appeals').count({ total: '*' }).first()
      data.totalAppeals += Number(appealsRow?.total || 0)
    } catch {}
  }

  return data
}
