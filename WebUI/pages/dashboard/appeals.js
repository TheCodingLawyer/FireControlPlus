import PlayerAppeals from '../../components/dashboard/PlayerAppeals'
import DefaultLayout from '../../components/DefaultLayout'
import Loader from '../../components/Loader'
import PageContainer from '../../components/PageContainer'
import PageHeader from '../../components/PageHeader'
import { useUser } from '../../utils'

export default function Page () {
  const { user, hasPermission } = useUser({ redirectTo: '/login', redirectIfFound: false })

  if (!user) return <Loader />

  // Check if user has permission to view appeals
  const canViewAppeals = hasPermission('player.appeals', 'view.any') || hasPermission('player.appeals', 'view.assigned')
  
  if (!canViewAppeals) {
    return (
      <DefaultLayout title='Appeals | Dashboard'>
        <PageContainer>
          <PageHeader title='Dashboard' />
          <div className='text-center py-8'>
            <p className='text-lg text-gray-300'>You don't have permission to view appeals.</p>
            <p className='text-sm text-gray-400 mt-2'>Contact an administrator if you believe this is an error.</p>
          </div>
        </PageContainer>
      </DefaultLayout>
    )
  }

  return (
    <DefaultLayout title='Appeals | Dashboard'>
      <PageContainer>
        <PageHeader title='Dashboard' />
        <PlayerAppeals title='Appeals' showActor />
      </PageContainer>
    </DefaultLayout>
  )
}
