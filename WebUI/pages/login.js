import { useRouter } from 'next/router'
import Link from 'next/link'
import DefaultLayout from '../components/DefaultLayout'
import PageContainer from '../components/PageContainer'
import PlayerLoginPasswordForm from '../components/PlayerLoginPasswordForm'
import PageHeader from '../components/PageHeader'
import Panel from '../components/Panel'
import Button from '../components/Button'
import { useUser } from '../utils'
import { MdPin } from 'react-icons/md'

function Page () {
  const router = useRouter()
  const { user } = useUser({ redirectIfFound: true, redirectTo: '/dashboard' })
  const onSuccess = () => {
    router.push('/dashboard')
  }

  return (
    <DefaultLayout title='Login' loading={user}>
      <PageContainer>
        <Panel className='mx-auto w-full max-w-md'>
          <PageHeader title='Login' subTitle='Welcome back' />
          
          {/* PIN Login Option - Prominent */}
          <div className='mb-6 p-4 bg-orange-500/20 border border-orange-500/30 rounded-lg'>
            <div className='text-center'>
              <p className='text-orange-200 mb-3'>
                <strong>New user?</strong> Login with your in-game PIN first
              </p>
              <Link href='/forgotten-password' passHref>
                <Button className='bg-primary-900 hover:bg-primary-800 font-semibold'>
                  <MdPin className='mr-2' />
                  Login with In-Game PIN
                </Button>
              </Link>
              <p className='text-orange-200 text-xs mt-2'>
                Can't join the server? <Link href='/get-pin' className='underline hover:text-orange-100'>Get PIN here</Link>
              </p>
            </div>
          </div>

          {/* Divider */}
          <div className='flex items-center mb-6'>
            <div className='flex-grow border-t border-gray-600'></div>
            <span className='px-3 text-gray-400 text-sm'>or</span>
            <div className='flex-grow border-t border-gray-600'></div>
          </div>

          <PlayerLoginPasswordForm onSuccess={onSuccess} showForgotPassword />
        </Panel>
      </PageContainer>
    </DefaultLayout>
  )
}

export default Page
