// Mock Session API for Frontend Demo
// Accepts any login credentials and creates a demo session

export default async function handler(req, res) {
  // Handle CORS
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, DELETE, OPTIONS')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type')
  res.setHeader('Access-Control-Allow-Credentials', 'true')

  if (req.method === 'OPTIONS') {
    return res.status(200).end()
  }

  // POST - Login (PIN or Password)
  if (req.method === 'POST') {
    const { name, pin, email, password, serverId } = req.body

    console.log('[Mock Session] Login attempt:', { name, pin: pin ? '******' : undefined, email, serverId })

    // PIN Login - requires name, pin, serverId
    if (pin) {
      // Accept any 6-digit PIN
      if (!name) {
        return res.status(400).json({ error: 'Username is required' })
      }
      if (!serverId) {
        return res.status(400).json({ error: 'Please select a server' })
      }
      if (pin.length !== 6) {
        return res.status(400).json({ error: 'PIN must be 6 digits' })
      }

      // Success! Return demo user data
      return res.status(200).json({
        hasAccount: false,
        playerId: 'e6b5c088-0680-44df-9e1b-9bf11792291b',
        playerName: name,
      })
    }

    // Password Login - requires email, password
    if (email && password) {
      if (password.length < 6) {
        return res.status(400).json({ error: 'Password must be at least 6 characters' })
      }

      // Success! Return 204 No Content (as expected by password form)
      return res.status(204).end()
    }

    return res.status(400).json({ error: 'Invalid login request' })
  }

  // DELETE - Logout
  if (req.method === 'DELETE') {
    console.log('[Mock Session] Logout')
    return res.status(204).end()
  }

  // GET - Check session
  if (req.method === 'GET') {
    return res.status(200).json({
      id: 'e6b5c088-0680-44df-9e1b-9bf11792291b',
      name: 'DemoPlayer',
      hasSession: true,
    })
  }

  return res.status(405).json({ error: 'Method not allowed' })
}


