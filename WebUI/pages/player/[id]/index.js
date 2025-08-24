import { NextSeo } from 'next-seo'
import { getApiRoot } from 'nextjs-url'
import DefaultLayout from '../../../components/DefaultLayout'
import { useUser } from '../../../utils'

import PlayerAlts from '../../../components/player/PlayerAlts'
import PlayerAvatar from '../../../components/player/PlayerAvatar'
import ActivePlayerBans from '../../../components/player/ActivePlayerBans'
import ActivePlayerMutes from '../../../components/player/ActivePlayerMutes'

import PlayerStatistics from '../../../components/player/PlayerStatistics'
import PlayerNotes from '../../../components/player/PlayerNotes'
import PlayerWarnings from '../../../components/player/PlayerWarnings'
import PlayerBans from '../../../components/player/PlayerBans'
import PlayerMutes from '../../../components/player/PlayerMutes'
import PlayerKicks from '../../../components/PlayerKicks'
import PlayerHistoryList from '../../../components/player/PlayerHistoryList'
import PageContainer from '../../../components/PageContainer'
import PlayerActions from '../../../components/player/PlayerActions'
import PlayerLastSeen from '../../../components/player/PlayerLastSeen'
import EditionIcon from '../../../components/player/EditionIcon'
export async function getServerSideProps ({ req, params }) {
  const HEX_UUID_REGEX = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
  const { parse, unparse } = require('uuid-parse')

  // Normalize Bedrock UUIDs without hyphens
  const rawId = params.id && params.id.length === 32
    ? params.id.replace(/(.{8})(.{4})(.{4})(.{4})(.{12})/, '$1-$2-$3-$4-$5')
    : params.id

  if (!HEX_UUID_REGEX.test(rawId)) return { notFound: true }

  const id = parse(rawId, Buffer.alloc(16))
  const data = await req.loaders.player.load({ id, fields: ['id', 'name', 'lastSeen'] })

  if (!data) return { notFound: true }

  const player = {
    ...data,
    id: unparse(data.id)
  }
  const baseUrl = getApiRoot(req).href

  return {
    props: {
      data: {
        player,
        ogImage: `${baseUrl}/opengraph/player/${player.id}`
      }
    }
  }
}

export default function Page ({ data }) {
  const { hasServerPermission } = useUser()

  return (
    <>
      <NextSeo
        openGraph={{
          title: data.player.name,
          images: [
            {
              url: data.ogImage,
              width: 1200,
              height: 600,
              alt: `${data.player.name} profile`
            }
          ]
        }}
        twitter={{
          cardType: 'summary_large_image'
        }}
      />
      <DefaultLayout title={data.player.name}>
        <PageContainer>
          <div className='grid lg:grid-cols-12 gap-6 lg:gap-12'>
            <div className='col-span-12 lg:col-span-3 flex flex-col gap-4'>
              <PlayerAvatar id={data.player.id} />
              <div className='flex flex-col justify-start justify-items-stretch w-full border-b border-primary-900 pb-6'>
                <h1 className='text-4xl font-bold pb-2 leading-none text-center'>
                  <span>{data.player.name}</span>
                  <EditionIcon
                    name={data.player.name}
                    bedrockSizeClass='h-8 w-8 md:h-9 md:w-9'
                    javaSizeClass='h-8 w-8 md:h-10 md:w-10'
                    javaClassName='ml-1 -translate-y-[1px] align-baseline'
                    bedrockClassName='ml-2 -translate-y-[1.5px] align-baseline'
                  />
                </h1>
                {hasServerPermission('player.history', 'view', null, true) ? <PlayerHistoryList id={data.player.id} lastSeen={data.player.lastSeen} /> : <PlayerLastSeen lastSeen={data.player.lastSeen} />}
                {hasServerPermission('player.alts', 'view', null, true) && <PlayerAlts id={data.player.id} />}
                <PlayerActions id={data.player.id} />
              </div>
              <PlayerStatistics id={data.player.id} />
              <ActivePlayerBans id={data.player.id} />
              <ActivePlayerMutes id={data.player.id} />
              {hasServerPermission('player.notes', 'view', null, true) && <PlayerNotes id={data.player.id} />}
            </div>
            <div className='col-span-12 lg:col-span-9'>
              <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
                {hasServerPermission('player.bans', 'view', null, true) && <PlayerBans id={data.player.id} />}
                {hasServerPermission('player.mutes', 'view', null, true) && <PlayerMutes id={data.player.id} />}
                {hasServerPermission('player.kicks', 'view', null, true) && <PlayerKicks id={data.player.id} />}
                {hasServerPermission('player.warnings', 'view', null, true) && <PlayerWarnings id={data.player.id} />}
              </div>
            </div>
          </div>
        </PageContainer>
      </DefaultLayout>
    </>
  )
}
