const mysql = require('mysql2/promise');

async function fixKickViewPermissions() {
  console.log('🔧 Fixing kick view permissions for all users...');
  
  const db = await mysql.createConnection({
    host: '172.18.0.1',
    port: 3306,
    user: 'u19_j6VEaTQ4tz',
    password: '@L.NXjiJc506Y5S8xHX.33NU',
    database: 's19_BanManager'
  });

  try {
    // 1. Get the player.kicks resource ID
    const [resource] = await db.execute(`
      SELECT resource_id FROM bm_web_resources WHERE name = 'player.kicks' LIMIT 1
    `);
    
    if (!resource.length) {
      console.log('❌ player.kicks resource not found');
      return;
    }
    
    const resourceId = resource[0].resource_id;
    console.log(`✅ Found player.kicks resource with ID: ${resourceId}`);

    // 2. Get the view permission value
    const [permission] = await db.execute(`
      SELECT value FROM bm_web_resource_permissions 
      WHERE resource_id = ? AND name = 'view' LIMIT 1
    `, [resourceId]);
    
    if (!permission.length) {
      console.log('❌ view permission not found for player.kicks');
      return;
    }
    
    const viewValue = permission[0].value;
    console.log(`✅ Found view permission with value: ${viewValue}`);

    // 3. Add view permission to Guest role (role_id = 1) if not already present
    const [guestRole] = await db.execute(`
      SELECT value FROM bm_web_role_resources 
      WHERE role_id = 1 AND resource_id = ? LIMIT 1
    `, [resourceId]);
    
    if (guestRole.length) {
      const currentValue = guestRole[0].value;
      if ((currentValue & viewValue) === 0) {
        // Permission not present, add it
        await db.execute(`
          UPDATE bm_web_role_resources 
          SET value = value | ? 
          WHERE role_id = 1 AND resource_id = ?
        `, [viewValue, resourceId]);
        console.log('✅ Added kick view permission to Guest role');
      } else {
        console.log('✅ Guest role already has kick view permission');
      }
    } else {
      // Create role resource entry
      await db.execute(`
        INSERT INTO bm_web_role_resources (role_id, resource_id, value) 
        VALUES (1, ?, ?)
      `, [resourceId, viewValue]);
      console.log('✅ Created kick view permission for Guest role');
    }

    // 4. Add view permission to Logged In role (role_id = 2) if not already present
    const [loggedInRole] = await db.execute(`
      SELECT value FROM bm_web_role_resources 
      WHERE role_id = 2 AND resource_id = ? LIMIT 1
    `, [resourceId]);
    
    if (loggedInRole.length) {
      const currentValue = loggedInRole[0].value;
      if ((currentValue & viewValue) === 0) {
        // Permission not present, add it
        await db.execute(`
          UPDATE bm_web_role_resources 
          SET value = value | ? 
          WHERE role_id = 2 AND resource_id = ?
        `, [viewValue, resourceId]);
        console.log('✅ Added kick view permission to Logged In role');
      } else {
        console.log('✅ Logged In role already has kick view permission');
      }
    } else {
      // Create role resource entry
      await db.execute(`
        INSERT INTO bm_web_role_resources (role_id, resource_id, value) 
        VALUES (2, ?, ?)
      `, [resourceId, viewValue]);
      console.log('✅ Created kick view permission for Logged In role');
    }

    console.log('🎉 Kick view permissions fixed! Kicks should now be visible to all users.');

  } catch (error) {
    console.error('❌ Error fixing kick permissions:', error);
  } finally {
    await db.end();
  }
}

// Run the fix
fixKickViewPermissions().catch(console.error); 