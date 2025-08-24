const geyserPrefix = process.env.GEYSER_PREFIX || '.'

export default function BedrockIcon ({ name, prefix = geyserPrefix, className = '', sizeClass = 'h-5 w-5 md:h-6 md:w-6' }) {
  if (!name) return null
  const isBedrock = prefix && name.startsWith(prefix)
  if (!isBedrock) return null

  const src = process.env.NEXT_PUBLIC_BEDROCK_ICON_PATH || '/images/bedrock.png'

  return (
    <img
      src={src}
      alt='Bedrock'
      title='Bedrock Edition'
      className={`ml-2 inline-block ${sizeClass} ${className}`}
    />
  )
} 