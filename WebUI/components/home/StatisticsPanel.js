import { FaBan } from 'react-icons/fa'
import { BsMicMute } from 'react-icons/bs'
import { AiOutlineWarning } from 'react-icons/ai'
import { FaSignOutAlt } from 'react-icons/fa'
import Loader from '../Loader'
import { numberFormatter, useApi } from '../../utils'

const StatisticsPanel = () => {
  const { data, loading } = useApi({
    query: `query statistics {
    statistics {
      totalActiveBans
      totalActiveMutes
      totalKicks
      totalWarnings
    }
  }`
  }, { refreshInterval: 5000 })

  const totalBans = (data?.statistics?.totalActiveBans || 0)
  const totalMutes = (data?.statistics?.totalActiveMutes || 0)
  const totalKicks = (data?.statistics?.totalKicks || 0)
  const totalWarnings = (data?.statistics?.totalWarnings || 0)

  return (
    <div className='px-5 py-12 grid grid-flow-col grid-cols-2 grid-rows-2 xl:grid-cols-4 xl:grid-rows-1 gap-4 -m-4 text-center'>
      {/* Warnings */}
      <div className='p-4'>
        <div className='px-4 py-6 rounded-3xl border-primary-900'>
          {loading && <Loader />}
          {data &&
            <>
              <AiOutlineWarning className='text-amber-800 w-12 h-12 mb-3 inline-block' />
              <h2 className='title-font font-medium text-3xl'>{numberFormatter(totalWarnings || 0)}</h2>
              <p className='leading-relaxed text-gray-300'>Warning{totalWarnings !== 1 && 's'}</p>
            </>}
        </div>
      </div>

      {/* Mutes */}
      <div className='p-4'>
        <div className='px-4 py-6 rounded-3xl border-primary-900'>
          {loading && <Loader />}
          {data &&
            <>
              <BsMicMute className='text-indigo-800 w-12 h-12 mb-3 inline-block' />
              <h2 className='title-font font-medium text-3xl'>{numberFormatter(totalMutes || 0)}</h2>
              <p className='leading-relaxed text-gray-300'>Mute{totalMutes !== 1 && 's'}</p>
            </>}
        </div>
      </div>

      {/* Kicks */}
      <div className='p-4'>
        <div className='px-4 py-6 rounded-3xl border-primary-900'>
          {loading && <Loader />}
          {data &&
            <>
              <FaSignOutAlt className='text-orange-800 w-12 h-12 mb-3 inline-block' />
              <h2 className='title-font font-medium text-3xl'>{numberFormatter(totalKicks || 0)}</h2>
              <p className='leading-relaxed text-gray-300'>Kick{totalKicks !== 1 && 's'}</p>
            </>}
        </div>
      </div>

      {/* Bans */}
      <div className='p-4'>
        <div className='px-4 py-6 rounded-3xl border-primary-900'>
          {loading && <Loader />}
          {data &&
            <>
              <FaBan className='text-red-800 w-12 h-12 mb-3 inline-block' />
              <h2 className='title-font font-medium text-3xl'>{numberFormatter(totalBans || 0)}</h2>
              <p className='leading-relaxed text-gray-300'>Ban{totalBans !== 1 && 's'}</p>
            </>}
        </div>
      </div>
    </div>
  )
}

export default StatisticsPanel
