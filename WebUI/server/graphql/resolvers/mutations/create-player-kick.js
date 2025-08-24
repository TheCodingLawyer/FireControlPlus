const ExposedError = require('../../../data/exposed-error')
const playerKick = require('../queries/player-kick')

module.exports = async function createPlayerKick (obj, { input }, { session, state }, info) {
  const server = state.serversPool.get(input.server)
  if (!server) throw new ExposedError('Server does not exist')

  const table = server.config.tables.playerKicks
  const player = input.player
  const actor = session.playerId

  let id

  try {
    const insertResult = await server.pool(table).insert({
      player_id: player,
      actor_id: actor,
      reason: input.reason,
      created: server.pool.raw('UNIX_TIMESTAMP()')
    })

    id = insertResult[0] // MySQL returns insertId as first element
  } catch (e) {
    throw e
  }

  // Queue kick command for game servers to pick up
  try {
    await state.dbPool('bm_pending_commands').insert({
      server_id: input.server,
      command: 'kick',
      player_id: player,
      actor_id: actor,
      args: input.reason,
      created: Math.floor(Date.now() / 1000)
    })
  } catch (e) {
    // Don't fail the kick if command queueing fails
    console.error('Failed to queue kick command:', e)
  }

  return playerKick(obj, { id, serverId: input.server }, { state }, info)
} 