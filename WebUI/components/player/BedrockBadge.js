const geyserPrefix = process.env.GEYSER_PREFIX || '.'

export default function BedrockBadge ({ name, prefix = geyserPrefix }) {
  if (!name) return null
  const isBedrock = prefix && name.startsWith(prefix)
  if (!isBedrock) return null

  return (
    <span className='ml-2 inline-flex items-center rounded-full bg-emerald-900/60 px-2 py-0.5 text-xs font-medium text-emerald-200 border border-emerald-700'>
      Bedrock
    </span>
  )
} 