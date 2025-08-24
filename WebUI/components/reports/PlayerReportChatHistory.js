import { useState } from 'react'
import { format, fromUnixTime } from 'date-fns'
import { useApi } from '../../utils'
import Button from '../Button'
import ErrorMessages from '../ErrorMessages'
import Loader from '../Loader'

export default function PlayerReportChatHistory({ report, serverId }) {
  const [showHistory, setShowHistory] = useState(false)
  const [limit, setLimit] = useState(20)

  const { loading, data, errors } = useApi({
    variables: { playerId: report.player.id, serverId, limit },
    query: showHistory ? `
      query playerChatHistory($playerId: UUID!, $serverId: ID!, $limit: Int!) {
        playerChatHistory(playerId: $playerId, serverId: $serverId, limit: $limit) {
          id
          message
          world
          x
          y
          z
          created
        }
      }
    ` : null
  })

  if (!showHistory) {
      return (
    <div className='bg-gray-50 dark:bg-gray-900 overflow-hidden shadow rounded-lg border border-gray-200 dark:border-gray-700'>
      <div className='px-4 py-5 sm:p-6'>
        <div className='flex items-center justify-between'>
          <div>
            <h3 className='text-lg leading-6 font-medium text-gray-900 dark:text-white'>
              Message History
            </h3>
            <p className='mt-1 text-sm text-gray-500 dark:text-gray-400'>
              View recent chat messages from {report.player.name}
            </p>
          </div>
          <Button onClick={() => setShowHistory(true)}>
            View Messages
          </Button>
        </div>
      </div>
    </div>
  )
  }

  if (loading) return <Loader />
  if (errors) return <ErrorMessages errors={errors} />

  const messages = data?.playerChatHistory || []

  return (
    <div className='bg-gray-50 dark:bg-gray-900 overflow-hidden shadow rounded-lg border border-gray-200 dark:border-gray-700'>
      <div className='px-4 py-5 sm:p-6'>
        <div className='flex items-center justify-between mb-4'>
          <div>
            <h3 className='text-lg leading-6 font-medium text-gray-900 dark:text-white'>
              Message History ({messages.length})
            </h3>
            <p className='mt-1 text-sm text-gray-500 dark:text-gray-400'>
              Recent chat messages from {report.player.name}
            </p>
          </div>
          <div className='flex space-x-2'>
            <select
              value={limit}
              onChange={(e) => setLimit(parseInt(e.target.value))}
              className='min-w-0 w-24 pl-3 pr-8 py-2 text-sm border-gray-300 dark:border-gray-600 focus:outline-none focus:ring-primary-500 focus:border-primary-500 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white'
            >
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
            <Button variant='secondary' onClick={() => setShowHistory(false)}>
              Hide
            </Button>
          </div>
        </div>

        {messages.length === 0 ? (
          <div className='text-center py-8'>
            <div className='text-gray-500 dark:text-gray-400'>
              <svg className='mx-auto h-12 w-12' fill='none' viewBox='0 0 24 24' stroke='currentColor'>
                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-3.582 8-8 8a8.959 8.959 0 01-4.906-1.471L3 21l2.471-5.094A8.959 8.959 0 013 12c0-4.418 3.582-8 8-8s8 3.582 8 8z' />
              </svg>
              <h3 className='mt-2 text-sm font-medium text-gray-900 dark:text-white'>No messages found</h3>
              <p className='mt-1 text-sm text-gray-500 dark:text-gray-400'>
                No recent chat messages found for this player.
              </p>
              <p className='mt-1 text-xs text-gray-400 dark:text-gray-500'>
                Messages are only tracked while the server is running with the latest BanManager version.
              </p>
            </div>
          </div>
        ) : (
          <div className='space-y-3 max-h-96 overflow-y-auto'>
            {messages.map((message) => (
              <div key={message.id} className='bg-gray-50 dark:bg-gray-700 rounded-lg p-4 border border-gray-200 dark:border-gray-600'>
                <div className='flex items-start justify-between'>
                  <div className='flex-1'>
                    <p className='text-sm text-gray-900 dark:text-white font-mono bg-white dark:bg-gray-800 p-3 rounded border border-gray-200 dark:border-gray-600 shadow-sm'>
                      {message.message}
                    </p>
                    <div className='mt-3 flex items-center text-xs text-gray-500 dark:text-gray-400 space-x-4'>
                      <span className='flex items-center'>
                        <svg className='w-4 h-4 mr-1' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                          <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' />
                        </svg>
                        {format(fromUnixTime(message.created), 'MMM d, yyyy HH:mm:ss')}
                      </span>
                      <span className='flex items-center'>
                        <svg className='w-4 h-4 mr-1' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                          <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z' />
                          <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M15 11a3 3 0 11-6 0 3 3 0 016 0z' />
                        </svg>
                        {message.world} ({message.x}, {message.y}, {message.z})
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {messages.length > 0 && (
          <div className='mt-4 text-xs text-gray-500 dark:text-gray-400 text-center'>
            Showing {messages.length} most recent messages • Messages are automatically cleaned up after 30 days
          </div>
        )}
      </div>
    </div>
  )
}
