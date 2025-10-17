#!/usr/bin/env node
require('dotenv').config()
const mysql = require('mysql')

const dbConfig = {
  host: process.env.DB_HOST || 'mysql.railway.internal',
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'railway',
  multipleStatements: true
}

const initSQL = `
CREATE TABLE IF NOT EXISTS \`bm_web_servers\` (
  \`id\` varchar(255) NOT NULL,
  \`name\` varchar(255) NOT NULL,
  \`host\` varchar(255) NOT NULL,
  \`port\` int(10) unsigned NOT NULL DEFAULT '3306',
  \`database\` varchar(255) NOT NULL,
  \`user\` varchar(255) NOT NULL,
  \`password\` varchar(255) NOT NULL DEFAULT '',
  \`console\` varbinary(16) NOT NULL,
  \`tables\` text NOT NULL,
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`bm_web_servers_name_unique\` (\`name\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_roles\` (
  \`role_id\` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`name\` VARCHAR(30) NOT NULL,
  \`parent_role_id\` INT UNSIGNED NULL,
  PRIMARY KEY (\`role_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_player_roles\` (
  \`player_id\` BINARY(16) NOT NULL,
  \`role_id\` INT UNSIGNED NOT NULL,
  PRIMARY KEY (\`player_id\`, \`role_id\`),
  KEY bm_web_player_roles_player_id (\`player_id\`),
  KEY bm_web_player_roles_role_id (\`role_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_player_server_roles\` (
  \`player_id\` BINARY(16) NOT NULL,
  \`server_id\` VARCHAR(255) NOT NULL,
  \`role_id\` INT UNSIGNED NOT NULL,
  PRIMARY KEY (\`player_id\`, \`server_id\`, \`role_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_resources\` (
  \`resource_id\` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`name\` varchar(255) NOT NULL,
  PRIMARY KEY (\`resource_id\`),
  UNIQUE KEY \`bm_web_resources_name_unique\` (\`name\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_resource_permissions\` (
  \`permission_id\` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`resource_id\` INT UNSIGNED NOT NULL,
  \`name\` varchar(255) NOT NULL,
  \`value\` int(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (\`permission_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_role_resources\` (
  \`role_id\` INT UNSIGNED NOT NULL,
  \`resource_id\` INT UNSIGNED NOT NULL,
  \`value\` int(11) UNSIGNED DEFAULT NULL,
  KEY \`bm_web_group_resources_group_id_index\` (\`role_id\`),
  KEY \`bm_web_group_resources_resource_id_index\` (\`resource_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS \`bm_web_users\` (
  \`player_id\` binary(16) NOT NULL,
  \`email\` varchar(255) NOT NULL,
  \`password\` varchar(255) DEFAULT NULL,
  PRIMARY KEY (\`player_id\`),
  KEY \`bm_web_users_email_index\` (\`email\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
`;

console.log('🚀 Initializing BanManager WebUI database schema...')
console.log(`📍 Connecting to: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`)

const connection = mysql.createConnection(dbConfig)

connection.connect((err) => {
  if (err) {
    console.error('❌ Database connection failed:', err.message)
    process.exit(1)
  }
  
  console.log('✅ Connected to database')
  
  connection.query(initSQL, (error, results) => {
    if (error) {
      console.error('❌ Schema initialization failed:', error.message)
      connection.end()
      process.exit(1)
    }
    
    console.log('✅ Database schema initialized successfully!')
    console.log('📋 Tables ready:', results.length, 'statements executed')
    
    connection.end()
    console.log('🎉 Initialization complete - starting server...')
  })
})
