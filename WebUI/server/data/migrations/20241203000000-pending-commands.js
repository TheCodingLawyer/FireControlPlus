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
        type: DataTypes.INTEGER,
        allowNull: false
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
      created: {
        allowNull: false,
        type: DataTypes.INTEGER
      }
    })

    await queryInterface.addIndex('bm_pending_commands', ['server_id', 'command'])
  },

  async down (queryInterface, Sequelize) {
    await queryInterface.dropTable('bm_pending_commands')
  }
} 