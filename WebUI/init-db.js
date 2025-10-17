#!/usr/bin/env node
require('dotenv').config()
const mysql = require('mysql2/promise')

const dbConfig = {
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
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
`

;(async () => {
  try {
    console.log('Initializing database schema...')
    const connection = await mysql.createConnection(dbConfig)
    await connection.query(initSQL)
    await connection.end()
    console.log('✅ Database schema initialized successfully')
    process.exit(0)
  } catch (error) {
    console.error('❌ Database initialization failed:', error.message)
    process.exit(1)
  }
})()

