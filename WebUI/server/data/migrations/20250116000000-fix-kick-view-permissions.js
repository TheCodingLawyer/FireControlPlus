const aclHelper = require('./lib/acl')

exports.setup = function () {}

exports.up = async function (db) {
  const { attachPermission } = aclHelper(db)

  // Give Guest (role_id 1) and Logged In (role_id 2) roles view permission for kicks
  // Similar to how bans and mutes work
  await attachPermission('player.kicks', 1, 'view')
  await attachPermission('player.kicks', 2, 'view')
}

exports.down = async function (db) {
  // Remove kick view permissions from Guest and Logged In roles
  const [resource] = await db.runSql('SELECT resource_id FROM bm_web_resources WHERE name = ? LIMIT 1', ['player.kicks'])
  if (!resource) return

  const [perm] = await db.runSql('SELECT permission_id, value FROM bm_web_resource_permissions WHERE resource_id = ? AND name = ? LIMIT 1', [resource.resource_id, 'view'])
  if (!perm) return

  // Remove from Guest role (1)
  await db.runSql('UPDATE bm_web_role_resources SET value = value - ? WHERE role_id = ? AND resource_id = ? AND (value & ?) != 0', [perm.value, 1, resource.resource_id, perm.value])
  
  // Remove from Logged In role (2)
  await db.runSql('UPDATE bm_web_role_resources SET value = value - ? WHERE role_id = ? AND resource_id = ? AND (value & ?) != 0', [perm.value, 2, resource.resource_id, perm.value])
}

exports._meta = { version: 1 } 