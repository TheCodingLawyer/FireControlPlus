const { parseResolveInfo, simplifyParsedResolveInfoFragmentWithType } = require('graphql-parse-resolve-info')
const { getReportsProvider } = require('../../data/reports/provider')

module.exports = {
  Server: {
    stats: {
      async resolve (obj, args, { state: { serversPool }, log }, info) {
        const parsedResolveInfoFragment = parseResolveInfo(info)
        const { fields } = simplifyParsedResolveInfoFragmentWithType(parsedResolveInfoFragment, info.returnType)

        const id = obj.id || args.id
        const server = serversPool.get(id)
        const stats = {
          totalActiveBans: 0,
          totalActiveMutes: 0,
          totalReports: 0,
          totalWarnings: 0,
          totalKicks: 0,
          totalPlayers: 0
        }

        if (fields?.totalActiveBans) {
          try {
            const { totalActiveBans } = await server.pool(server.config.tables.playerBans)
              .select(server.pool.raw('COUNT(*) AS `totalActiveBans`'))
              .first()

            stats.totalActiveBans = totalActiveBans
          } catch (e) {
            log.error(e, `failed to retrieve total active bans for server ${server.config.name}`)
          }
        }

        if (fields?.totalActiveMutes) {
          try {
            const { totalActiveMutes } = await server.pool(server.config.tables.playerMutes)
              .select(server.pool.raw('COUNT(*) AS `totalActiveMutes`'))
              .first()

            stats.totalActiveMutes = totalActiveMutes
          } catch (e) {
            log.error(e, `failed to retrieve total active mutes for server ${server.config.name}`)
          }
        }

        if (fields?.totalReports) {
          try {
            const provider = await getReportsProvider(server)
            if (provider === 'tigerreports') {
              const { totalReports } = await server.pool('tigerreports_reports')
                .where('archived', 0)
                .select(server.pool.raw('COUNT(report_id) AS `totalReports`'))
                .first()
              stats.totalReports = totalReports
            } else {
              const { totalReports } = await server.pool(server.config.tables.playerReports)
                .select(server.pool.raw('COUNT(*) AS `totalReports`'))
                .first()
              stats.totalReports = totalReports
            }
          } catch (e) {
            log.error(e, `failed to retrieve total reports for server ${server.config.name}`)
          }
        }

        if (fields?.totalWarnings) {
          try {
            const { totalWarnings } = await server.pool(server.config.tables.playerWarnings)
              .select(server.pool.raw('COUNT(*) AS `totalWarnings`'))
              .first()

            stats.totalWarnings = totalWarnings
          } catch (e) {
            log.error(e, `failed to retrieve total warnings for server ${server.config.name}`)
          }
        }

        if (fields?.totalKicks) {
          try {
            const { totalKicks } = await server.pool(server.config.tables.playerKicks)
              .select(server.pool.raw('COUNT(*) AS `totalKicks`'))
              .first()

            stats.totalKicks = totalKicks
          } catch (e) {
            log.error(e, `failed to retrieve total kicks for server ${server.config.name}`)
          }
        }

        if (fields?.totalPlayers) {
          try {
            const { totalPlayers } = await server.pool(server.config.tables.players)
              .select(server.pool.raw('COUNT(*) AS `totalPlayers`'))
              .first()

            stats.totalPlayers = totalPlayers
          } catch (e) {
            log.error(e, `failed to retrieve total players for server ${server.config.name}`)
          }
        }

        return stats
      }
    }
  }
}
