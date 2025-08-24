const { getSql } = require('../../utils')
const { parseResolveInfo, simplifyParsedResolveInfoFragmentWithType } = require('graphql-parse-resolve-info')

module.exports = async function listReports (obj, { player, assigned, actor, stateId, serverId, limit, offset, order }, { state }, info) {
  const server = state.serversPool.get(serverId)
  const data = {}

  // BanManager reports only
  const aclFilter = []

  if (!state.acl.hasServerPermission(serverId, 'player.reports', 'view.any')) {
    aclFilter.push(['assignee_id', state.session.playerId])
  }

  const filter = aclFilter.length ? { $or: aclFilter } : {}

  if (actor) filter.actor_id = actor
  if (assigned) filter.assignee_id = assigned
  if (player) filter.player_id = player
  if (stateId) filter.state_id = stateId

  const table = server.config.tables.playerReports
  const total = await server.pool(table).where(filter).count({ total: '*' }).first()

  data.total = Number(total?.total || 0)

  const parsed = parseResolveInfo(info)
  const { fields } = simplifyParsedResolveInfoFragmentWithType(parsed, info.returnType)

  if (fields.records) {
    const query = getSql(info.schema, server, fields.records, 'playerReports')
      .where(filter)
      .limit(limit)
      .offset(offset)

    if (order) query.orderByRaw(order.replace('_', ' '))

    const records = await query.exec()

    data.records = records
  }

  return data
}
