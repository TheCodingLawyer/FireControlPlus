-- BanManager WebUI Database Schema
-- Run this in your Railway MySQL database

CREATE TABLE IF NOT EXISTS `bm_web_servers` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `host` varchar(255) NOT NULL,
  `port` int(10) unsigned NOT NULL DEFAULT '3306',
  `database` varchar(255) NOT NULL,
  `user` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL DEFAULT '',
  `console` varbinary(16) NOT NULL,
  `tables` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bm_web_servers_name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_roles` (
  `role_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(30) NOT NULL,
  `parent_role_id` INT UNSIGNED NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_player_roles` (
  `player_id` BINARY(16) NOT NULL,
  `role_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`player_id`, `role_id`),
  KEY bm_web_player_roles_player_id (`player_id`),
  KEY bm_web_player_roles_role_id (`role_id`),
  CONSTRAINT `bm_web_player_roles_role_id_fk` FOREIGN KEY (`role_id`) REFERENCES `bm_web_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_player_server_roles` (
  `player_id` BINARY(16) NOT NULL,
  `server_id` VARCHAR(255) NOT NULL,
  `role_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`player_id`, `server_id`, `role_id`),
  KEY bm_web_player_server_roles_player_id (`player_id`),
  KEY bm_web_player_server_roles_role_id (`role_id`),
  KEY bm_web_player_server_roles_server_id (`server_id`),
  CONSTRAINT `bm_web_player_server_roles_role_id_fk` FOREIGN KEY (`role_id`) REFERENCES `bm_web_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `bm_web_player_server_roles_server_id_fk` FOREIGN KEY (`server_id`) REFERENCES `bm_web_servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_resources` (
  `resource_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`resource_id`),
  UNIQUE KEY `bm_web_resources_name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_resource_permissions` (
  `permission_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id` INT UNSIGNED NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` int(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (`permission_id`),
  KEY `bm_web_resource_permissions_resource_id_index` (`resource_id`),
  CONSTRAINT `bm_web_resource_permissions_resource_id_fk` FOREIGN KEY (`resource_id`) REFERENCES `bm_web_resources` (`resource_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_role_resources` (
  `role_id` INT UNSIGNED NOT NULL,
  `resource_id` INT UNSIGNED NOT NULL,
  `value` int(11) UNSIGNED DEFAULT NULL,
  KEY `bm_web_group_resources_group_id_index` (`role_id`),
  KEY `bm_web_group_resources_resource_id_index` (`resource_id`),
  KEY `bm_web_group_resources_value_index` (`value`),
  KEY `bm_web_group_resources_group_id_resource_id_index` (`role_id`,`resource_id`),
  CONSTRAINT `bm_web_role_resources_resource_id_fk` FOREIGN KEY (`resource_id`) REFERENCES `bm_web_resources` (`resource_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `bm_web_role_resources_role_id_fk` FOREIGN KEY (`role_id`) REFERENCES `bm_web_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_users` (
  `player_id` binary(16) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`player_id`),
  KEY `bm_web_users_email_index` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_pin_logins` (
  `pin` int(11) NOT NULL,
  `player_id` binary(16) NOT NULL,
  `expires` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`pin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_notification_rules` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `player_id` binary(16) NOT NULL,
  `server_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `bm_web_notification_rules_player_id_index` (`player_id`),
  KEY `bm_web_notification_rules_server_id_index` (`server_id`),
  CONSTRAINT `bm_web_notification_rules_server_id_fk` FOREIGN KEY (`server_id`) REFERENCES `bm_web_servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_push_subscriptions` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` binary(16) NOT NULL,
  `endpoint` varchar(500) NOT NULL,
  `keys_p256dh` varchar(255) NOT NULL,
  `keys_auth` varchar(255) NOT NULL,
  `created` int(10) unsigned NOT NULL,
  `updated` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `bm_web_push_subscriptions_player_id_index` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_rate_limits` (
  `key` varchar(255) NOT NULL,
  `points` int(9) NOT NULL DEFAULT 0,
  `expire` BIGINT UNSIGNED,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_appeals` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `punishment_type` varchar(50) NOT NULL,
  `punishment_id` int(11) unsigned NOT NULL,
  `punishment_actor_id` binary(16) NOT NULL,
  `punishment_reason` varchar(255) NOT NULL,
  `punishment_created` bigint(20) unsigned NOT NULL,
  `punishment_expires` bigint(20) unsigned NOT NULL,
  `punishment_soft` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `punishment_silent` tinyint(1) unsigned NOT NULL DEFAULT 0,
  `actor_id` binary(16) NOT NULL,
  `assignee_id` binary(16) DEFAULT NULL,
  `reason` text NOT NULL,
  `created` bigint(20) unsigned NOT NULL,
  `updated` bigint(20) unsigned NOT NULL,
  `server_id` varchar(255) NOT NULL,
  `state_id` int(11) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `bm_web_appeals_server_id_index` (`server_id`),
  CONSTRAINT `bm_web_appeals_server_id_fk` FOREIGN KEY (`server_id`) REFERENCES `bm_web_servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `bm_web_appeal_comments` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `appeal_id` int(11) unsigned NOT NULL,
  `actor_id` binary(16) NOT NULL,
  `comment` text NOT NULL,
  `created` bigint(20) unsigned NOT NULL,
  `updated` bigint(20) unsigned NOT NULL,
  `type` varchar(50) NOT NULL DEFAULT 'comment',
  `state_id` int(11) unsigned DEFAULT NULL,
  `acl` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `bm_web_appeal_comments_appeal_id_index` (`appeal_id`),
  CONSTRAINT `bm_web_appeal_comments_appeal_id_fk` FOREIGN KEY (`appeal_id`) REFERENCES `bm_web_appeals` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

