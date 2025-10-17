import { useEffect, useState } from 'react'
import Link from 'next/link'
import { BsTrash } from 'react-icons/bs'
import Badge from '../Badge'
import PermanentBadge from './PermanentBadge'
import ErrorMessages from '../ErrorMessages'
import Avatar from '../Avatar'
import Modal from '../Modal'
import Button from '../Button'
import { formatTimestampAsDate, formatTimestampAsTime, useMutateApi } from '../../utils'
import { TimeDurationAbbreviated } from '../Time'
import { MdRedo } from 'react-icons/md'

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
  }
}

export default function PastPlayerPunishment ({ punishment, serverId, type, onDeleted }) {
  const meta = metaMap[type]
  const [open, setOpen] = useState(false)
  const [isDeleted, setIsDeleted] = useState(false)

  const { load, data, loading, errors } = useMutateApi({ query: meta.deleteMutation })

  // Don't render if this item has been deleted
  if (isDeleted) return null

  // Auto-cleanup: If this punishment has invalid data that suggests it's orphaned, remove it
  useEffect(() => {
    // Check for common signs of orphaned/invalid records
    if (!punishment.id || !punishment.created || punishment.created === 0) {
      console.warn(`Removing orphaned ${type} record:`, punishment)
      setIsDeleted(true)
      const deletionKey = `delete${type.charAt(0).toUpperCase() + type.slice(1)}${type === 'note' ? '' : 'Record'}`
      onDeleted({ [deletionKey]: { id: punishment.id || 'orphaned' } })
    }
  }, [punishment, type, onDeleted])

  const showConfirmDelete = (e) => {
    e.preventDefault()

    setOpen(true)
  }
  const handleConfirmDelete = async () => {
    try {
      await load({ id: punishment.id, serverId })
    } catch (error) {
      // If the record is not found, treat it as already deleted and remove from UI
      if (error.message && (error.message.includes('not found') || error.message.includes('does not exist'))) {
        setOpen(false)
        setIsDeleted(true)
        // Simulate successful deletion response to remove from parent list
        const deletionKey = `delete${type.charAt(0).toUpperCase() + type.slice(1)}${type === 'note' ? '' : 'Record'}`
        onDeleted({ [deletionKey]: { id: punishment.id } })
      } else {
        // For other errors, keep the modal open to show the error
        console.error('Deletion failed:', error)
      }
    }
  }
  const handleDeleteCancel = () => setOpen(false)

  useEffect(() => {
    if (!data) return
    if (Object.keys(data).some(key => !!data[key].id)) {
      setOpen(false)
      setIsDeleted(true)
      onDeleted(data)
    }
  }, [data])

  const label = punishment.expired === 0
    ? <PermanentBadge className='inline-flex' />
    : <Badge className='border border-primary-900 py-0 px-1 text-sm inline-flex'><TimeDurationAbbreviated startTimestamp={punishment.pastCreated} endTimestamp={punishment.expired} /></Badge>

  return (
    <div className='w-full border border-primary-900 rounded-3xl p-4 mb-2'>
      <div className='grid grid-cols-12 gap-1'>
        <div className='col-span-12 lg:col-span-5'>
          <div className='flex flex-row gap-4'>
            <div className='flex flex-col flex-none justify-center items-center gap-2'>
              {punishment.pastActor ? (
                <Link href={`/player/${punishment.pastActor.id}`}><Avatar uuid={punishment.pastActor.id} height='33' width='33' /></Link>
              ) : (
                <Avatar uuid="00000000-0000-0000-0000-000000000000" height='33' width='33' />
              )}
            </div>
            <div className='flex flex-col text-left'>
              {punishment.pastActor ? (
                <Link href={`/player/${punishment.pastActor.id}`}><span className='truncate'>{punishment.pastActor.name}</span></Link>
              ) : (
                <span className='truncate text-gray-400'>Unknown</span>
              )}
              <div className='flex flex-row gap-1'>
                <span className='text-sm text-gray-400'>{formatTimestampAsDate(punishment.pastCreated)}</span>
                <span className='text-sm text-gray-400'>{formatTimestampAsTime(punishment.pastCreated)}</span>
              </div>
            </div>
            <div className='block lg:hidden ml-auto self-start'>
              {label}
            </div>
          </div>
          <div className='break-words whitespace-normal mt-2'>{punishment.reason}</div>
        </div>
        <div className='col-span-12 lg:col-span-2 text-center place-self-center flex items-center gap-2 lg:gap-0 flex-row lg:flex-col'>
          <span className='hidden lg:block'>{label}</span>
          <MdRedo className='text-xl my-2 hidden lg:block' />
          {punishment.acl.delete &&
            <div className='block lg:hidden'>
              <Button className='bg-red-800 w-auto' onClick={showConfirmDelete}>
                <BsTrash className='text-sm' />
              </Button>
            </div>}
        </div>
        <div className='col-span-12 lg:col-span-5'>
          <div className='flex flex-row gap-4'>
            <div className='flex flex-col flex-none justify-center items-center gap-2'>
              {punishment.actor ? (
                <Link href={`/player/${punishment.actor.id}`}><Avatar uuid={punishment.actor.id} height='33' width='33' /></Link>
              ) : (
                <Avatar uuid="00000000-0000-0000-0000-000000000000" height='33' width='33' />
              )}
            </div>
            <div className='flex flex-col text-left'>
              {punishment.actor ? (
                <Link href={`/player/${punishment.actor.id}`}><span className='truncate'>{punishment.actor.name}</span></Link>
              ) : (
                <span className='truncate text-gray-400'>Unknown</span>
              )}
              <div className='flex flex-row gap-1'>
                <span className='text-sm text-gray-400'>{formatTimestampAsDate(punishment.created)}</span>
                <span className='text-sm text-gray-400'>{formatTimestampAsTime(punishment.created)}</span>
              </div>
            </div>
          </div>
          {punishment.createdReason
            ? <div className='break-words whitespace-normal mt-2'>{punishment.createdReason}</div>
            : <div className='mt-2 text-gray-400'>Deletion reason unknown</div>}
        </div>
      </div>
      {punishment.acl.delete &&
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
        </>}
    </div>
  )
}
