import { AiOutlineWarning } from 'react-icons/ai'
import { BsMicMute } from 'react-icons/bs'
import { FaBan, FaSignOutAlt, FaPencilAlt } from 'react-icons/fa'
import { useUser } from '../../utils'
import Link from 'next/link'
import Button from '../Button'
import Input from '../Input'
import { useState } from 'react'
import { useApi, useMutateApi } from '../../utils'
import Modal from '../Modal'

export default function PlayerActions ({ id }) {
	const { hasServerPermission, hasPermission } = useUser()
	const canCreateBan = hasServerPermission('player.bans', 'create', null, true) || hasPermission('player.bans', 'create')
	const canCreateMute = hasServerPermission('player.mutes', 'create', null, true) || hasPermission('player.mutes', 'create')
	const canCreateWarning = hasServerPermission('player.warnings', 'create', null, true) || hasPermission('player.warnings', 'create')
	const canCreateKick = hasServerPermission('player.kicks', 'create', null, true) || hasPermission('player.kicks', 'create')

	const [kickOpen, setKickOpen] = useState(false)
	const [reason, setReason] = useState('')
	const { data: serversData } = useApi({ query: `query { servers { id name } }` })
	const kickMutation = `mutation createPlayerKick($input: CreatePlayerKickInput!) { createPlayerKick(input: $input) { id player { id name } actor { id name } reason created } }`
	const { load: kickPlayer, loading: kicking } = useMutateApi({ query: kickMutation })

	const onGlobalKick = async () => {
		if (!reason || !canCreateKick) return
		
		try {
			const servers = serversData?.servers || []
			const allowed = servers.filter(s => hasServerPermission('player.kicks', 'create', s.id) || hasPermission('player.kicks', 'create'))
			
			for (const s of allowed) {
				await kickPlayer({ input: { player: id, server: s.id, reason } })
			}
			
			setReason('')
			setKickOpen(false)
		} catch (error) {
			console.error('Failed to kick player:', error)
		}
	}

	if (!canCreateBan && !canCreateMute && !canCreateWarning && !canCreateKick) return null

	return (
		<>
			<div className='flex items-center justify-center gap-3 pt-4'>
				{canCreateBan && (
						<Link href={`/player/${id}/ban`} passHref>
						<div className='group'>
							<Button className='btn-outline flex items-center justify-center rounded-full transition-all duration-500 ease-out w-12 h-12 group-hover:w-20 group-hover:px-2 group-hover:pr-3 overflow-hidden'>
								<FaBan className='text-red-800 text-lg flex-shrink-0' />
								<span className='w-0 opacity-0 ml-0 overflow-hidden transition-all duration-500 ease-out group-hover:w-8 group-hover:opacity-100 group-hover:ml-0.5 whitespace-nowrap text-sm'>
									Ban
								</span>
							</Button>
				</div>
						</Link>
					)}
				{canCreateMute && (
						<Link href={`/player/${id}/mute`} passHref>
						<div className='group'>
							<Button className='btn-outline flex items-center justify-center rounded-full transition-all duration-500 ease-out w-12 h-12 group-hover:w-20 group-hover:px-2 group-hover:pr-3 overflow-hidden'>
								<BsMicMute className='text-indigo-800 text-lg flex-shrink-0' />
								<span className='w-0 opacity-0 ml-0 overflow-hidden transition-all duration-500 ease-out group-hover:w-8 group-hover:opacity-100 group-hover:ml-0.5 whitespace-nowrap text-sm'>
									Mute
								</span>
							</Button>
				</div>
						</Link>
					)}
				{canCreateKick && (
					<div className='group'>
						<Button 
							className='btn-outline flex items-center justify-center rounded-full transition-all duration-500 ease-out w-12 h-12 group-hover:w-20 group-hover:px-2 group-hover:pr-3 overflow-hidden' 
							onClick={() => setKickOpen(true)}
						>
							<FaSignOutAlt className='text-orange-800 text-lg flex-shrink-0' />
							<span className='w-0 opacity-0 ml-0 overflow-hidden transition-all duration-500 ease-out group-hover:w-8 group-hover:opacity-100 group-hover:ml-0.5 whitespace-nowrap text-sm'>
								Kick
							</span>
						</Button>
				</div>
				)}
				{canCreateWarning && (
						<Link href={`/player/${id}/warn`} passHref>
						<div className='group'>
							<Button className='btn-outline flex items-center justify-center rounded-full transition-all duration-500 ease-out w-12 h-12 group-hover:w-20 group-hover:px-2 group-hover:pr-3 overflow-hidden'>
								<AiOutlineWarning className='text-amber-800 text-lg flex-shrink-0' />
								<span className='w-0 opacity-0 ml-0 overflow-hidden transition-all duration-500 ease-out group-hover:w-8 group-hover:opacity-100 group-hover:ml-0.5 whitespace-nowrap text-sm'>
									Warn
								</span>
							</Button>
				</div>
						</Link>
					)}
			</div>

			<Modal
				open={kickOpen}
				title='Kick'
				cancelButton='Close'
				confirmButton='Kick'
				confirmDisabled={!reason}
				loading={kicking}
				onCancel={() => setKickOpen(false)}
				onConfirm={onGlobalKick}
			>
				<div className='space-y-4'>
					<p className='text-sm opacity-80'>This will issue a kick on all servers you have permission for.</p>
					<Input
						icon={<FaPencilAlt />}
						placeholder='Enter reason'
						value={reason}
						onChange={(e) => setReason(e.target.value)}
						required
					/>
				</div>
			</Modal>
		</>
	)
}
