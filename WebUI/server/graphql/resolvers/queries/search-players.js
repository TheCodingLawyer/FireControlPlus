const { unparse } = require('uuid-parse')
const { uniqBy } = require('lodash')
const { parseResolveInfo } = require('graphql-parse-resolve-info')
const { getSql } = require('../../utils')

module.exports = async function searchPlayers (obj, { name, limit }, { state }, info) {
  const fields = parseResolveInfo(info)

  const results = await Promise.all(Array.from(state.serversPool.values()).map(async (server) => {
    const query = getSql(info.schema, server, fields, 'players')
      .where('name', 'like', `${name}%`)
      .orderBy('lastSeen', 'desc')
      .limit(limit)

    return query.exec()
  }))

  const flattened = results.reduce((prev, cur) => prev.concat(cur))
    .map((player) => {
      player.id = unparse(player.id)
      return player
    })

  const unique = uniqBy(flattened, 'id')
  unique.sort((a, b) => (b.lastSeen || 0) - (a.lastSeen || 0))

  return unique.slice(0, limit)
}
