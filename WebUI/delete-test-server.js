const mysql = require('mysql2/promise')

async function deleteTestServer() {
  const connection = await mysql.createConnection({
    host: 'trolley.proxy.rlwy.net',
    port: 10457,
    user: 'root',
    password: 'MadfCAylCMfHnhzdaHtRSsqsPkKokuOh',
    database: 'railway'
  })

  try {
    console.log('🔌 Connected to database')
    console.log('🗑️  Deleting test server...')
    
    await connection.execute('DELETE FROM bm_web_servers WHERE id = ?', ['test-server'])
    
    console.log('✅ Test server deleted!')
    console.log('')
    console.log('🎉 You can now login without errors!')
    console.log('   https://banmanagerrail-production.up.railway.app/login')
    
  } catch (error) {
    console.error('❌ Error:', error.message)
  } finally {
    await connection.end()
  }
}

deleteTestServer().catch(console.error)

