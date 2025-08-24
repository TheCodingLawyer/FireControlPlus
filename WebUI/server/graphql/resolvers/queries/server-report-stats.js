const ExposedError = require('../../../data/exposed-error')

module.exports = async function serverReportStats (obj, { id, intervalDays }, { state }) {
  if (!state.serversPool.has(id)) throw new ExposedError('Server does not exist')
  const { pool, config } = state.serversPool.get(id)

  const t = config.tables

  const totalRow = await pool(t.playerReports).count({ total: '*' }).first()
  const total = Number(totalRow?.total || 0)

  const totalHistoryValues = await pool(t.playerReports)
    .select(['created'])
    .whereRaw('created >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL ? DAY))', [intervalDays])

  const byDay = new Map()

  for (const row of totalHistoryValues) {
    const day = Math.floor(row.created / 86400) * 86400
    byDay.set(day, (byDay.get(day) || 0) + 1)
  }

  const totalHistory = Array.from(byDay.entries()).sort((a, b) => a[0] - b[0]).map(([date, value]) => ({ date, value }))

  return { total, averageLength: null, totalHistory }
}
