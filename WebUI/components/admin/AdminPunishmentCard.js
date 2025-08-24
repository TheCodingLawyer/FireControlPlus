import { useEffect, useState } from 'react'
import Link from 'next/link'
import { BsTrash } from 'react-icons/bs'
import Badge from '../Badge'
import PermanentBadge from '../player/PermanentBadge'
import ErrorMessages from '../ErrorMessages'
import Avatar from '../Avatar'
import Modal from '../Modal'
import Button from '../Button'
import { formatTimestampAsDate, formatTimestampAsTime, useMutateApi } from '../../utils'
import { TimeDurationAbbreviated } from '../Time'

const metaMap = {
  ban: {
    editPath: 'ban',
    deleteMutation: `mutation deletePlayerBanRecord($id: ID!, $serverId: ID!) {
      deletePlayerBanRecord(id: $id, serverId: $serverId) {
        id
      }
    }`
  },
  mute: {
    editPath: 'mute',
    deleteMutation: `mutation deletePlayerMuteRecord($id: ID!, $serverId: ID!) {
      deletePlayerMuteRecord(id: $id, serverId: $serverId) {
        id
      }
    }`
  },
  note: {
    editPath: 'note',
    deleteMutation: `mutation deletePlayerNote($id: ID!, $serverId: ID!) {
      deletePlayerNote(id: $id, serverId: $serverId) {
        id
      }
    }`
  },
  warning: {
    editPath: 'warning',
    deleteMutation: `mutation deletePlayerWarning($id: ID!, $serverId: ID!) {
      deletePlayerWarning(id: $id, serverId: $serverId) {
        id
      }
    }`
  },
  kick: {
    editPath: 'kick',
    deleteMutation: `mutation deletePlayerKick($id: ID!, $serverId: ID!) {
      deletePlayerKick(id: $id, serverId: $serverId) {
        id
      }
    }`
  }
}

export default function AdminPunishmentCard ({ punishment, serverId, type, onDeleted, isActive = false }) {
  const meta = metaMap[type]
  const [open, setOpen] = useState(false)

  const { load, data, loading, errors } = useMutateApi({ query: meta.deleteMutation })

  const showConfirmDelete = (e) => {
    e.preventDefault()
    setOpen(true)
  }
  
  const handleConfirmDelete = async () => {
    await load({ id: punishment.id, serverId })
  }
  
  const handleDeleteCancel = () => setOpen(false)

  useEffect(() => {
    if (!data) return
    if (Object.keys(data).some(key => !!data[key].id)) {
      setOpen(false)
      onDeleted(data)
    }
  }, [data])

  // Determine duration label
  let durationLabel = null
  if (type === 'kick') {
    durationLabel = <Badge className='border border-primary-900 py-0 px-1 text-sm inline-flex'>Instant</Badge>
  } else if (punishment.expired === 0 || punishment.expires === 0) {
    durationLabel = <PermanentBadge className='inline-flex' />
  } else {
    const expiryTime = punishment.expired || punishment.expires
    const createdTime = punishment.pastCreated || punishment.created
    if (expiryTime && createdTime) {
      durationLabel = <Badge className='border border-primary-900 py-0 px-1 text-sm inline-flex'>
        <TimeDurationAbbreviated startTimestamp={createdTime} endTimestamp={expiryTime} />
      </Badge>
    }
  }

  // Get the actor (who issued the punishment)
  const actor = punishment.actor || punishment.pastActor
  const createdTime = punishment.created || punishment.pastCreated

  // Dynamic styling based on active/past status
  const cardClasses = isActive 
    ? 'w-full border-2 border-red-500 bg-red-900/10 rounded-3xl p-4 mb-2 shadow-lg shadow-red-500/20' 
    : 'w-full border border-gray-600 bg-gray-900/20 rounded-3xl p-4 mb-2'
  
  const statusBadge = isActive ? (
    <div className='flex items-center gap-1 px-2 py-1 bg-red-500 text-white text-xs font-semibold rounded-full'>
      <div className='w-2 h-2 bg-white rounded-full animate-pulse'></div>
      ACTIVE
    </div>
  ) : (
    <div className='flex items-center gap-1 px-2 py-1 bg-gray-500 text-gray-200 text-xs font-semibold rounded-full'>
      <div className='w-2 h-2 bg-gray-300 rounded-full'></div>
      EXPIRED
    </div>
  )

  return (
    <div className={cardClasses}>
      <div className='grid grid-cols-12 gap-4'>
        {/* Punished Player */}
        <div className='col-span-12 lg:col-span-5'>
          <div className='flex flex-row gap-4'>
            <div className='flex flex-col flex-none justify-center items-center gap-2'>
              {punishment.player ? (
                <Link href={`/player/${punishment.player.id}`}>
                  <Avatar uuid={punishment.player.id} height='40' width='40' />
                </Link>
              ) : (
                <Avatar uuid="00000000-0000-0000-0000-000000000000" height='40' width='40' />
              )}
            </div>
            <div className='flex flex-col text-left flex-grow'>
              <div className='flex items-center gap-2 flex-wrap'>
                {punishment.player ? (
                  <Link href={`/player/${punishment.player.id}`}>
                    <span className='truncate font-semibold text-lg'>{punishment.player.name}</span>
                  </Link>
                ) : (
                  <span className='truncate text-gray-400 font-semibold text-lg'>Unknown Player</span>
                )}
                {statusBadge}
                <div className='block lg:hidden'>
                  {durationLabel}
                </div>
              </div>
              <div className='flex flex-row gap-1 text-sm text-gray-400'>
                <span>{formatTimestampAsDate(createdTime)}</span>
                <span>{formatTimestampAsTime(createdTime)}</span>
              </div>
              <div className='mt-2'>
                <span className='text-sm text-gray-400'>Reason: </span>
                <span className='break-words whitespace-normal'>{punishment.reason}</span>
              </div>
              {punishment.points && (
                <div className='mt-1'>
                  <span className='text-sm text-gray-400'>Points: </span>
                  <span>{punishment.points}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Duration & Actions */}
        <div className='col-span-12 lg:col-span-2 text-center place-self-center flex items-center gap-2 lg:gap-0 flex-row lg:flex-col'>
          <div className='hidden lg:block'>{durationLabel}</div>
          {punishment.acl.delete && (
            <div className='block lg:hidden'>
              <Button className='bg-red-800 w-auto' onClick={showConfirmDelete}>
                <BsTrash className='text-sm' />
              </Button>
            </div>
          )}
        </div>

        {/* Punished By */}
        <div className='col-span-12 lg:col-span-5'>
          <div className='flex flex-row gap-4'>
            <div className='flex flex-col flex-none justify-center items-center gap-2'>
              {actor ? (
                <Avatar uuid={actor.id} height='33' width='33' />
              ) : (
                <Avatar uuid="00000000-0000-0000-0000-000000000000" height='33' width='33' />
              )}
            </div>
            <div className='flex flex-col text-left flex-grow'>
              <div className='text-sm text-gray-400 mb-1'>Punished by:</div>
              {actor ? (
                <span className='truncate'>{actor.name}</span>
              ) : (
                <span className='truncate text-gray-400'>Unknown</span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Delete Modal */}
      {punishment.acl.delete && (
        <>
          <Modal
            title={`Delete ${type}`}
            confirmButton='Delete'
            open={open}
            onConfirm={handleConfirmDelete}
            onCancel={handleDeleteCancel}
            loading={loading}
          >
            <ErrorMessages errors={errors} />
            <p className='pb-1'>Are you sure you want to delete this {type}?</p>
            <p className='pb-1'>This action cannot be undone</p>
          </Modal>
          <div className='hidden lg:flex justify-end'>
            <Button className='mt-4 bg-red-800 w-auto' onClick={showConfirmDelete}>
              <BsTrash className='text-sm' />
            </Button>
          </div>
        </>
      )}
    </div>
  )
}
