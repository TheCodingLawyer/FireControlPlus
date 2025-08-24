import React, { useState } from 'react'
import Loader from '../Loader'
import ServerSelector from './ServerSelector'
import { useApi } from '../../utils'
import AdminPunishmentCard from './AdminPunishmentCard'
import { RiNumbersLine } from 'react-icons/ri'

const query = `
query listPlayerPunishmentRecords($serverId: ID!, $type: RecordType!) {
  listPlayerPunishmentRecords(serverId: $serverId, type: $type) {
    total
    records {
      ... on PlayerWarning {
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
        points
        expires
        acl {
          delete
          update
        }
      }
    }
  }
}`

export default function AllPlayerWarnings () {
  const [tableState, setTableState] = useState({ type: 'PlayerWarning', serverId: null })
  const { loading, data, mutate } = useApi({ query: !tableState.serverId ? null : query, variables: { ...tableState } })
  
  // Auto-select first server when servers are loaded
  const { data: serversData } = useApi({ query: `query { servers { id name } }` })
  
  React.useEffect(() => {
    if (serversData?.servers?.length && !tableState.serverId) {
      setTableState({ ...tableState, serverId: serversData.servers[0].id })
    }
  }, [serversData, tableState.serverId])

  const rows = data?.listPlayerPunishmentRecords?.records || []
  const total = data?.listPlayerPunishmentRecords.total || 0
  const onDeleted = ({ deletePlayerWarning: { id } }) => {
    const records = rows.filter(c => c.id !== id)

    mutate({ ...data, listPlayerPunishmentRecords: { records, total: total - 1 } }, false)
  }

  const totalPoints = rows.reduce((acc, row) => acc + (row.points || 0), 0)

  return (
    <div>
      <h1
        className='pb-4 mb-4 border-b border-amber-800' id='warnings'
      >
        <div className='flex items-center'>
          <p className='mr-6 text-xl font-bold'>All Warnings ({total})</p>
          <div className='text-sm text-gray-400 flex items-center gap-4'>
            <div className='flex items-center gap-1'>
              <RiNumbersLine />
              <span className='truncate'>{totalPoints}</span>
            </div>
          </div>
          <div className='w-40 inline-block ml-auto'>
            <ServerSelector
              onChange={serverId => setTableState({ ...tableState, serverId })}
            />
          </div>
        </div>
      </h1>
      <div className='relative'>
        {loading && <div className='absolute bg-black/50 h-full w-full'><Loader /></div>}
        {data?.listPlayerPunishmentRecords?.total > 0 && rows.map((row, i) => (
          <AdminPunishmentCard key={i} type='warning' punishment={row} serverId={tableState.serverId} onDeleted={onDeleted} />
        ))}
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
