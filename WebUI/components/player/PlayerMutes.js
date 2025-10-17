import React, { useState, useEffect } from 'react'
import Loader from '../Loader'
import ServerSelector from '../admin/ServerSelector'
import { useApi } from '../../utils'
import PastPlayerPunishment from './PastPlayerPunishment'

const query = `
query listPlayerPunishmentRecords($serverId: ID!, $player: UUID!, $type: RecordType!) {
  listPlayerPunishmentRecords(serverId: $serverId, player: $player, type: $type) {
    total
    records {
      ... on PlayerMuteRecord {
        id
        actor {
          id
          name
        }
        pastActor {
          id
          name
        }
        created
        pastCreated
        expired
        createdReason
        reason
        soft
        acl {
          delete
        }
      }
    }
  }
}`

export default function PlayerMutes ({ id }) {
  const [tableState, setTableState] = useState({ type: 'PlayerMuteRecord', serverId: null })
  const { loading, data, mutate } = useApi({ query: !tableState.serverId ? null : query, variables: { ...tableState, player: id } })
  
  // Auto-select first server when servers are loaded
  const { data: serversData } = useApi({ query: `query { servers { id name } }` })
  
  React.useEffect(() => {
    if (serversData?.servers?.length && !tableState.serverId) {
      setTableState({ ...tableState, serverId: serversData.servers[0].id })
    }
  }, [serversData, tableState.serverId])

  const rows = data?.listPlayerPunishmentRecords?.records || []
  const total = data?.listPlayerPunishmentRecords?.total || 0
  const onDeleted = ({ deletePlayerMuteRecord: { id } }) => {
    const records = rows.filter(c => c.id !== id)

    mutate({ ...data, listPlayerPunishmentRecords: { records, total: total - 1 } }, false)
  }

  // AGGRESSIVE cleanup function to remove orphaned records
  const cleanupOrphanedRecords = () => {
    if (!data?.listPlayerPunishmentRecords?.records) return
    
    const originalCount = data.listPlayerPunishmentRecords.records.length
    
    // More aggressive filtering - remove records with ANY suspicious data
    const validRecords = data.listPlayerPunishmentRecords.records.filter(record => {
      // Keep records that have ALL required valid data
      const isValid = record.id && 
                     record.created && 
                     record.created > 0 && 
                     record.reason && 
                     record.actor &&
                     record.actor.id
      
      if (!isValid) {
        console.warn('🗑️ Removing invalid mute record:', record)
      }
      return isValid
    })

    if (validRecords.length !== originalCount) {
      console.log(`🧹 AGGRESSIVE CLEANUP: Removed ${originalCount - validRecords.length} problematic mute records`)
      mutate({ 
        ...data, 
        listPlayerPunishmentRecords: { 
          records: validRecords, 
          total: validRecords.length 
        } 
      }, false)
    }
  }

  // Run aggressive cleanup IMMEDIATELY when data loads
  useEffect(() => {
    if (data?.listPlayerPunishmentRecords?.records) {
      // Run cleanup after a tiny delay to ensure component is mounted
      setTimeout(() => {
        cleanupOrphanedRecords()
      }, 50)
    }
  }, [data?.listPlayerPunishmentRecords?.records])



  return (
    <div>
      <h1
        className='pb-4 mb-4 border-b border-indigo-800' id='mutes'
      >
        <div className='flex items-center'>
          <p className='mr-6 text-xl font-bold '>Past Mutes ({total})</p>
          <div className='w-40 inline-block'>
            <ServerSelector
              onChange={serverId => setTableState({ ...tableState, serverId })}
            />
          </div>
        </div>
      </h1>
      <div className='relative'>
        {loading && <div className='absolute bg-black/50 h-full w-full'><Loader /></div>}
        {data?.listPlayerPunishmentRecords?.total > 0 && rows.map((row, i) => (<PastPlayerPunishment type='mute' punishment={row} key={i} serverId={tableState.serverId} onDeleted={onDeleted} />))}
      </div>
      {!data?.listPlayerPunishmentRecords?.total && (
        <div className='flex items-center'>
          <div>
            None
          </div>
        </div>
      )}
    </div>
  )
}
