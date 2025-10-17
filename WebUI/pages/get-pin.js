import { useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { AiOutlineUser } from 'react-icons/ai'
import { MdPin } from 'react-icons/md'
import DefaultLayout from '../components/DefaultLayout'
import PageContainer from '../components/PageContainer'
import PageHeader from '../components/PageHeader'
import Panel from '../components/Panel'
import Input from '../components/Input'
import Button from '../components/Button'
import ServerSelector from '../components/admin/ServerSelector'
import { useUser } from '../utils'

export default function Page () {
  const { user } = useUser({ redirectIfFound: true, redirectTo: '/dashboard' })
  const [pin, setPin] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const { handleSubmit, formState, register, control } = useForm()

  const onSubmit = async (data) => {
    setLoading(true)
    setError(null)
    setPin(null)

    try {
      const response = await fetch((process.env.BASE_PATH || '') + '/api/generate-pin', {
        method: 'POST',
        body: JSON.stringify(data),
        headers: new Headers({ 'Content-Type': 'application/json' }),
        credentials: 'include'
      })

      if (response.status !== 200) {
        const responseData = await response.json()
        throw new Error(responseData.error)
      }

      const responseData = await response.json()
      setPin(responseData.pin)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <DefaultLayout title='Get PIN' loading={user}>
      <PageContainer>
        <Panel className='mx-auto w-full max-w-md'>
          <PageHeader title='Get Login PIN' subTitle='For banned or new players' />
          
          <div className='mb-6 p-4 bg-blue-500/20 border border-blue-500/30 rounded-lg'>
            <p className='text-blue-200 text-sm'>
              <strong>Can't join the server?</strong> Get your login PIN here to access the web interface and submit appeals.
            </p>
          </div>

          {!pin ? (
            <form onSubmit={handleSubmit(onSubmit)} className='mx-auto'>
              <div className='flex flex-col relative w-full'>
                <Controller
                  name='serverId'
                  control={control}
                  defaultValue={false}
                  rules={{ required: true }}
                  render={({ field }) => <ServerSelector className='mb-6' {...field} />}
                />
                
                <Input
                  required
                  label='Minecraft Username'
                  icon={<AiOutlineUser />}
                  iconPosition='left'
                  error={error}
                  {...register('name')}
                />
                
                <Button disabled={loading} loading={loading}>
                  Generate PIN
                </Button>
              </div>
            </form>
          ) : (
            <div className='text-center'>
              <div className='mb-6 p-6 bg-green-500/20 border border-green-500/30 rounded-lg'>
                <div className='flex items-center justify-center mb-4'>
                  <MdPin className='text-4xl text-green-400' />
                </div>
                <p className='text-green-200 mb-2'>
                  <strong>Your PIN is:</strong>
                </p>
                <div className='text-3xl font-mono font-bold text-green-100 mb-4'>
                  {pin}
                </div>
                <p className='text-green-200 text-sm'>
                  This PIN expires in 5 minutes. Use it to login to the web interface.
                </p>
              </div>
              
              <Button onClick={() => { setPin(null); setError(null) }}>
                Generate New PIN
              </Button>
            </div>
          )}
        </Panel>
      </PageContainer>
    </DefaultLayout>
  )
}
