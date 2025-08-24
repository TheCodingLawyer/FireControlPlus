import { FaBan } from 'react-icons/fa'
import { BsMicMute } from 'react-icons/bs'
import { AiOutlineWarning } from 'react-icons/ai'
import { FaSignOutAlt } from 'react-icons/fa'
import { FaFlag } from 'react-icons/fa'
import ErrorMessages from '../ErrorMessages'
import Loader from '../Loader'
import { useApi } from '../../utils'
import Link from 'next/link'

const PlayerStatistics = ({ id }) => {
  const { loading, data, errors } = useApi({
    variables: { id }, query: `query playerStatistics($id: UUID!) {
      playerStatistics(player: $id) {
        totalActiveBans
        totalBans
        totalMutes
        totalActiveMutes
        totalWarnings
        totalKicks
        totalReports
      }
    }`
  }, { refreshInterval: 5000 })

  if (loading) return <Loader />
  if (errors) return <ErrorMessages errors={errors} />
  const stats = data?.playerStatistics || { totalActiveBans: 0, totalBans: 0, totalMutes: 0, totalActiveMutes: 0, totalWarnings: 0, totalKicks: 0, totalReports: 0 }

  return (
    <div className='grid grid-cols-5 gap-4 text-center'>
      <div>
        <Link href={{ pathname: '/dashboard/punishments', query: { tab: 'bans' } }}>
          <div className='py-4 transform transition duration-500 hover:scale-110 justify-center items-center flex flex-col gap-1'>
            <FaBan className='w-8 h-8 inline-block text-red-800' />
            <h2 className='title-font font-medium'>{(stats.totalBans || 0) + (stats.totalActiveBans || 0)}</h2>
            <p className='text-sm text-gray-400'>Bans</p>
          </div>
        </Link>
      </div>
      <div>
        <Link href={{ pathname: '/dashboard/punishments', query: { tab: 'mutes' } }}>
          <div className='py-4 transform transition duration-500 hover:scale-110 justify-center items-center flex flex-col gap-1'>
            <BsMicMute className='w-8 h-8 inline-block text-indigo-800' />
            <h2 className='title-font font-medium'>{(stats.totalMutes || 0) + (stats.totalActiveMutes || 0)}</h2>
            <p className='text-sm text-gray-400'>Mutes</p>
          </div>
        </Link>
      </div>
      <div>
        <Link href={{ pathname: '/dashboard/punishments', query: { tab: 'warnings' } }}>
          <div className='py-4 transform transition duration-500 hover:scale-110 justify-center items-center flex flex-col gap-1'>
            <AiOutlineWarning className='w-8 h-8 inline-block text-amber-800' />
            <h2 className='title-font font-medium'>{stats.totalWarnings || 0}</h2>
            <p className='text-sm text-gray-400'>Warnings</p>
          </div>
        </Link>
      </div>
      <div>
        <Link href={{ pathname: '/dashboard/punishments', query: { tab: 'kicks' } }}>
          <div className='py-4 transform transition duration-500 hover:scale-110 justify-center items-center flex flex-col gap-1'>
            <FaSignOutAlt className='w-8 h-8 inline-block text-amber-800' />
            <h2 className='title-font font-medium'>{stats.totalKicks || 0}</h2>
            <p className='text-sm text-gray-400'>Kicks</p>
          </div>
        </Link>
      </div>
      <div>
        <Link href='/dashboard/reports'>
          <div className='py-4 transform transition duration-500 hover:scale-110 justify-center items-center flex flex-col gap-1'>
            <FaFlag className='w-8 h-8 inline-block text-rose-700' />
            <h2 className='title-font font-medium'>{stats.totalReports || 0}</h2>
            <p className='text-sm text-gray-400'>Reports</p>
          </div>
        </Link>
      </div>
    </div>
  )
}

export default PlayerStatistics
