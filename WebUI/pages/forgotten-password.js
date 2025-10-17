import { useRouter } from 'next/router'
import DefaultLayout from '../components/DefaultLayout'
import PageContainer from '../components/PageContainer'
import PlayerLoginPinForm from '../components/PlayerLoginPinForm'
import { useUser } from '../utils'
import PageHeader from '../components/PageHeader'
import Panel from '../components/Panel'

function Page () {
  const router = useRouter()
  const { user } = useUser({ redirectIfFound: true, redirectTo: '/dashboard' })
  const onSuccess = ({ responseData }) => {
    if (responseData.hasAccount) return router.push('/account/password')

    router.push('/dashboard')
  }

  return (
    <DefaultLayout title='Login with PIN' loading={user}>
      <PageContainer>
        <Panel className='mx-auto w-full max-w-md'>
          <PageHeader title='Login with In-Game PIN' subTitle='From Minecraft Server' />
          <div className='mb-6 p-4 bg-blue-500/20 border border-blue-500/30 rounded-lg'>
            <p className='text-blue-200 text-sm'>
              <strong>How to get your PIN:</strong> Join the Minecraft server and use the <code className='bg-primary-900 px-1 rounded'>/bmpin</code> command, or check your ban screen if you're banned.
            </p>
          </div>
          <PlayerLoginPinForm onSuccess={onSuccess} showHint />
        </Panel>
      </PageContainer>
    </DefaultLayout>
  )
}

export default Page
