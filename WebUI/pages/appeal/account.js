import { useRouter } from 'next/router'
import DefaultLayout from '../../components/DefaultLayout'
import PageContainer from '../../components/PageContainer'
import PlayerLoginPasswordForm from '../../components/PlayerLoginPasswordForm'
import Panel from '../../components/Panel'
import AppealStepHeader from '../../components/appeal/AppealStepHeader'

function Page () {
  const router = useRouter()
  const onSuccess = () => {
    router.push('/appeal/punishment')
  }

  return (
    <DefaultLayout title='Login | Appeal'>
      <PageContainer>
        <Panel className='mx-auto w-full max-w-md'>
          <AppealStepHeader step={1} title='Account Login' nextStep='Select Punishment' />
          <PlayerLoginPasswordForm onSuccess={onSuccess} showForgotPassword />
        </Panel>
      </PageContainer>
    </DefaultLayout>
  )
}

export default Page
