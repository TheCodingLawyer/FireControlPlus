const { parseResolveInfo } = require('graphql-parse-resolve-info')
const { getSql } = require('../../utils')
const ExposedError = require('../../../data/exposed-error')

module.exports = async function playerChatHistory (obj, { playerId, serverId, limit = 50 }, { session, state }, info) {
  if (!state.serversPool.has(serverId)) throw new ExposedError('Server not found')

  const server = state.serversPool.get(serverId)
  const table = server.config.tables.playerChatHistory

  if (!table) {
    // Return empty array if chat history table doesn't exist
    console.warn(`playerChatHistory table not configured for server ${serverId}`)
    return []
  }

  if (!state.acl.hasServerPermission(serverId, 'player.reports', 'view.any')) {
    if (!session || !session.playerId) throw new ExposedError('You do not have permission to perform this action, please contact your server administrator')

    const canView = state.acl.hasServerPermission(serverId, 'player.reports', 'view.own') ||
      state.acl.hasServerPermission(serverId, 'player.reports', 'view.assigned') ||
      state.acl.hasServerPermission(serverId, 'player.reports', 'view.reported')

    if (!canView) throw new ExposedError('You do not have permission to perform this action, please contact your server administrator')
  }

  try {
    // Convert UUID to hex string for logging
    const uuidHex = Buffer.from(playerId, 'binary').toString('hex')
    const formattedUuid = [
      uuidHex.slice(0, 8),
      uuidHex.slice(8, 12),
      uuidHex.slice(12, 16),
      uuidHex.slice(16, 20),
      uuidHex.slice(20, 32)
    ].join('-')
    
    console.log(`Fetching chat history for player ${formattedUuid} from table ${table}`)
    
    // Get player name for better logging
    const playersTable = server.config.tables.players
    let playerName = 'Unknown'
    
    if (playersTable) {
      try {
        const playerResult = await server.pool(playersTable)
          .select('name')
          .where('id', playerId)
          .first()
        
        if (playerResult) {
          playerName = playerResult.name
        }
      } catch (e) {
        // Ignore player name lookup errors
      }
    }
    
    const query = server.pool(table)
      .select('*')
      .where('player_id', playerId)
      .orderBy('created', 'desc')
      .limit(limit)

    const results = await query
    
    console.log(`Found ${results.length} chat messages for player ${playerName} (${formattedUuid})`)

    return results.map(row => ({
      id: row.id,
      message: row.message,
      world: row.world,
      x: row.x,
      y: row.y,
      z: row.z,
      created: row.created
    }))
  } catch (error) {
    // If table doesn't exist or query fails, return empty array
    console.error('Failed to fetch chat history:', error.message)
    console.error('Error details:', error)
    return []
  }
}
