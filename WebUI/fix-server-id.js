#!/usr/bin/env node

// Load environment variables from .env file
require('dotenv').config();

const mysql = require('mysql2/promise');

console.log('🔧 Fixing BanManager Server ID Column');
console.log('===================================');

async function fixServerIdColumn() {
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

        // Check current server_id column type
        console.log('🔍 Checking current server_id column...');
        const [columns] = await db.execute(`
            SELECT COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'bm_pending_commands' AND COLUMN_NAME = 'server_id'
        `, [dbConfig.database]);

        if (columns.length > 0) {
            console.log(`Current server_id type: ${columns[0].COLUMN_TYPE}`);
        }

        // Alter server_id to be VARCHAR to handle string IDs
        console.log('🔧 Changing server_id to VARCHAR(50)...');
        await db.execute(`
            ALTER TABLE bm_pending_commands 
            MODIFY COLUMN server_id VARCHAR(50) NOT NULL
        `);

        console.log('🎉 SUCCESS! server_id column updated to VARCHAR(50)');
        console.log('✅ Can now handle both numeric and string server IDs');
        console.log('✅ WebUI kick commands will insert properly');
        
    } catch (error) {
        console.error('❌ Fix failed:', error.message);
        console.error('💡 The column may already be the correct type');
    } finally {
        if (db) {
            await db.end();
        }
    }
}

fixServerIdColumn().then(() => {
    console.log('🔧 Server ID column fix completed');
    process.exit(0);
}).catch((error) => {
    console.error('💥 Fix failed:', error.message);
    process.exit(1);
}); 