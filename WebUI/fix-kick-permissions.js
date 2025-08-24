const mysql = require('mysql2/promise');

async function fixKickPermissions() {
  console.log('🔧 Fixing kick permissions...');
  
  const db = await mysql.createConnection({
    host: '172.18.0.1',
    port: 3306,
    user: 'u19_j6VEaTQ4tz',
    password: '@L.NXjiJc506Y5S8xHX.33NU',
    database: 's19_BanManager'
  });

  try {
    // 1. Ensure player.kicks resource exists
    await db.execute(`
      INSERT IGNORE INTO bm_web_resources (name) VALUES ('player.kicks')
    `);
    console.log('✅ player.kicks resource ensured');

    // 2. Ensure kick permissions exist
    const [resource] = await db.execute(`
      SELECT resource_id FROM bm_web_resources WHERE name = 'player.kicks' LIMIT 1
    `);
    
    if (resource.length) {
      const resourceId = resource[0].resource_id;
      
      // Add all kick permissions
      const permissions = ['create', 'view', 'update.any', 'update.own', 'delete.any', 'delete.own'];
      let permValue = 1;
      
      for (const perm of permissions) {
        await db.execute(`
          INSERT IGNORE INTO bm_web_resource_permissions (resource_id, name, value) 
          VALUES (?, ?, ?)
        `, [resourceId, perm, permValue]);
        permValue *= 2;
      }
      console.log('✅ Kick permissions created');

      // 3. Give Admin role (ID 3) all kick permissions
      const totalValue = 63; // All permissions combined (1+2+4+8+16+32)
      await db.execute(`
        INSERT INTO bm_web_role_resources (role_id, resource_id, value) 
        VALUES (3, ?, ?) 
        ON DUPLICATE KEY UPDATE value = ?
      `, [resourceId, totalValue, totalValue]);
      console.log('✅ Admin role given kick permissions');
    }

    // 4. Create dummy player record for angry_merchant
    const username = 'angry_merchant';
    const dummyUuid = '12345678-1234-4444-8888-123456789abc';
    
    await db.execute(`
      INSERT IGNORE INTO bm_players (id, name, ip, lastSeen) 
      VALUES (UNHEX(REPLACE(?, '-', '')), ?, INET6_ATON('127.0.0.1'), UNIX_TIMESTAMP())
    `, [dummyUuid, username]);

    // 5. Assign user to Admin role
    const [player] = await db.execute(`
      SELECT id FROM bm_players WHERE name = ? LIMIT 1
    `, [username]);
    
    if (player.length) {
      await db.execute(`
        INSERT IGNORE INTO bm_web_player_roles (player_id, role_id) VALUES (?, 3)
      `, [player[0].id]);
      console.log('✅ angry_merchant assigned Admin role');
    }

    console.log('🎉 Kick permissions fixed! Restart the WebUI and the kick button should appear.');
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await db.end();
  }
}

if (require.main === module) {
  fixKickPermissions().catch(console.error);
}

module.exports = fixKickPermissions; 