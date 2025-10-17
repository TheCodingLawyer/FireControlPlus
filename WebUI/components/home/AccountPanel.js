import { mutate } from 'swr'
import Link from 'next/link'
import PlayerLoginPasswordForm from '../PlayerLoginPasswordForm'
import Avatar from '../Avatar'
import Button from '../Button'
import { useUser } from '../../utils'
import PageHeader from '../PageHeader'
import Panel from '../Panel'
import { MdPin } from 'react-icons/md'

const AccountPanel = () => {
  const { user } = useUser()
  const handleLogin = () => {
    mutate('/api/user')
  }

  return (
    <Panel>
      <PageHeader title={user ? user.name : 'Sign In'} subTitle={user ? 'My Account' : 'Already have an account?'} />
      <div className='flex items-center'>
        {user
          ? (
            <div className='flex flex-col items-center w-full gap-2'>
              <Avatar type='body' height='148' width='66' uuid={user.id} />
              <Link href='/dashboard' passHref>
                <Button>My Dashboard</Button>
              </Link>
            </div>
            )
          : (
            <div className='w-full'>
              {/* PIN Login Option */}
              <div className='mb-4 text-center'>
                <Link href='/forgotten-password' passHref>
                  <Button className='bg-primary-900 hover:bg-primary-800 font-semibold w-full'>
                    <MdPin className='mr-2' />
                    Login with In-Game PIN
                  </Button>
                </Link>
                <p className='text-gray-400 text-xs mt-2'>
                  Can't join the server? <Link href='/get-pin' className='underline hover:text-gray-300'>Get PIN here</Link>
                </p>
              </div>
              
              {/* Divider */}
              <div className='flex items-center mb-4'>
                <div className='flex-grow border-t border-gray-600'></div>
                <span className='px-2 text-gray-400 text-xs'>or</span>
                <div className='flex-grow border-t border-gray-600'></div>
              </div>

              <PlayerLoginPasswordForm onSuccess={handleLogin} showForgotPassword />
            </div>
            )}
      </div>
    </Panel>
  )
}

export default AccountPanel
