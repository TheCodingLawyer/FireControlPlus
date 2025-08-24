const { parseResolveInfo } = require('graphql-parse-resolve-info')
const { getSql } = require('../../utils')
const ExposedError = require('../../../data/exposed-error')

// eslint-disable-next-line complexity
module.exports = async function report (obj, { id, serverId }, { session, state }, info) {
  if (!state.serversPool.has(serverId)) throw new ExposedError('Server not found')

  const server = state.serversPool.get(serverId)
  const table = server.config.tables.playerReports

  if (!state.acl.hasServerPermission(serverId, 'player.reports', 'view.any')) {
    if (!session || !session.playerId) throw new ExposedError('You do not have permission to perform this action, please contact your server administrator')

    const canView = state.acl.hasServerPermission(serverId, 'player.reports', 'view.own') ||
      state.acl.hasServerPermission(serverId, 'player.reports', 'view.assigned') ||
      state.acl.hasServerPermission(serverId, 'player.reports', 'view.reported')

    if (!canView) throw new ExposedError('You do not have permission to perform this action, please contact your server administrator')
  }

  const query = getSql(info.schema, server, parseResolveInfo(info), 'playerReport')
    .where({ [`${table}.id`]: id })

  if (!state.acl.hasServerPermission(serverId, 'player.reports', 'view.any')) {
    const aclQuery = server.pool.queryBuilder()

    if (state.acl.hasServerPermission(serverId, 'player.reports', 'view.own')) {
      aclQuery.orWhere(`${table}.actor_id`, session.playerId)
    }
    if (state.acl.hasServerPermission(serverId, 'player.reports', 'view.assigned')) {
      aclQuery.orWhere(`${table}.assignee_id`, session.playerId)
    }
    if (state.acl.hasServerPermission(serverId, 'player.reports', 'view.reported')) {
      aclQuery.orWhere(`${table}.player_id`, session.playerId)
    }

    query.andWhere(aclQuery)
  }

  const [result] = await query.exec()

  if (!result) throw new ExposedError('Report not found')

  return result
}
