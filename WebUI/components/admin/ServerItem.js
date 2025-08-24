import { useEffect, useState } from 'react'
import { FaBan } from 'react-icons/fa'
import { BsMicMute } from 'react-icons/bs'
import { AiOutlineWarning } from 'react-icons/ai'
import { FaFlag, FaSignOutAlt } from 'react-icons/fa'
import clsx from 'clsx'
import Link from 'next/link'
import ResponsivePie, { PieContext } from '../charts/ResponsivePie'
import resolveConfig from 'tailwindcss/resolveConfig'
import tailwindConfig from '../../tailwind.config'
import Modal from '../Modal'
import { useMutateApi } from '../../utils'
import ErrorMessages from '../ErrorMessages'
import Message from '../Message'
import Input from '../Input'

const fullConfig = resolveConfig(tailwindConfig)

const StatItem = ({ onClick, icon, value, label, selected }) => {
  return (
    <div onMouseEnter={onClick} onClick={onClick} className={clsx('p-4 rounded-2xl border-primary-900 text-center transition-colors flex flex-col items-center justify-center', { 'bg-gray-900': !selected, 'bg-gray-800': selected })}>
      {icon}
      <h2 className='title-font font-medium text-2xl'>{value}</h2>
      <p className='text-sm text-gray-400'>{label}</p>
    </div>
  )
}

export default function ServerItem ({ canDelete, server, onDeleted }) {
  const [open, setOpen] = useState(false)
  const [confirmValue, setConfirmValue] = useState('')
  const { load, loading, errors, data } = useMutateApi({
    query: `mutation deleteServer($id: ID!) {
        deleteServer(id: $id)
      }`
  })

  useEffect(() => {
    if (!data) return
    if (Object.keys(data).some(key => !!data[key])) {
      setOpen(false)
      onDeleted(data)
    }
  }, [data])

  const showConfirmDelete = (e) => {
    e.preventDefault()

    setOpen(true)
  }
  const handleConfirmDelete = async () => {
    setConfirmValue('')
    await load({ id: server.id })
  }
  const handleDeleteCancel = () => {
    setConfirmValue('')
    setOpen(false)
  }

  const chartData = [
    {
      id: 'bans' + server.id,
      label: 'bans',
      value: server.stats.totalActiveBans,
      color: fullConfig.theme.colors.red['500']
    },
    {
      id: 'mutes' + server.id,
      label: 'mutes',
      value: server.stats.totalActiveMutes,
      color: fullConfig.theme.colors.indigo['500']
    },
    {
      id: 'reports' + server.id,
      label: 'reports',
      value: server.stats.totalReports,
      color: fullConfig.theme.colors.rose['700']
    },
    {
      id: 'warnings' + server.id,
      label: 'warnings',
      value: server.stats.totalWarnings,
      color: fullConfig.theme.colors.amber['500']
    },
    {
      id: 'kicks' + server.id,
      label: 'kicks',
      value: server.stats.totalKicks,
      color: fullConfig.theme.colors.orange['800']
    }
  ]

  return (
    <div className='bg-black shadow-md rounded-md overflow-hidden text-center w-[32rem] relative'>
      <Modal
        title='Delete server'
        confirmButton='Delete'
        confirmDisabled={confirmValue !== server.name}
        open={open}
        onConfirm={handleConfirmDelete}
        onCancel={handleDeleteCancel}
        loading={loading}
      >
        <ErrorMessages errors={errors} />
        <Message warning>
          <Message.Header>Warning</Message.Header>
          <Message.List>
            <Message.Item>Related <strong>appeals and roles</strong> will be removed</Message.Item>
            <Message.Item>This action cannot be undone</Message.Item>
          </Message.List>
        </Message>
        <p className='mb-4'>Please type <strong>{server.name}</strong> to confirm</p>
        <Input
          onChange={(e, { value }) => setConfirmValue(value)}
          placeholder='Type server name'
          className='mb-0'
          inputClassName='border border-gray-900'
          required
        />
      </Modal>
      <div className='pt-5 px-5 flex justify-between items-center'>
        <h5 className='text-xl font-semibold mb-2'>
          {server.name}
        </h5>
        <div className='flex items-center gap-2'>
          <span className='text-xs bg-emerald-700 text-white px-2 py-1 rounded-full'>
            total: {server.stats.totalPlayers} players
          </span>
          <Link
            href={`/admin/servers/${server.id}/edit`}
            className='
              bg-accent-500
              hover:bg-accent-600
              text-white
              font-bold
              uppercase
              text-xs
              px-4
              py-2
              rounded
              shadow
              hover:shadow-md
              outline-none
              focus:outline-none
              ease-linear
              transition-all
              duration-150
            '
            title='Edit Server'
          >
            Edit
          </Link>
          {canDelete &&
            <button
              className='
                bg-red-500
                text-white
                active:bg-red-600
                font-bold
                uppercase
                text-xs
                px-4
                py-2
                rounded
                shadow
                hover:shadow-md
                outline-none
                focus:outline-none
                ease-linear
                transition-all
                duration-150
              '
              type='button'
              onClick={showConfirmDelete}
            >
              Delete
            </button>}
        </div>
      </div>
      <ResponsivePie
        chartData={chartData}
        selectedLabel='reports'
      >
        <PieContext.Consumer>
          {({ setSelectedLabel, selectedLabel }) => (
            <div className='grid grid-cols-5 gap-4 pb-6 px-4'>
              <StatItem
                icon={<FaBan className='w-10 h-10 inline-block mb-2 text-red-800' />}
                selected={selectedLabel === 'bans'}
                onClick={() => setSelectedLabel('bans')}
                value={server.stats.totalActiveBans}
                label='Bans'
              />
              <StatItem
                icon={<BsMicMute className='w-10 h-10 inline-block mb-2 text-indigo-800' />}
                selected={selectedLabel === 'mutes'}
                onClick={() => setSelectedLabel('mutes')}
                value={server.stats.totalActiveMutes}
                label='Mutes'
              />
              <StatItem
                icon={<FaFlag className='w-10 h-10 inline-block mb-2 text-rose-700' />}
                selected={selectedLabel === 'reports'}
                onClick={() => setSelectedLabel('reports')}
                value={server.stats.totalReports}
                label='Reports'
              />
              <StatItem
                icon={<AiOutlineWarning className='w-10 h-10 inline-block mb-2 text-amber-800' />}
                selected={selectedLabel === 'warnings'}
                onClick={() => setSelectedLabel('warnings')}
                value={server.stats.totalWarnings}
                label='Warnings'
              />
              <StatItem
                icon={<FaSignOutAlt className='w-10 h-10 inline-block mb-2 text-orange-800' />}
                selected={selectedLabel === 'kicks'}
                onClick={() => setSelectedLabel('kicks')}
                value={server.stats.totalKicks}
                label='Kicks'
              />
            </div>
          )}
        </PieContext.Consumer>
      </ResponsivePie>
    </div>
  )
}
