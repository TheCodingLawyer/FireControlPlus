/**
 * Reports Provider - Detects which reports system is being used
 * Supports: BanManager (default) and TigerReports
 */

async function getReportsProvider ({ pool, config }) {
  try {
    // Check if TigerReports table exists
    const result = await pool.raw('SHOW TABLES LIKE \'tigerreports_reports\'')
    
    if (result && result[0] && result[0].length > 0) {
      return 'tigerreports'
    }
  } catch (error) {
    // Table doesn't exist or error occurred, fall back to default
  }
  
  return 'banmanager'
}

module.exports = {
  getReportsProvider
}

