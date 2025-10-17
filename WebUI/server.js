require('dotenv').config()
const requireEnv = require('require-environment-variables')

requireEnv(
  ['ENCRYPTION_KEY',
    'SESSION_KEY',
    'DB_HOST',
    'DB_PORT',
    'DB_USER',
    'DB_NAME',
    'CONTACT_EMAIL',
    'NOTIFICATION_VAPID_PUBLIC_KEY',
    'NOTIFICATION_VAPID_PRIVATE_KEY'
  ])

const logger = require('pino')(
  {
    name: 'banmanager-webui',
    level: process.env.LOG_LEVEL || 'debug',
    redact: {
      paths: ['req.headers.cookie', 'res.headers["set-cookie"]'],
      censor: '*******'
    }
  })
const createApp = require('./server/app')
const { setupPool, setupServersPool } = require('./server/connections')
const port = process.env.PORT || 3000
const dbConfig = {
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  multipleStatements: false
}

;(async () => {
  try {
    const dbPool = await setupPool(dbConfig, logger)
    const serversPool = await setupServersPool({ dbPool, logger })
    const app = await createApp({ dbPool, logger, serversPool, disableUI: process.env.DISABLE_UI === 'true' })

    // Railway requires binding to 0.0.0.0 for proxy to work
    app.listen(port, '0.0.0.0', () => logger.info(`Listening on 0.0.0.0:${port}`))
  } catch (error) {
    logger.error(error)
    process.exit(1)
  }
})()
