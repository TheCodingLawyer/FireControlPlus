const mysql = require('mysql2/promise');

(async () => {
  const connection = await mysql.createConnection({
    host: process.env.DB_HOST || 'trolley.proxy.rlwy.net',
    port: process.env.DB_PORT || 10457,
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || 'MadfCAylCMfHnhzdaHtRSsqsPkKokuOh',
    database: process.env.DB_NAME || 'railway'
  });

  console.log('🔌 Connected to database\n');

  // Define all resources and their permissions
  const resources = [
    { name: 'servers', permissions: ['view', 'manage'] },
    { name: 'players', permissions: ['view', 'ip'] },
    { name: 'player.alts', permissions: ['view'] },
    { name: 'player.bans', permissions: ['create', 'update.any', 'update.own', 'delete.any', 'delete.own', 'view'] },
    { name: 'player.ips', permissions: ['view'] },
    { name: 'player.history', permissions: ['view'] },
    { name: 'player.kicks', permissions: ['create', 'update.any', 'update.own', 'delete.any', 'delete.own', 'view'] },
    { name: 'player.mutes', permissions: ['create', 'update.any', 'update.own', 'delete.any', 'delete.own', 'view'] },
    { name: 'player.notes', permissions: ['create', 'update.any', 'update.own', 'delete.any', 'delete.own', 'view'] },
    { name: 'player.warnings', permissions: ['create', 'update.any', 'update.own', 'delete.any', 'delete.own', 'view'] },
    { name: 'player.reports', permissions: ['create', 'update.state', 'update.assignee', 'view', 'comment', 'comment.delete.any', 'comment.delete.own'] },
    { name: 'player.appeals', permissions: ['view'] }
  ];

  // Get Admin role ID
  const [roles] = await connection.execute(`SELECT role_id FROM bm_web_roles WHERE name = 'Admin'`);
  const adminRoleId = roles[0].role_id;
  console.log(`✅ Found Admin role (ID: ${adminRoleId})\n`);

  for (const resource of resources) {
    console.log(`📝 Processing resource: ${resource.name}`);
    
    // Insert resource if it doesn't exist
    const [existing] = await connection.execute(
      `SELECT resource_id FROM bm_web_resources WHERE name = ?`,
      [resource.name]
    );
    
    let resourceId;
    if (existing.length === 0) {
      const [result] = await connection.execute(
        `INSERT INTO bm_web_resources (name) VALUES (?)`,
        [resource.name]
      );
      resourceId = result.insertId;
      console.log(`   ✅ Created resource (ID: ${resourceId})`);
      
      // Insert role_resource entry for Admin with value 0 (will be updated below)
      await connection.execute(
        `INSERT INTO bm_web_role_resources (role_id, resource_id, value) VALUES (?, ?, 0)`,
        [adminRoleId, resourceId]
      );
    } else {
      resourceId = existing[0].resource_id;
      console.log(`   ℹ️  Resource already exists (ID: ${resourceId})`);
    }
    
    // Add permissions and calculate total permission value
    let totalValue = 0;
    for (let i = 0; i < resource.permissions.length; i++) {
      const permissionName = resource.permissions[i];
      const permissionValue = Math.pow(2, i);
      
      // Insert permission if it doesn't exist
      await connection.execute(
        `INSERT IGNORE INTO bm_web_resource_permissions (resource_id, name, value) VALUES (?, ?, ?)`,
        [resourceId, permissionName, permissionValue]
      );
      
      totalValue += permissionValue;
    }
    
    // Grant all permissions to Admin role
    await connection.execute(
      `INSERT INTO bm_web_role_resources (role_id, resource_id, value) VALUES (?, ?, ?)
       ON DUPLICATE KEY UPDATE value = ?`,
      [adminRoleId, resourceId, totalValue, totalValue]
    );
    
    console.log(`   🔑 Granted all permissions to Admin (value: ${totalValue})`);
  }

  await connection.end();
  console.log('\n🎉 All resources and permissions initialized!');
  console.log('✅ Admin role now has full access to the dashboard!');
})();

