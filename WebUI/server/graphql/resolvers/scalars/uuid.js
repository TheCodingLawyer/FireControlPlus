const { parse, unparse } = require('uuid-parse')
const { GraphQLScalarType } = require('graphql')
const { Kind } = require('graphql/language')
const ExposedErrpr = require('../../../data/exposed-error')

const HEX_UUID_REGEX = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
const HEX_UUID_COMPACT_REGEX = /^[0-9a-fA-F]{32}$/

function normalizeUuidString (value) {
  if (typeof value !== 'string') return value
  if (HEX_UUID_REGEX.test(value)) return value
  if (HEX_UUID_COMPACT_REGEX.test(value)) {
    return value.replace(/(.{8})(.{4})(.{4})(.{4})(.{12})/, '$1-$2-$3-$4-$5')
  }
  return value
}

module.exports = new GraphQLScalarType(
  {
    name: 'UUID',
    serialize: value => {
      if (!Buffer.isBuffer(value)) return value

      return unparse(value)
    },
    parseValue: value => {
      const normalized = normalizeUuidString(value)
      if (!HEX_UUID_REGEX.test(normalized)) {
        throw new ExposedErrpr('Type Error: Invalid UUID')
      }

      return parse(normalized, Buffer.alloc(16))
    },
    parseLiteral: ast => {
      if (ast.kind === Kind.STRING) {
        const normalized = normalizeUuidString(ast.value)
        if (HEX_UUID_REGEX.test(normalized)) {
          return parse(normalized, Buffer.alloc(16))
        }
      }

      return null
    }
  })
