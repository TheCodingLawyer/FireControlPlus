let client = null
let ready = false

async function getClient () {
  if (client || ready) return client

  const url = process.env.REDIS_URL || 'redis://127.0.0.1:6379'

  // Allow explicit disable if variable is set to empty
  if (!url) {
    ready = true
    return null
  }

  try {
    // Lazy require to avoid mandatory dependency
    // eslint-disable-next-line n/no-missing-require
    const { createClient } = require('redis')
    client = createClient({ url })
    client.on('error', () => {})
    await client.connect()
    ready = true
  } catch (e) {
    ready = true
    client = null
  }

  return client
}

async function publishKick (payload) {
  try {
    const cli = await getClient()
    if (!cli) return false

    await cli.publish('bm:kick', JSON.stringify(payload))
    return true
  } catch (_) {
    return false
  }
}

module.exports = { publishKick } 