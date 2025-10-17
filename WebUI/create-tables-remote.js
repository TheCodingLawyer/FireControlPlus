const mysql = require('mysql2')
const fs = require('fs')
const path = require('path')

const connection = mysql.createConnection({
  host: 'trolley.proxy.rlwy.net',
  port: 10457,
  user: 'root',
  password: 'MadfCAylCMfHnhzdaHtRSsqsPkKokuOh',
  database: 'railway',
  multipleStatements: true
})

const sqlFile = fs.readFileSync(path.join(__dirname, '..', 'CREATE_TABLES.sql'), 'utf8')

console.log('🔌 Connecting to Railway MySQL...')

connection.connect((err) => {
  if (err) {
    console.error('❌ Connection failed:', err.message)
    process.exit(1)
  }
  
  console.log('✅ Connected!')
  console.log('📝 Creating tables...')
  
  connection.query(sqlFile, (error, results) => {
    if (error) {
      console.error('❌ Error creating tables:', error.message)
      connection.end()
      process.exit(1)
    }
    
    console.log('✅ All tables created successfully!')
    console.log(`📊 Created ${results.length} tables`)
    
    connection.end()
    console.log('🎉 Done!')
  })
})

