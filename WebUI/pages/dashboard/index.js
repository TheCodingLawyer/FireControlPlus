import ActivePunishments from '../../components/dashboard/ActivePunishments'
import PlayerAppeals from '../../components/dashboard/PlayerAppeals'
import PlayerReports from '../../components/dashboard/PlayerReports'
import DefaultLayout from '../../components/DefaultLayout'
import Loader from '../../components/Loader'
import PageContainer from '../../components/PageContainer'
import PageHeader from '../../components/PageHeader'
import PlayerStatistics from '../../components/player/PlayerStatistics'
import { useUser } from '../../utils'

export default function Page () {
  const { user, hasPermission } = useUser({ redirectTo: '/login', redirectIfFound: false })

  if (!user) return <Loader />

  // Check if user has permission to view dashboard (reports or appeals)
  const canViewReports = hasPermission('player.reports', 'view.any') || hasPermission('player.reports', 'view.assigned')
  const canViewAppeals = hasPermission('player.appeals', 'view.any') || hasPermission('player.appeals', 'view.assigned')
  
  if (!canViewReports && !canViewAppeals) {
    return (
      <DefaultLayout title='Dashboard'>
        <PageContainer>
          <PageHeader title='Dashboard' />
          <div className='text-center py-8'>
            <p className='text-lg text-gray-300'>You don't have permission to access the dashboard.</p>
            <p className='text-sm text-gray-400 mt-2'>Contact an administrator if you believe this is an error.</p>
          </div>
        </PageContainer>
      </DefaultLayout>
    )
  }

  return (
    <DefaultLayout title='Dashboard'>
      <PageContainer>
        <PageHeader title='Dashboard' />
        <div className='space-y-10'>
          <PlayerStatistics id={user.id} />
          <div>
            <h2 className='text-lg font-bold pb-4 border-b border-accent-200 leading-none'>Active Punishments</h2>
            <ActivePunishments id={user.id} />
          </div>
          {canViewAppeals && (
            <div>
              <PlayerAppeals id={user.id} title='Your appeals' />
            </div>
          )}
          {canViewReports && (
            <div>
              <PlayerReports id={user.id} title='Your reports' />
            </div>
          )}
        </div>
      </PageContainer>
    </DefaultLayout>
  )
}
