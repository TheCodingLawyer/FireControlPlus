#!/usr/bin/env node

/**
 * Script to add playerChatHistory table to existing WebUI server configurations
 * Run this after updating BanManager to add message history support
 */

const mysql = require('mysql2/promise');

console.log('🔧 BanManager WebUI - Add Chat History Table');
console.log('==========================================');
console.log('This script will update your WebUI server configurations');
console.log('to include the new playerChatHistory table.\n');

async function updateServerConfigs() {
    const dbConfig = {
        host: process.env.DB_HOST || process.env.DATABASE_HOST || '94.231.215.249',
        port: parseInt(process.env.DB_PORT || process.env.DATABASE_PORT) || 3306,
        database: process.env.DB_NAME || process.env.DATABASE_NAME || 's19_BanManager',
        user: process.env.DB_USER || process.env.DATABASE_USER || 'u19_j6VEaTQ4tz',
        password: process.env.DB_PASSWORD || process.env.DB_PASS || process.env.DATABASE_PASS || '@L.NXjiJc506Y5S8xHX.33NU'
    };

    console.log(`🔗 Connecting to: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`);
    
    let db;
    try {
        db = await mysql.createConnection(dbConfig);
        console.log('✅ Database connection successful\n');

        // Get all servers
        const [servers] = await db.execute("SELECT id, name, tables FROM bm_web_servers");
        
        if (servers.length === 0) {
            console.log('ℹ️  No servers found in WebUI database');
            return;
        }

        console.log(`📋 Found ${servers.length} server(s) to update:\n`);

        let updatedCount = 0;
        
        for (const server of servers) {
            console.log(`🖥️  Processing server: ${server.name} (${server.id})`);
            
            let tables;
            try {
                tables = JSON.parse(server.tables);
            } catch (e) {
                console.log(`   ❌ Error parsing tables JSON for ${server.name}: ${e.message}`);
                continue;
            }

            // Check if playerChatHistory already exists
            if (tables.playerChatHistory) {
                console.log(`   ✅ playerChatHistory already configured: ${tables.playerChatHistory}`);
                continue;
            }

            // Add the new table
            tables.playerChatHistory = 'bm_player_chat_history';
            
            // Update the server
            const updatedTables = JSON.stringify(tables);
            await db.execute(
                "UPDATE bm_web_servers SET tables = ? WHERE id = ?",
                [updatedTables, server.id]
            );
            
            console.log(`   ✅ Added playerChatHistory table: bm_player_chat_history`);
            updatedCount++;
        }

        console.log(`\n🎉 SUCCESS! Updated ${updatedCount} server configuration(s)`);
        
        if (updatedCount > 0) {
            console.log('\n📝 Next steps:');
            console.log('   1. Restart your WebUI server to pick up the changes');
            console.log('   2. Make sure your Minecraft server is running the latest BanManager');
            console.log('   3. Have players chat in-game to generate message history');
            console.log('   4. Check the WebUI - message history should now work!');
        }
        
    } catch (error) {
        console.error('❌ Error:', error.message);
        console.error('\n🔍 Troubleshooting:');
        console.error('   1. Make sure your database connection details are correct');
        console.error('   2. Ensure the WebUI database exists and has bm_web_servers table');
        console.error('   3. Check that your database user has UPDATE permissions');
        process.exit(1);
    } finally {
        if (db) {
            await db.end();
        }
    }
}

// Environment variables are optional now - we have defaults
console.log('💡 Using database configuration (set env vars to override):');
console.log(`   Host: ${process.env.DB_HOST || process.env.DATABASE_HOST || '94.231.215.249'}`);
console.log(`   Database: ${process.env.DB_NAME || process.env.DATABASE_NAME || 's19_BanManager'}`);
console.log(`   User: ${process.env.DB_USER || process.env.DATABASE_USER || 'u19_j6VEaTQ4tz'}`);
console.log('');

runUpdate().catch(console.error);

async function runUpdate() {
    await updateServerConfigs();
}
