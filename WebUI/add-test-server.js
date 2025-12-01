const mysql = require('mysql2')
const { v4: uuidv4 } = require('uuid')

const connection = mysql.createConnection({
  host: 'trolley.proxy.rlwy.net',
  port: 10457,
  user: 'root',
  password: 'MadfCAylCMfHnhzdaHtRSsqsPkKokuOh',
  database: 'railway'
})

// Generate a UUID for the console (server) player
const consoleUUID = Buffer.from(uuidv4().replace(/-/g, ''), 'hex')

const testServer = {
  id: 'test-server',
  name: 'Test Server',
  host: 'localhost',
  port: 3306,
  database: 'minecraft',
  user: 'root',
  password: '',
  console: consoleUUID,
  tables: JSON.stringify({
    playerBans: 'bm_player_bans',
    playerBanRecords: 'bm_player_ban_records',
    playerMutes: 'bm_player_mutes',
    playerMuteRecords: 'bm_player_mute_records',
    playerKicks: 'bm_player_kicks',
    playerNotes: 'bm_player_notes',
    playerReports: 'bm_player_reports',
    playerReportComments: 'bm_player_report_comments',
    playerReportCommands: 'bm_player_report_commands',
    playerReportLocations: 'bm_player_report_locations',
    playerReportStates: 'bm_player_report_states',
    playerWarnings: 'bm_player_warnings',
    ipBans: 'bm_ip_bans',
    ipBanRecords: 'bm_ip_ban_records',
    ipMutes: 'bm_ip_mutes',
    ipMuteRecords: 'bm_ip_mute_records',
    ipRangeBans: 'bm_ip_range_bans',
    ipRangeBanRecords: 'bm_ip_range_ban_records',
    players: 'bm_players',
    playerHistory: 'bm_player_history',
    playerPins: 'bm_player_pins',
    playerSessionHistory: 'bm_player_session_history'
  })
}

console.log('🔌 Connecting to Railway MySQL...')

connection.connect((err) => {
  if (err) {
    console.error('❌ Connection failed:', err.message)
    process.exit(1)
  }
  
  console.log('✅ Connected!')
  console.log('📝 Adding test server configuration...')
  
  connection.query('INSERT INTO bm_web_servers SET ?', testServer, (error, results) => {
    if (error) {
      if (error.code === 'ER_DUP_ENTRY') {
        console.log('⚠️  Test server already exists, updating...')
        connection.query('UPDATE bm_web_servers SET ? WHERE id = ?', [testServer, testServer.id], (updateError) => {
          if (updateError) {
            console.error('❌ Error updating server:', updateError.message)
            connection.end()
            process.exit(1)
          }
          console.log('✅ Test server updated successfully!')
          connection.end()
          console.log('🎉 Done!')
        })
      } else {
        console.error('❌ Error adding server:', error.message)
        connection.end()
        process.exit(1)
      }
    } else {
      console.log('✅ Test server added successfully!')
      console.log(`📊 Server ID: ${testServer.id}`)
      console.log(`📛 Server Name: ${testServer.name}`)
      connection.end()
      console.log('🎉 Done!')
    }
  })
})





