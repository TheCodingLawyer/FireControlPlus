// Mock GraphQL API for Frontend Demo
// This returns demo data so the frontend can run without a real backend

const DEMO_PLAYERS = [
  { id: '069a79f4-44e9-4726-a5be-fca90e38aaf5', name: 'Notch' },
  { id: 'f84c6a79-0a4e-45e0-879b-cd49ebd4c4e2', name: 'jeb_' },
  { id: 'c06f8906-4c8a-4911-9c29-ea1dbd1aab82', name: 'Dinnerbone' },
  { id: 'e6b5c088-0680-44df-9e1b-9bf11792291b', name: 'xXHacker420Xx' },
  { id: '1a2b3c4d-5e6f-7890-abcd-ef1234567890', name: 'GrieferKing' },
  { id: 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', name: 'SpamBot99' },
  { id: '12345678-1234-1234-1234-123456789012', name: 'CoolBuilder' },
  { id: '87654321-4321-4321-4321-210987654321', name: 'ProPvPer' },
]

const DEMO_SERVERS = [
  { id: 'server-1', name: 'Survival', ip: 'play.example.com', port: 25565 },
  { id: 'server-2', name: 'Creative', ip: 'play.example.com', port: 25566 },
  { id: 'server-3', name: 'Factions', ip: 'play.example.com', port: 25567 },
]

const DEMO_ROLES = [
  { id: 1, name: 'Guest', parent: null },
  { id: 2, name: 'Player', parent: 1 },
  { id: 3, name: 'VIP', parent: 2 },
  { id: 4, name: 'Moderator', parent: 3 },
  { id: 5, name: 'Admin', parent: 4 },
]

const DEMO_BANS = [
  {
    id: 'ban-1',
    reason: 'Using hacks and x-ray texture pack',
    created: Math.floor(Date.now() / 1000) - 86400,
    expires: 0,
    player: DEMO_PLAYERS[3], // xXHacker420Xx - our demo user!
    actor: DEMO_PLAYERS[2],
    server: DEMO_SERVERS[0],
    acl: { yours: true, update: true, delete: true },
  },
  {
    id: 'ban-2', 
    reason: 'Griefing spawn area',
    created: Math.floor(Date.now() / 1000) - 172800,
    expires: Math.floor(Date.now() / 1000) + 604800,
    player: DEMO_PLAYERS[4],
    actor: DEMO_PLAYERS[1],
    server: DEMO_SERVERS[0],
    acl: { yours: false, update: true, delete: true },
  },
  {
    id: 'ban-3',
    reason: 'Advertising other servers',
    created: Math.floor(Date.now() / 1000) - 3600,
    expires: Math.floor(Date.now() / 1000) + 86400,
    player: DEMO_PLAYERS[5],
    actor: DEMO_PLAYERS[2],
    server: DEMO_SERVERS[1],
    acl: { yours: false, update: true, delete: true },
  },
]

const DEMO_MUTES = [
  {
    id: 'mute-1',
    reason: 'Excessive spam in chat',
    created: Math.floor(Date.now() / 1000) - 7200,
    expires: Math.floor(Date.now() / 1000) + 3600,
    player: DEMO_PLAYERS[3], // xXHacker420Xx - our demo user!
    actor: DEMO_PLAYERS[1],
    server: DEMO_SERVERS[0],
    acl: { yours: true, update: true, delete: true },
  },
  {
    id: 'mute-2',
    reason: 'Toxic behavior towards other players',
    created: Math.floor(Date.now() / 1000) - 14400,
    expires: Math.floor(Date.now() / 1000) + 43200,
    player: DEMO_PLAYERS[7],
    actor: DEMO_PLAYERS[2],
    server: DEMO_SERVERS[2],
    acl: { yours: false, update: true, delete: true },
  },
]

const DEMO_WARNINGS = [
  {
    id: 'warn-1',
    reason: 'Please read the server rules',
    created: Math.floor(Date.now() / 1000) - 86400,
    player: DEMO_PLAYERS[3], // xXHacker420Xx - our demo user!
    actor: DEMO_PLAYERS[1],
    server: DEMO_SERVERS[0],
    read: false,
    points: 1,
    acl: { yours: true, update: true, delete: true },
  },
  {
    id: 'warn-2',
    reason: 'Minor griefing - first offense',
    created: Math.floor(Date.now() / 1000) - 172800,
    player: DEMO_PLAYERS[7],
    actor: DEMO_PLAYERS[2],
    server: DEMO_SERVERS[0],
    read: true,
    points: 2,
    acl: { yours: false, update: true, delete: true },
  },
]

const DEMO_KICKS = [
  {
    id: 'kick-1',
    reason: 'AFK for too long',
    created: Math.floor(Date.now() / 1000) - 3600,
    player: DEMO_PLAYERS[6],
    actor: DEMO_PLAYERS[0],
    server: DEMO_SERVERS[0],
  },
  {
    id: 'kick-2',
    reason: 'Server restart',
    created: Math.floor(Date.now() / 1000) - 7200,
    player: DEMO_PLAYERS[7],
    actor: DEMO_PLAYERS[0],
    server: DEMO_SERVERS[1],
  },
]

const APPEAL_STATES = [
  { id: 1, name: 'Open' },
  { id: 2, name: 'Assigned' },
  { id: 3, name: 'Resolved' },
  { id: 4, name: 'Rejected' },
]

const REPORT_STATES = [
  { id: 1, name: 'Open' },
  { id: 2, name: 'Assigned' },
  { id: 3, name: 'Resolved' },
  { id: 4, name: 'Closed' },
]

const DEMO_APPEALS = [
  {
    id: 'appeal-1',
    reason: 'I was falsely banned, I never used hacks!',
    created: Math.floor(Date.now() / 1000) - 3600,
    updated: Math.floor(Date.now() / 1000) - 1800,
    actor: DEMO_PLAYERS[3],
    assignee: DEMO_PLAYERS[2],
    state: APPEAL_STATES[0],
    punishmentType: 'ban',
    punishmentId: 'ban-1',
    punishmentReason: 'Using hacks and x-ray texture pack',
    server: DEMO_SERVERS[0],
    acl: { comment: true, assign: true, state: true, delete: true },
  },
  {
    id: 'appeal-2',
    reason: 'Sorry for griefing, I will not do it again. Please unban me.',
    created: Math.floor(Date.now() / 1000) - 86400,
    updated: Math.floor(Date.now() / 1000) - 43200,
    actor: DEMO_PLAYERS[4],
    assignee: null,
    state: APPEAL_STATES[1],
    punishmentType: 'ban',
    punishmentId: 'ban-2',
    punishmentReason: 'Griefing spawn area',
    server: DEMO_SERVERS[0],
    acl: { comment: true, assign: true, state: true, delete: true },
  },
]

const DEMO_REPORTS = [
  {
    id: 'report-1',
    reason: 'This player is using fly hacks!',
    created: Math.floor(Date.now() / 1000) - 1800,
    updated: Math.floor(Date.now() / 1000) - 900,
    actor: DEMO_PLAYERS[6],
    player: DEMO_PLAYERS[3],
    assignee: DEMO_PLAYERS[2],
    state: REPORT_STATES[0],
    server: DEMO_SERVERS[0],
    acl: { comment: true, assign: true, state: true, delete: true },
  },
  {
    id: 'report-2',
    reason: 'Swearing and being toxic in chat',
    created: Math.floor(Date.now() / 1000) - 7200,
    updated: Math.floor(Date.now() / 1000) - 3600,
    actor: DEMO_PLAYERS[7],
    player: DEMO_PLAYERS[5],
    assignee: null,
    state: REPORT_STATES[1],
    server: DEMO_SERVERS[0],
    acl: { comment: true, assign: true, state: true, delete: true },
  },
]

const DEMO_RESOURCES = [
  { name: 'player.bans', permissions: [{ name: 'view', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'create', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'update', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'delete', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }] },
  { name: 'player.mutes', permissions: [{ name: 'view', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'create', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'update', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'delete', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }] },
  { name: 'player.kicks', permissions: [{ name: 'view', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'create', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }] },
  { name: 'player.warnings', permissions: [{ name: 'view', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'create', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'update', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'delete', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }] },
  { name: 'player.notes', permissions: [{ name: 'view', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'create', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'update', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'delete', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }] },
  { name: 'player.reports', permissions: [{ name: 'view', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'create', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'update', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }, { name: 'delete', allowed: true, serversAllowed: ['server-1', 'server-2', 'server-3'] }] },
  { name: 'player.appeals', permissions: [{ name: 'view', allowed: true, serversAllowed: [] }, { name: 'create', allowed: true, serversAllowed: [] }, { name: 'update', allowed: true, serversAllowed: [] }, { name: 'delete', allowed: true, serversAllowed: [] }] },
  { name: 'servers', permissions: [{ name: 'manage', allowed: true, serversAllowed: [] }] },
  { name: 'roles', permissions: [{ name: 'manage', allowed: true, serversAllowed: [] }] },
  { name: 'notification.rules', permissions: [{ name: 'manage', allowed: true, serversAllowed: [] }] },
  { name: 'webhooks', permissions: [{ name: 'manage', allowed: true, serversAllowed: [] }] },
]

// Demo user - this is the "logged in" player for appeal demo
// Using xXHacker420Xx as the demo user so they have punishments to appeal
const DEMO_USER = {
  id: 'e6b5c088-0680-44df-9e1b-9bf11792291b', // xXHacker420Xx - has a ban!
  name: 'xXHacker420Xx',
  email: 'demo@example.com',
  hasAccount: true,
  session: { type: 'pin' },
  resources: DEMO_RESOURCES,
}

const NOTIFICATION_TYPES = [
  { name: 'appealCreated', description: 'Appeal Created' },
  { name: 'appealComment', description: 'Appeal Comment' },
  { name: 'appealStateChange', description: 'Appeal State Change' },
  { name: 'reportCreated', description: 'Report Created' },
  { name: 'reportComment', description: 'Report Comment' },
  { name: 'reportStateChange', description: 'Report State Change' },
]

const WEBHOOK_TYPES = [
  { name: 'APPEAL_CREATED', description: 'Appeal Created' },
  { name: 'APPEAL_UPDATED', description: 'Appeal Updated' },
  { name: 'REPORT_CREATED', description: 'Report Created' },
  { name: 'REPORT_UPDATED', description: 'Report Updated' },
  { name: 'BAN_CREATED', description: 'Ban Created' },
  { name: 'MUTE_CREATED', description: 'Mute Created' },
]

// Handle GraphQL queries
function handleQuery(query, variables) {
  // Statistics
  if (query.includes('statistics')) {
    return {
      statistics: {
        totalActiveBans: DEMO_BANS.length,
        totalActiveMutes: DEMO_MUTES.length,
        totalKicks: DEMO_KICKS.length + 156,
        totalWarnings: DEMO_WARNINGS.length + 89,
      }
    }
  }

  // Current user (me)
  if (query.includes('{ me') || query.includes('me {')) {
    return { me: DEMO_USER }
  }

  // Search players
  if (query.includes('searchPlayers')) {
    const name = variables?.name?.toLowerCase() || ''
    const limit = variables?.limit || 10
    const filtered = name
      ? DEMO_PLAYERS.filter(p => p.name.toLowerCase().includes(name))
      : DEMO_PLAYERS
    return {
      searchPlayers: filtered.slice(0, limit)
    }
  }

  // Servers
  if (query.includes('servers') && !query.includes('serverId')) {
    return { servers: DEMO_SERVERS }
  }

  // Single player
  if (query.includes('player(') && variables?.id) {
    const player = DEMO_PLAYERS.find(p => p.id === variables.id) || DEMO_PLAYERS[0]
    return {
      player: {
        ...player,
        lastSeen: Math.floor(Date.now() / 1000) - 3600,
        ip: '192.168.1.100',
      }
    }
  }

  // Combined punishment query (used by PunishmentPicker)
  if (query.includes('playerBans') && query.includes('playerMutes') && query.includes('playerWarnings')) {
    const playerId = variables?.id || variables?.player
    const bans = playerId 
      ? DEMO_BANS.filter(b => b.player.id === playerId)
      : DEMO_BANS
    const mutes = playerId
      ? DEMO_MUTES.filter(m => m.player.id === playerId)
      : DEMO_MUTES
    const warnings = playerId
      ? DEMO_WARNINGS.filter(w => w.player.id === playerId)
      : DEMO_WARNINGS
    return { 
      playerBans: bans,
      playerMutes: mutes,
      playerWarnings: warnings
    }
  }

  // Player bans
  if (query.includes('playerBans')) {
    const playerId = variables?.id || variables?.player
    const bans = playerId 
      ? DEMO_BANS.filter(b => b.player.id === playerId)
      : DEMO_BANS
    return { playerBans: bans }
  }

  // Single ban
  if (query.includes('playerBan(')) {
    const ban = DEMO_BANS.find(b => b.id === variables?.id) || DEMO_BANS[0]
    return { playerBan: ban }
  }

  // Player mutes
  if (query.includes('playerMutes')) {
    const playerId = variables?.id || variables?.player
    const mutes = playerId
      ? DEMO_MUTES.filter(m => m.player.id === playerId)
      : DEMO_MUTES
    return { playerMutes: mutes }
  }

  // Single mute
  if (query.includes('playerMute(')) {
    const mute = DEMO_MUTES.find(m => m.id === variables?.id) || DEMO_MUTES[0]
    return { playerMute: mute }
  }

  // Player warnings (list)
  if (query.includes('playerWarnings(') && !query.includes('playerWarning(')) {
    const playerId = variables?.id || variables?.player
    const warnings = playerId
      ? DEMO_WARNINGS.filter(w => w.player.id === playerId)
      : DEMO_WARNINGS
    return { playerWarnings: warnings }
  }

  // Single warning
  if (query.includes('playerWarning(')) {
    const warning = DEMO_WARNINGS.find(w => w.id === variables?.id) || DEMO_WARNINGS[0]
    return { playerWarning: warning }
  }

  // Player statistics
  if (query.includes('playerStatistics')) {
    return {
      playerStatistics: {
        totalActiveBans: 1,
        totalBans: 3,
        totalActiveMutes: 0,
        totalMutes: 2,
        totalWarnings: 5,
        totalKicks: 12,
      }
    }
  }

  // Player alts
  if (query.includes('playerAlts')) {
    return {
      playerAlts: [DEMO_PLAYERS[6], DEMO_PLAYERS[7]]
    }
  }

  // List punishment records
  if (query.includes('listPlayerPunishmentRecords')) {
    const type = variables?.type
    let records = []
    if (type === 'PlayerBanRecord') records = DEMO_BANS
    else if (type === 'PlayerMuteRecord') records = DEMO_MUTES
    else if (type === 'PlayerWarning') records = DEMO_WARNINGS
    else if (type === 'PlayerNote') records = []
    return {
      listPlayerPunishmentRecords: {
        total: records.length,
        records: records,
      }
    }
  }

  // List appeals
  if (query.includes('listPlayerAppeals')) {
    return {
      listPlayerAppeals: {
        total: DEMO_APPEALS.length,
        records: DEMO_APPEALS,
      }
    }
  }

  // Single appeal
  if (query.includes('appeal(') || (query.includes('appealStates') && variables?.id)) {
    const appeal = DEMO_APPEALS.find(a => a.id === variables?.id) || DEMO_APPEALS[0]
    return {
      appeal: appeal,
      appealStates: APPEAL_STATES,
    }
  }

  // Appeal states
  if (query.includes('appealStates')) {
    return { appealStates: APPEAL_STATES }
  }

  // List reports
  if (query.includes('listPlayerReports')) {
    return {
      listPlayerReports: {
        total: DEMO_REPORTS.length,
        records: DEMO_REPORTS,
      }
    }
  }

  // Report states
  if (query.includes('reportStates')) {
    return { reportStates: REPORT_STATES }
  }

  // Single report
  if (query.includes('report(')) {
    const report = DEMO_REPORTS.find(r => r.id === variables?.id) || DEMO_REPORTS[0]
    return {
      report: report,
      reportStates: REPORT_STATES,
    }
  }

  // Roles
  if (query.includes('roles')) {
    return { 
      roles: DEMO_ROLES.map(r => ({
        ...r,
        resources: DEMO_RESOURCES,
      })),
      servers: DEMO_SERVERS,
    }
  }

  // Single role
  if (query.includes('role(')) {
    const role = DEMO_ROLES.find(r => r.id === parseInt(variables?.id)) || DEMO_ROLES[0]
    return {
      role: {
        ...role,
        resources: DEMO_RESOURCES,
      }
    }
  }

  // Resources
  if (query.includes('resources')) {
    return { resources: DEMO_RESOURCES }
  }

  // Webhooks
  if (query.includes('listWebhooks')) {
    return {
      listWebhooks: {
        total: 2,
        records: [
          { id: 'wh-1', name: 'Discord Alerts', url: 'https://discord.com/api/webhooks/...', templateType: 'discord', enabled: true },
          { id: 'wh-2', name: 'Slack Notifications', url: 'https://hooks.slack.com/...', templateType: 'slack', enabled: false },
        ]
      }
    }
  }

  // Single webhook
  if (query.includes('webhook(')) {
    return {
      webhook: { id: 'wh-1', name: 'Discord Alerts', url: 'https://discord.com/api/webhooks/...', templateType: 'discord', enabled: true, events: ['APPEAL_CREATED', 'REPORT_CREATED'] }
    }
  }

  // Webhook types
  if (query.includes('webhookTypes') || query.includes('WebhookType')) {
    return {
      webhookTypes: { enumValues: WEBHOOK_TYPES.map(w => ({ name: w.name })) },
      webhookContentTypes: { enumValues: [{ name: 'application/json' }, { name: 'application/x-www-form-urlencoded' }] }
    }
  }

  // Notification rules
  if (query.includes('listNotificationRules')) {
    return {
      listNotificationRules: {
        total: 1,
        records: [
          { id: 'nr-1', type: 'appealCreated', roles: [DEMO_ROLES[4]] }
        ]
      }
    }
  }

  // Notifications list
  if (query.includes('listNotifications')) {
    return {
      listNotifications: {
        total: 3,
        records: [
          { id: 'n-1', type: 'appealCreated', state: 'unread', created: Math.floor(Date.now() / 1000) - 1800, appeal: DEMO_APPEALS[0] },
          { id: 'n-2', type: 'reportCreated', state: 'unread', created: Math.floor(Date.now() / 1000) - 3600, report: DEMO_REPORTS[0] },
          { id: 'n-3', type: 'appealComment', state: 'read', created: Math.floor(Date.now() / 1000) - 7200, appeal: DEMO_APPEALS[1] },
        ]
      }
    }
  }

  // Notification types
  if (query.includes('notificationTypes') || query.includes('NotificationType')) {
    return {
      notificationTypes: { enumValues: NOTIFICATION_TYPES.map(n => ({ name: n.name })) }
    }
  }

  // Server tables (for admin)
  if (query.includes('serverTables')) {
    return {
      serverTables: {
        players: 'bm_players',
        playerBans: 'bm_player_bans',
        playerBanRecords: 'bm_player_ban_records',
        playerMutes: 'bm_player_mutes',
        playerMuteRecords: 'bm_player_mute_records',
        playerKicks: 'bm_player_kicks',
        playerNotes: 'bm_player_notes',
        playerHistory: 'bm_player_history',
        playerReports: 'bm_player_reports',
        playerReportLocations: 'bm_player_report_locations',
        playerReportStates: 'bm_player_report_states',
        playerReportCommands: 'bm_player_report_commands',
        playerReportComments: 'bm_player_report_comments',
        playerWarnings: 'bm_player_warnings',
        ipBans: 'bm_ip_bans',
        ipBanRecords: 'bm_ip_ban_records',
        ipMutes: 'bm_ip_mutes',
        ipMuteRecords: 'bm_ip_mute_records',
        ipRangeBans: 'bm_ip_range_bans',
        ipRangeBanRecords: 'bm_ip_range_ban_records',
        nameBans: 'bm_name_bans',
        nameBanRecords: 'bm_name_ban_records',
      }
    }
  }

  // Single server
  if (query.includes('server(')) {
    const server = DEMO_SERVERS.find(s => s.id === variables?.id) || DEMO_SERVERS[0]
    return {
      server: {
        ...server,
        tables: {},
      },
      serverTables: {},
    }
  }

  // Player session history
  if (query.includes('listPlayerSessionHistory')) {
    return {
      listPlayerSessionHistory: {
        total: 5,
        records: [
          { id: 'sh-1', join: Math.floor(Date.now() / 1000) - 3600, leave: Math.floor(Date.now() / 1000) - 1800, ip: '192.168.1.100' },
          { id: 'sh-2', join: Math.floor(Date.now() / 1000) - 86400, leave: Math.floor(Date.now() / 1000) - 82800, ip: '192.168.1.100' },
          { id: 'sh-3', join: Math.floor(Date.now() / 1000) - 172800, leave: Math.floor(Date.now() / 1000) - 169200, ip: '192.168.1.101' },
        ]
      }
    }
  }

  // Player chat history
  if (query.includes('playerChatHistory')) {
    return {
      playerChatHistory: [
        { id: 'ch-1', message: 'Hello everyone!', created: Math.floor(Date.now() / 1000) - 60 },
        { id: 'ch-2', message: 'Anyone want to trade?', created: Math.floor(Date.now() / 1000) - 120 },
        { id: 'ch-3', message: 'Thanks for the help!', created: Math.floor(Date.now() / 1000) - 180 },
      ]
    }
  }

  // Report comments
  if (query.includes('listPlayerReportComments')) {
    return {
      listPlayerReportComments: {
        total: 2,
        records: [
          { id: 'rc-1', comment: 'I witnessed this as well, they were definitely flying.', created: Math.floor(Date.now() / 1000) - 600, actor: DEMO_PLAYERS[7], type: 'comment' },
          { id: 'rc-2', comment: 'Looking into this report now.', created: Math.floor(Date.now() / 1000) - 300, actor: DEMO_PLAYERS[2], type: 'comment' },
        ]
      }
    }
  }

  // Appeal comments
  if (query.includes('listPlayerAppealComments')) {
    return {
      listPlayerAppealComments: {
        total: 1,
        records: [
          { id: 'ac-1', comment: 'We are reviewing your appeal.', created: Math.floor(Date.now() / 1000) - 900, actor: DEMO_PLAYERS[2], type: 'comment' },
        ]
      }
    }
  }

  // Default empty response
  console.log('[Mock GraphQL] Unhandled query:', query.substring(0, 200))
  return {}
}

// Handle mutations (just return success)
function handleMutation(query, variables) {
  console.log('[Mock GraphQL] Mutation:', query.substring(0, 100), variables)
  
  // Return success for any mutation
  if (query.includes('createPlayerBan')) return { createPlayerBan: { id: 'new-ban-' + Date.now(), ...variables?.input } }
  if (query.includes('createPlayerMute')) return { createPlayerMute: { id: 'new-mute-' + Date.now(), ...variables?.input } }
  if (query.includes('createPlayerWarning')) return { createPlayerWarning: { id: 'new-warn-' + Date.now(), ...variables?.input } }
  if (query.includes('createPlayerKick')) return { createPlayerKick: { id: 'new-kick-' + Date.now(), ...variables?.input } }
  if (query.includes('createPlayerNote')) return { createPlayerNote: { id: 'new-note-' + Date.now(), ...variables?.input } }
  if (query.includes('createAppeal')) return { createAppeal: { id: 'new-appeal-' + Date.now() } }
  if (query.includes('createReportComment')) return { createReportComment: { id: 'new-rc-' + Date.now() } }
  if (query.includes('createAppealComment')) return { createAppealComment: { id: 'new-ac-' + Date.now() } }
  if (query.includes('updatePlayerBan')) return { updatePlayerBan: { id: variables?.id } }
  if (query.includes('updatePlayerMute')) return { updatePlayerMute: { id: variables?.id } }
  if (query.includes('updatePlayerWarning')) return { updatePlayerWarning: { id: variables?.id } }
  if (query.includes('updateRole')) return { updateRole: { id: variables?.id } }
  if (query.includes('createRole')) return { createRole: { id: 'new-role-' + Date.now() } }
  if (query.includes('deleteRole')) return { deleteRole: true }
  if (query.includes('createServer')) return { createServer: { id: 'new-server-' + Date.now() } }
  if (query.includes('updateServer')) return { updateServer: { id: variables?.id } }
  if (query.includes('deleteServer')) return { deleteServer: true }
  if (query.includes('createWebhook')) return { createWebhook: { id: 'new-wh-' + Date.now() } }
  if (query.includes('updateWebhook')) return { updateWebhook: { id: variables?.id } }
  if (query.includes('deleteWebhook')) return { deleteWebhook: true }
  if (query.includes('setPassword')) return { setPassword: true }
  if (query.includes('setEmail')) return { setEmail: true }
  if (query.includes('assignRole')) return { assignRole: DEMO_PLAYERS[0] }
  if (query.includes('assignServerRole')) return { assignServerRole: DEMO_PLAYERS[0] }
  
  return { success: true }
}

export default async function handler(req, res) {
  // Handle CORS
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type')

  if (req.method === 'OPTIONS') {
    return res.status(200).end()
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' })
  }

  try {
    const { query, variables } = req.body

    if (!query) {
      return res.status(400).json({ error: 'Query is required' })
    }

    let data
    if (query.trim().startsWith('mutation')) {
      data = handleMutation(query, variables)
    } else {
      data = handleQuery(query, variables)
    }

    return res.status(200).json({ data })
  } catch (error) {
    console.error('[Mock GraphQL] Error:', error)
    return res.status(500).json({ 
      errors: [{ message: error.message }]
    })
  }
}

