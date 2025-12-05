import DefaultLayout from '../../../components/DefaultLayout'
import PageContainer from '../../../components/PageContainer'
import Panel from '../../../components/Panel'
import AppealStepHeader from '../../../components/appeal/AppealStepHeader'
import PunishmentPicker from '../../../components/appeal/PunishmentPicker'

function Page () {
  return (
    <DefaultLayout title='Select Punishment | Appeal'>
      <PageContainer>
        <Panel className='mx-auto w-full max-w-md'>
          <AppealStepHeader step={2} title='Select Punishment' nextStep='Write Appeal' />
          <PunishmentPicker />
        </Panel>
      </PageContainer>
    </DefaultLayout>
  )
}

export default Page
