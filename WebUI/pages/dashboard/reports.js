import PlayerReports from '../../components/dashboard/PlayerReports'
import DefaultLayout from '../../components/DefaultLayout'
import Loader from '../../components/Loader'
import PageContainer from '../../components/PageContainer'
import PageHeader from '../../components/PageHeader'
import { useUser } from '../../utils'

export default function Page () {
  const { user, hasPermission } = useUser({ redirectTo: '/login', redirectIfFound: false })

  if (!user) return <Loader />

  // Check if user has permission to view reports
  const canViewReports = hasPermission('player.reports', 'view.any') || hasPermission('player.reports', 'view.assigned')
  
  if (!canViewReports) {
    return (
      <DefaultLayout title='Reports | Dashboard'>
        <PageContainer>
          <PageHeader title='Dashboard' />
          <div className='text-center py-8'>
            <p className='text-lg text-gray-300'>You don't have permission to view reports.</p>
            <p className='text-sm text-gray-400 mt-2'>Contact an administrator if you believe this is an error.</p>
          </div>
        </PageContainer>
      </DefaultLayout>
    )
  }

  return (
    <DefaultLayout title='Reports | Dashboard'>
      <PageContainer>
        <PageHeader title='Dashboard' />
        <PlayerReports title='Reports' />
      </PageContainer>
    </DefaultLayout>
  )
}
