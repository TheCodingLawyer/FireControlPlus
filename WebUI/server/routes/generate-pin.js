const { hash } = require('../data/hash')
const { isLength } = require('validator')

module.exports = async function ({ request: { body }, throw: throwError, response, state }) {
  if (typeof body.name !== 'string' || !isLength(body.name, { min: 2, max: 17 })) {
    return throwError(400, 'Invalid username')
  }

  if (!body.serverId) {
    return throwError(400, 'Server selection required')
  }

  const server = state.serversPool.get(body.serverId)

  if (!server) {
    return throwError(400, 'Server does not exist')
  }

  // Generate a random 6-digit PIN
  const pin = Math.floor(100000 + Math.random() * 900000).toString()
  const hashedPin = await hash(pin)
  
  // Set expiration to 5 minutes from now
  const expires = Math.floor(Date.now() / 1000) + (5 * 60)

  const { playerPins, players } = server.config.tables

  try {
    // Get or create player
    let playerId
    const existingPlayer = await server.pool(players)
      .select('id')
      .where('name', body.name)
      .first()

    if (existingPlayer) {
      playerId = existingPlayer.id
    } else {
      // For banned players who might not exist in the players table yet
      // We'll need to handle this case - for now, require the player to exist
      return throwError(400, 'Player not found. Please contact an administrator.')
    }

    // Delete any existing PINs for this player
    await server.pool(playerPins)
      .where('player_id', playerId)
      .del()

    // Insert new PIN
    await server.pool(playerPins)
      .insert({
        player_id: playerId,
        pin: hashedPin,
        expires: expires
      })

    response.body = { pin }
  } catch (error) {
    console.error('PIN generation error:', error)
    return throwError(500, 'Failed to generate PIN. Please try again.')
  }
}
