import React, { useState } from 'react'
import Loader from '../Loader'
import ServerSelector from './ServerSelector'
import { useApi } from '../../utils'
import AdminPunishmentCard from './AdminPunishmentCard'

const activeMutesQuery = `
query listPlayerPunishmentRecords($serverId: ID!, $type: RecordType!) {
  listPlayerPunishmentRecords(serverId: $serverId, type: $type) {
    total
    records {
      ... on PlayerMute {
        id
        player {
          id
          name
        }
        actor {
          id
          name
        }
        created
        reason
        expires
        acl {
          delete
          update
        }
      }
    }
  }
}`;

const pastMutesQuery = `
query listPlayerPunishmentRecords($serverId: ID!, $type: RecordType!) {
  listPlayerPunishmentRecords(serverId: $serverId, type: $type) {
    total
    records {
      ... on PlayerMuteRecord {
        id
        player {
          id
          name
        }
        actor {
          id
          name
        }
        created
        reason
        expired
        acl {
          delete
          update
        }
      }
    }
  }
}`

export default function AllPlayerMutes () {
  const [tableState, setTableState] = useState({ serverId: null })
  
  // Fetch both active and past mutes with error handling
  const { loading: activeLoading, data: activeData, error: activeError } = useApi({ 
    query: !tableState.serverId ? null : activeMutesQuery, 
    variables: { serverId: tableState.serverId, type: 'PlayerMute' } 
  })
  const { loading: pastLoading, data: pastData } = useApi({ 
    query: !tableState.serverId ? null : pastMutesQuery, 
    variables: { serverId: tableState.serverId, type: 'PlayerMuteRecord' } 
  })
  
  // Auto-select first server when servers are loaded
  const { data: serversData } = useApi({ query: `query { servers { id name } }` })
  
  React.useEffect(() => {
    if (serversData?.servers?.length && !tableState.serverId) {
      setTableState({ ...tableState, serverId: serversData.servers[0].id })
    }
  }, [serversData, tableState.serverId])

  // Combine active and past mutes, fallback to past only if active fails
  const activeRows = (!activeError && activeData?.listPlayerPunishmentRecords?.records) || []
  const pastRows = pastData?.listPlayerPunishmentRecords?.records || []
  
  // Mark records as active or past for UI distinction
  const markedActiveRows = activeRows.map(row => ({ ...row, _isActive: true, _type: 'active' }))
  const markedPastRows = pastRows.map(row => ({ ...row, _isActive: false, _type: 'past' }))
  
  const allRows = [...markedActiveRows, ...markedPastRows].sort((a, b) => b.created - a.created)
  const total = activeRows.length + pastRows.length
  const loading = activeLoading || pastLoading

  const onDeleted = ({ deletePlayerMute, deletePlayerMuteRecord }) => {
    // Handle deletion of either active or past mute
    const deletedId = deletePlayerMute?.id || deletePlayerMuteRecord?.id
    // Note: We'd need to refetch data here for simplicity, or implement more complex state management
    window.location.reload() // Simple solution for now
  }

  return (
    <div>
      <h1
        className='pb-4 mb-4 border-b border-indigo-800' id='mutes'
      >
        <div className='flex items-center'>
          <p className='mr-6 text-xl font-bold '>All Mutes ({total})</p>
          <div className='flex-grow'></div>
          <div className='w-40 inline-block'>
            <ServerSelector
              onChange={serverId => setTableState({ ...tableState, serverId })}
            />
          </div>
        </div>
      </h1>
      <div className='relative'>
        {loading && <div className='absolute bg-black/50 h-full w-full'><Loader /></div>}
        {total > 0 && allRows.map((row, i) => (
          <AdminPunishmentCard key={i} type='mute' punishment={row} serverId={tableState.serverId} onDeleted={onDeleted} isActive={row._isActive} />
        ))}
      </div>
      {!total && (
        <div className='flex items-center'>
          <div>
            None
          </div>
        </div>
      )}
    </div>
  )
}
