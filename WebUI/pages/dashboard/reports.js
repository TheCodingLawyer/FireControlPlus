import PlayerReports from '../../components/dashboard/PlayerReports'
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
    <DefaultLayout title='Reports | Dashboard'>
      <PageContainer>
        <PageHeader title='Dashboard' />
        <PlayerReports title='Reports' />
      </PageContainer>
    </DefaultLayout>
  )
}
