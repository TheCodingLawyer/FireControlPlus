#!/usr/bin/env node

// Load environment variables from .env file
require('dotenv').config();

const mysql = require('mysql2/promise');

console.log('🚀 BanManager Kick Table Migration');
console.log('==================================');

async function runMigration() {
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

        // Check if table exists
        const [tables] = await db.execute("SHOW TABLES LIKE 'bm_pending_commands'");
        
        if (tables.length > 0) {
            console.log('✅ Table bm_pending_commands already exists - kick system ready!');
            return;
        }

        console.log('📋 Creating bm_pending_commands table...');
        
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        console.log('🎉 SUCCESS! Table bm_pending_commands created!');
        console.log('✅ WebUI kick buttons will now work instantly');
        console.log('✅ MC server "command queue" errors will stop');
        
    } catch (error) {
        console.error('❌ Migration failed:', error.message);
        
        if (error.message.includes('Access denied')) {
            console.error('💡 Fix: Check your database credentials in environment variables');
        } else if (error.message.includes('connect')) {
            console.error('💡 Fix: Check if database server is running and accessible');
        } else if (error.message.includes('Unknown database')) {
            console.error('💡 Fix: Make sure the database name is correct');
        } else if (error.code === 'MODULE_NOT_FOUND') {
            console.error('💡 Fix: mysql2 module not installed yet');
        }
        
        console.error('\n📋 Manual fix - run this SQL in your database:');
        console.error(`
CREATE TABLE bm_pending_commands (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_id INT NOT NULL,
    command VARCHAR(50) NOT NULL,
    player_id CHAR(36) NOT NULL,
    actor_id CHAR(36) NOT NULL,
    args TEXT,
    created BIGINT NOT NULL,
    processed TINYINT(1) DEFAULT 0,
    INDEX idx_server_command (server_id, command),
    INDEX idx_created (created)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        `);
    } finally {
        if (db) {
            await db.end();
        }
    }
}

runMigration().then(() => {
    console.log('🔧 Migration completed');
    process.exit(0);
}).catch((error) => {
    console.error('💥 Migration error:', error.message);
    process.exit(0); // Don't fail startup
}); 