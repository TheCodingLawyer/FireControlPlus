const mysql = require('mysql2/promise');

(async () => {
  const connection = await mysql.createConnection({
    host: process.env.DB_HOST || 'trolley.proxy.rlwy.net',
    port: process.env.DB_PORT || 10457,
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || 'MadfCAylCMfHnhzdaHtRSsqsPkKokuOh',
    database: process.env.DB_NAME || 'railway'
  });

  console.log('🔌 Connected to database');
  console.log('📝 Adding updated column to bm_web_users...');

  // Add the updated column with default value
  const currentTimestamp = Math.floor(Date.now() / 1000);
  await connection.execute(`
    ALTER TABLE bm_web_users 
    ADD COLUMN \`updated\` int(10) unsigned NOT NULL DEFAULT ${currentTimestamp}
  `);
  console.log('✅ Updated column added!');

  // Also make email nullable (from migration 20200418163339)
  await connection.execute(`
    ALTER TABLE bm_web_users 
    MODIFY COLUMN \`email\` varchar(255) DEFAULT NULL
  `);
  console.log('✅ Email column set to nullable!');

  await connection.end();
  console.log('\n🎉 bm_web_users table fixed! Try logging in again.');
})();

