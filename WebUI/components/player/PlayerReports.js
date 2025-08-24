import { useState, useEffect } from 'react'
import { format, fromUnixTime } from 'date-fns'
import Badge from '../Badge'
import Link from 'next/link'
import Loader from '../Loader'
import Avatar from '../Avatar'
import Table from '../Table'
import Pagination from '../Pagination'
import ServerSelector from '../admin/ServerSelector'
import { fromNow, useApi } from '../../utils'

const query = `
  query listPlayerReports($serverId: ID!, $player: UUID, $limit: Int, $offset: Int) {
    listPlayerReports(serverId: $serverId, player: $player, limit: $limit, offset: $offset) {
      total
      records {
        id
        created
        updated
        state { id name }
        player { id name }
        assignee { id name }
      }
    }
  }`

const ReportRow = ({ serverId, row, dateFormat }) => (
  <Table.Row>
    <Table.Cell>
      <Link href={`/reports/${serverId}/${row.id}`} passHref>
        <Badge className='bg-accent-500 sm:mx-auto'>#{row.id}</Badge>
      </Link>
    </Table.Cell>
    <Table.Cell>
      <Link href={`/reports/${serverId}/${row.id}`} passHref>
        <div className='flex items-center'>
          <div className='flex-shrink-0'>
            <Avatar uuid={row.player.id} height='26' width='26' />
          </div>
          <div className='ml-3'>
            <p className='whitespace-no-wrap'>{row.player.name}</p>
          </div>
        </div>
      </Link>
    </Table.Cell>
    <Table.Cell>{format(fromUnixTime(row.created), dateFormat)}</Table.Cell>
    <Table.Cell>{row.state.name}</Table.Cell>
    <Table.Cell>
      {row.assignee && (
        <Link href={`/player/${row.assignee.id}`} passHref>
          <div className='flex items-center'>
            <div className='flex-shrink-0'>
              <Avatar uuid={row.assignee.id} height='26' width='26' />
            </div>
            <div className='ml-3'>
              <p className='whitespace-no-wrap'>{row.assignee.name}</p>
            </div>
          </div>
        </Link>
      )}
    </Table.Cell>
    <Table.Cell>{fromNow(row.updated)}</Table.Cell>
  </Table.Row>
)

export default function PlayerReports ({ id, title = 'Reports' }) {
  const [tableState, setTableState] = useState({ serverId: null, activePage: 1, limit: 10, offset: 0 })

  // When ServerSelector auto-picks first server, onChange will set serverId.
  // Also ensure we default once on mount in case ServerSelector's query finishes after this.
  const handleServerChange = (serverId) => setTableState(prev => ({ ...prev, serverId }))

  const variables = tableState.serverId ? { serverId: tableState.serverId, player: id, limit: tableState.limit, offset: tableState.offset } : null
  const { loading, data } = useApi({ query: !variables ? null : query, variables })

  const handlePageChange = ({ activePage }) => setTableState({ ...tableState, activePage, offset: (activePage - 1) * tableState.limit })
  const dateFormat = 'yyyy-MM-dd HH:mm:ss'
  const total = data?.listPlayerReports?.total || 0
  const totalPages = Math.ceil(total / tableState.limit)

  return (
    <div className='space-y-3'>
      <h1 className='text-lg font-bold pb-2 border-b border-accent-200 leading-none flex items-center'>
        <span className='mr-4'>{title}</span>
        <div className='w-40 inline-block'>
          <ServerSelector onChange={handleServerChange} />
        </div>
      </h1>
      {!tableState.serverId && (
        <div className='py-8'><Loader /></div>
      )}
      {tableState.serverId && (
        <Table>
          <Table.Header>
            <Table.Row>
              <Table.HeaderCell>ID</Table.HeaderCell>
              <Table.HeaderCell>Player</Table.HeaderCell>
              <Table.HeaderCell>At</Table.HeaderCell>
              <Table.HeaderCell>State</Table.HeaderCell>
              <Table.HeaderCell>Assigned</Table.HeaderCell>
              <Table.HeaderCell>Last Updated</Table.HeaderCell>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {loading
              ? <Table.Row><Table.Cell colSpan='6'><Loader /></Table.Cell></Table.Row>
              : data?.listPlayerReports?.records?.map((row, i) => (<ReportRow serverId={tableState.serverId} row={row} dateFormat={dateFormat} key={i} />))}
          </Table.Body>
          <Table.Footer>
            <Table.Row>
              <Table.HeaderCell colSpan='6' border={false}>
                <Pagination totalPages={totalPages} activePage={tableState.activePage} onPageChange={handlePageChange} />
              </Table.HeaderCell>
            </Table.Row>
          </Table.Footer>
        </Table>
      )}
    </div>
  )
} 