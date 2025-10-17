const mysql = require('mysql2/promise')
const argon2 = require('argon2')
const { v4: uuidv4 } = require('uuid')

async function createAdminUser() {
  const connection = await mysql.createConnection({
    host: 'trolley.proxy.rlwy.net',
    port: 10457,
    user: 'root',
    password: 'MadfCAylCMfHnhzdaHtRSsqsPkKokuOh',
    database: 'railway'
  })

  try {
    console.log('🔌 Connected to database')
    
    // Generate UUID for admin player
    const playerUUID = Buffer.from(uuidv4().replace(/-/g, ''), 'hex')
    
    // Hash password
    const hashedPassword = await argon2.hash('admin123')
    
    console.log('📝 Creating admin user...')
    
    // Create admin user
    await connection.execute(
      'INSERT INTO bm_web_users (player_id, email, password) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE password = ?',
      [playerUUID, 'admin@banmanager.com', hashedPassword, hashedPassword]
    )
    
    console.log('✅ Admin user created!')
    console.log('')
    console.log('🔐 Login Credentials:')
    console.log('   Email: admin@banmanager.com')
    console.log('   Password: admin123')
    console.log('')
    
    // Create Guest role if doesn't exist
    const [roles] = await connection.execute('SELECT role_id FROM bm_web_roles WHERE name = ?', ['Guest'])
    
    let guestRoleId
    if (roles.length === 0) {
      const [result] = await connection.execute('INSERT INTO bm_web_roles (name, parent_role_id) VALUES (?, NULL)', ['Guest'])
      guestRoleId = result.insertId
      console.log('✅ Created Guest role')
    } else {
      guestRoleId = roles[0].role_id
    }
    
    // Create Admin role if doesn't exist
    const [adminRoles] = await connection.execute('SELECT role_id FROM bm_web_roles WHERE name = ?', ['Admin'])
    
    let adminRoleId
    if (adminRoles.length === 0) {
      const [result] = await connection.execute('INSERT INTO bm_web_roles (name, parent_role_id) VALUES (?, ?)', ['Admin', guestRoleId])
      adminRoleId = result.insertId
      console.log('✅ Created Admin role')
    } else {
      adminRoleId = adminRoles[0].role_id
    }
    
    // Assign admin role to user
    await connection.execute(
      'INSERT IGNORE INTO bm_web_player_roles (player_id, role_id) VALUES (?, ?)',
      [playerUUID, adminRoleId]
    )
    
    console.log('✅ Assigned Admin role to user')
    
    // Get or create 'servers' resource
    let [resources] = await connection.execute('SELECT resource_id FROM bm_web_resources WHERE name = ?', ['servers'])
    let serverResourceId
    
    if (resources.length === 0) {
      const [result] = await connection.execute('INSERT INTO bm_web_resources (name) VALUES (?)', ['servers'])
      serverResourceId = result.insertId
    } else {
      serverResourceId = resources[0].resource_id
    }
    
    // Create 'manage' permission for servers resource
    let [permissions] = await connection.execute(
      'SELECT permission_id FROM bm_web_resource_permissions WHERE resource_id = ? AND name = ?',
      [serverResourceId, 'manage']
    )
    
    let managePermissionId
    if (permissions.length === 0) {
      const [result] = await connection.execute(
        'INSERT INTO bm_web_resource_permissions (resource_id, name) VALUES (?, ?)',
        [serverResourceId, 'manage']
      )
      managePermissionId = result.insertId
    } else {
      managePermissionId = permissions[0].permission_id
    }
    
    // Grant admin role full permissions
    await connection.execute(
      'INSERT IGNORE INTO bm_web_role_resources (role_id, resource_id, value) VALUES (?, ?, ?)',
      [adminRoleId, serverResourceId, managePermissionId]
    )
    
    console.log('✅ Granted full permissions to Admin role')
    console.log('')
    console.log('🎉 Setup complete! You can now login at:')
    console.log('   https://banmanagerrail-production.up.railway.app/login')
    
  } catch (error) {
    console.error('❌ Error:', error.message)
    throw error
  } finally {
    await connection.end()
  }
}

createAdminUser().catch(console.error)

