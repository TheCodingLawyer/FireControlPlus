#!/usr/bin/env node

// Load environment variables from .env file
require('dotenv').config();

console.log('🔍 BanManager Migration Debug Test');
console.log('=================================');

// Test 1: Check if mysql2 is available
console.log('Test 1: Checking mysql2 module...');
try {
    const mysql = require('mysql2/promise');
    console.log('✅ mysql2/promise module found');
} catch (error) {
    console.log('❌ mysql2/promise module missing:', error.message);
    process.exit(1);
}

// Test 2: Show environment variables
console.log('\nTest 2: Environment variables...');
console.log('DB_HOST:', process.env.DB_HOST || 'NOT SET');
console.log('DB_PORT:', process.env.DB_PORT || 'NOT SET');
console.log('DB_NAME:', process.env.DB_NAME || 'NOT SET');
console.log('DATABASE_NAME:', process.env.DATABASE_NAME || 'NOT SET');
console.log('DB_USER:', process.env.DB_USER || 'NOT SET');
console.log('DATABASE_USER:', process.env.DATABASE_USER || 'NOT SET');
console.log('DB_PASSWORD:', process.env.DB_PASSWORD ? '***SET***' : 'NOT SET');
console.log('DB_PASS:', process.env.DB_PASS ? '***SET***' : 'NOT SET');
console.log('DATABASE_PASS:', process.env.DATABASE_PASS ? '***SET***' : 'NOT SET');

// Test 3: Try database connection
console.log('\nTest 3: Testing database connection...');

async function testConnection() {
    const mysql = require('mysql2/promise');
    
    const dbConfig = {
        host: process.env.DB_HOST || 'localhost',
        port: parseInt(process.env.DB_PORT) || 3306,
        database: process.env.DB_NAME || process.env.DATABASE_NAME || 'banmanager',
        user: process.env.DB_USER || process.env.DATABASE_USER || 'root',
        password: process.env.DB_PASSWORD || process.env.DB_PASS || process.env.DATABASE_PASS || ''
    };

    console.log('Attempting connection with config:');
    console.log(`  Host: ${dbConfig.host}`);
    console.log(`  Port: ${dbConfig.port}`);
    console.log(`  Database: ${dbConfig.database}`);
    console.log(`  User: ${dbConfig.user}`);
    console.log(`  Password: ${dbConfig.password ? '***SET***' : 'EMPTY'}`);

    let db;
    try {
        db = await mysql.createConnection(dbConfig);
        console.log('✅ Database connection successful!');

        // Test query
        const [result] = await db.execute('SELECT 1 as test');
        console.log('✅ Test query successful:', result);

        // Check if table exists
        const [tables] = await db.execute("SHOW TABLES LIKE 'bm_pending_commands'");
        console.log(`Table check: ${tables.length > 0 ? 'EXISTS' : 'MISSING'}`);

        // Try to create table
        if (tables.length === 0) {
            console.log('Attempting to create table...');
            await db.execute(`
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            `);
            console.log('🎉 Table created successfully!');
        } else {
            console.log('✅ Table already exists - kick system ready!');
        }

    } catch (error) {
        console.log('❌ Database error:', error.message);
        console.log('Error code:', error.code);
        console.log('Error details:', error);
    } finally {
        if (db) {
            await db.end();
        }
    }
}

testConnection().then(() => {
    console.log('\n🔧 Debug test completed');
    process.exit(0);
}).catch((error) => {
    console.error('\n💥 Debug test failed:', error.message);
    process.exit(1);
}); 