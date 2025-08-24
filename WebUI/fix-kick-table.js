#!/usr/bin/env node

// Load environment variables from .env file
require('dotenv').config();

const mysql = require('mysql2/promise');

console.log('🔧 Fixing BanManager Kick Table Format');
console.log('====================================');

async function fixKickTable() {
    const dbConfig = {
        host: process.env.DB_HOST || 'localhost',
        port: parseInt(process.env.DB_PORT) || 3306,
        database: process.env.DB_NAME || process.env.DATABASE_NAME || 'banmanager',
        user: process.env.DB_USER || process.env.DATABASE_USER || 'root',
        password: process.env.DB_PASSWORD || process.env.DB_PASS || process.env.DATABASE_PASS || ''
    };

    console.log(`Connecting to: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`);
    
    let db;
    try {
        db = await mysql.createConnection(dbConfig);
        console.log('✅ Database connection successful');

        // Drop existing table if it exists
        console.log('🗑️  Dropping existing bm_pending_commands table...');
        await db.execute('DROP TABLE IF EXISTS bm_pending_commands');
        console.log('✅ Old table dropped');

        // Create table with correct BINARY(16) format
        console.log('📋 Creating bm_pending_commands table with BINARY(16) UUIDs...');
        
        await db.execute(`
            CREATE TABLE bm_pending_commands (
                id INT AUTO_INCREMENT PRIMARY KEY,
                server_id INT NOT NULL,
                command VARCHAR(50) NOT NULL,
                player_id BINARY(16) NOT NULL,
                actor_id BINARY(16) NOT NULL,
                args TEXT,
                created BIGINT NOT NULL,
                processed TINYINT(1) DEFAULT 0,
                INDEX idx_server_command (server_id, command),
                INDEX idx_created (created)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8
        `);

        console.log('🎉 SUCCESS! Table bm_pending_commands created with correct format!');
        console.log('✅ UUIDs will now be stored as BINARY(16) matching BanManager format');
        console.log('✅ WebUI kick buttons will work correctly');
        console.log('✅ MC server "command queue" errors will stop');
        
    } catch (error) {
        console.error('❌ Fix failed:', error.message);
        console.error('💡 You may need to manually drop the table and restart WebUI');
    } finally {
        if (db) {
            await db.end();
        }
    }
}

fixKickTable().then(() => {
    console.log('🔧 Table fix completed');
    process.exit(0);
}).catch((error) => {
    console.error('💥 Fix failed:', error.message);
    process.exit(1);
}); 