'use strict'

const { DataTypes } = require('sequelize')

module.exports = {
  async up (queryInterface, Sequelize) {
    await queryInterface.createTable('bm_pending_commands', {
      id: {
        allowNull: false,
        autoIncrement: true,
        primaryKey: true,
        type: DataTypes.INTEGER
      },
      server_id: {
        type: DataTypes.STRING(50),
        allowNull: false,
        defaultValue: 'ALL'
      },
      command: {
        type: DataTypes.STRING(50),
        allowNull: false
      },
      player_id: {
        type: 'BINARY(16)',
        allowNull: false
      },
      actor_id: {
        type: 'BINARY(16)',
        allowNull: false
      },
      args: {
        type: DataTypes.TEXT,
        allowNull: true
      },
      processed: {
        type: DataTypes.BOOLEAN,
        allowNull: false,
        defaultValue: false
      },
      created: {
        allowNull: false,
        type: DataTypes.BIGINT
      }
    })

    await queryInterface.addIndex('bm_pending_commands', ['server_id', 'command'])
    await queryInterface.addIndex('bm_pending_commands', ['processed'])
  },

  async down (queryInterface, Sequelize) {
    await queryInterface.dropTable('bm_pending_commands')
  }
} 