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
  console.log('🗑️  Dropping old bm_web_rate_limits table...');

  await connection.execute(`DROP TABLE IF EXISTS bm_web_rate_limits`);
  console.log('✅ Old table dropped!');

  console.log('📝 Creating new bm_web_rate_limits table...');
  await connection.execute(`
    CREATE TABLE bm_web_rate_limits (
      \`key\` varchar(255) NOT NULL,
      \`points\` int(9) NOT NULL DEFAULT 0,
      \`expire\` BIGINT UNSIGNED,
      PRIMARY KEY (\`key\`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8
  `);
  console.log('✅ New table created with correct schema!');

  await connection.end();
  console.log('\n🎉 Rate limits table fixed! Try logging in again.');
})();

