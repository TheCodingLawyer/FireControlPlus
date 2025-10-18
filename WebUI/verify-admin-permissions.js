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

  // Check if admin user exists
  const [users] = await connection.execute(
    `SELECT HEX(player_id) as player_id, email FROM bm_web_users WHERE email = 'admin@banmanager.com'`
  );
  
  if (users.length === 0) {
    console.log('❌ Admin user not found!');
    await connection.end();
    return;
  }
  
  console.log('✅ Admin user exists:', users[0].email);
  const playerId = users[0].player_id;
  
  // Check roles
  const [roles] = await connection.execute(`SELECT role_id, name FROM bm_web_roles`);
  console.log('\n📋 Available roles:', roles);
  
  // Check player roles
  const [playerRoles] = await connection.execute(
    `SELECT pr.role_id, r.name 
     FROM bm_web_player_roles pr 
     JOIN bm_web_roles r ON pr.role_id = r.role_id 
     WHERE pr.player_id = UNHEX(?)`,
    [playerId]
  );
  console.log('\n👤 Admin user roles:', playerRoles);
  
  // Check resources
  const [resources] = await connection.execute(`SELECT resource_id, name FROM bm_web_resources`);
  console.log('\n🔐 Available resources:', resources);
  
  // Check Admin role permissions
  const adminRole = roles.find(r => r.name === 'Admin');
  if (adminRole) {
    const [permissions] = await connection.execute(
      `SELECT rr.resource_id, r.name, rr.value 
       FROM bm_web_role_resources rr 
       JOIN bm_web_resources r ON rr.resource_id = r.resource_id 
       WHERE rr.role_id = ?`,
      [adminRole.role_id]
    );
    console.log('\n🔑 Admin role permissions:', permissions);
  }
  
  await connection.end();
  console.log('\n✅ Verification complete!');
})();

