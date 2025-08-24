const aclHelper = require('./lib/acl')

exports.setup = function () {}

exports.up = async function (db) {
  const { addPermission, attachPermission } = aclHelper(db)

  // Ensure player.kicks has a create permission and give it to Admin role (3)
  await addPermission('player.kicks', 'create')
  await attachPermission('player.kicks', 3, 'create')
}

exports.down = async function (db) {
  // Best-effort rollback: remove the specific permission row and subtract from admin bitmask
  const [resource] = await db.runSql('SELECT resource_id FROM bm_web_resources WHERE name = ? LIMIT 1', ['player.kicks'])
  if (!resource) return

  const [perm] = await db.runSql('SELECT permission_id, value FROM bm_web_resource_permissions WHERE resource_id = ? AND name = ? LIMIT 1', [resource.resource_id, 'create'])
  if (!perm) return

  await db.runSql('UPDATE bm_web_role_resources SET value = value - ? WHERE role_id = ? AND resource_id = ?', [perm.value, 3, resource.resource_id])
  await db.runSql('DELETE FROM bm_web_resource_permissions WHERE permission_id = ?', [perm.permission_id])
}

exports._meta = { version: 1 } 