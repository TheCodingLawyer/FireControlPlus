import DefaultLayout from '../../components/DefaultLayout'
import PageContainer from '../../components/PageContainer'
import { useUser } from '../../utils'
import Loader from '../../components/Loader'
import { useRouter } from 'next/router'
import Link from 'next/link'
import AllPlayerBans from '../../components/admin/AllPlayerBans'
import AllPlayerMutes from '../../components/admin/AllPlayerMutes'
import AllPlayerWarnings from '../../components/admin/AllPlayerWarnings'
import AllPlayerKicks from '../../components/admin/AllPlayerKicks'
import PlayerAppeals from '../../components/dashboard/PlayerAppeals'
import PlayerReports from '../../components/dashboard/PlayerReports'
import { FaBan, FaSignOutAlt, FaFlag, FaGavel } from 'react-icons/fa'
import { BsMicMute } from 'react-icons/bs'
import { AiOutlineWarning } from 'react-icons/ai'

const tabs = [
  { id: 'bans', name: 'Bans', Icon: FaBan },
  { id: 'mutes', name: 'Mutes', Icon: BsMicMute },
  { id: 'warnings', name: 'Warnings', Icon: AiOutlineWarning },
  { id: 'kicks', name: 'Kicks', Icon: FaSignOutAlt },
  { id: 'reports', name: 'Reports', Icon: FaFlag },
  { id: 'appeals', name: 'Appeals', Icon: FaGavel }
]

export default function Page () {
  const router = useRouter()
  const activeTab = (router.query.tab || 'bans') + ''
  const { user } = useUser({ redirectTo: '/login', redirectIfFound: false })

  if (!user) return <Loader />

  return (
    <DefaultLayout title='Punishments'>
      <PageContainer>
        <nav className='flex flex-wrap gap-2 border-b border-primary-900 mb-6 pb-2'>
          {tabs.map(t => {
            const isActive = activeTab === t.id
            const Icon = t.Icon
            return (
              <Link key={t.id} href={{ pathname: '/dashboard/punishments', query: { tab: t.id } }}>
                <span className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full cursor-pointer transition-colors ${isActive ? 'bg-accent-600 text-white' : 'bg-primary-900/40 text-gray-300 hover:bg-primary-900 hover:text-white'}`}>
                  <Icon className='w-4 h-4' />
                  <span className='text-sm font-medium'>{t.name}</span>
                </span>
              </Link>
            )
          })}
        </nav>
        <div className='space-y-10'>
          {(activeTab === 'bans') && (
            <div id='bans'>
              <AllPlayerBans />
            </div>
          )}
          {(activeTab === 'mutes') && (
            <div id='mutes'>
              <AllPlayerMutes />
            </div>
          )}
          {(activeTab === 'warnings') && (
            <div id='warnings'>
              <AllPlayerWarnings />
            </div>
          )}
          {(activeTab === 'kicks') && (
            <div id='kicks'>
              <AllPlayerKicks />
            </div>
          )}
          {(activeTab === 'reports') && (
            <div id='reports'>
              <PlayerReports title='Reports' />
            </div>
          )}
          {(activeTab === 'appeals') && (
            <div id='appeals'>
              <PlayerAppeals title='Appeals' />
            </div>
          )}
        </div>
      </PageContainer>
    </DefaultLayout>
  )
} 