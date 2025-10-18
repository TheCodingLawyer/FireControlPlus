import PlayerAppeals from '../../components/dashboard/PlayerAppeals'
import DefaultLayout from '../../components/DefaultLayout'
import Loader from '../../components/Loader'
import PageContainer from '../../components/PageContainer'
import PageHeader from '../../components/PageHeader'
import { useUser } from '../../utils'

export default function Page () {
  // TEMPORARILY DISABLED AUTH: No redirect, no permission checks
  const { user } = useUser()

  if (!user) return <Loader />

  // TEMPORARILY DISABLED AUTH: All permission checks removed
  
  return (
    <DefaultLayout title='Appeals | Dashboard'>
      <PageContainer>
        <PageHeader title='Dashboard' />
        <PlayerAppeals title='Appeals' showActor />
      </PageContainer>
    </DefaultLayout>
  )
}
