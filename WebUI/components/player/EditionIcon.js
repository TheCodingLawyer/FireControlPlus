import BedrockIcon from './BedrockIcon'

const geyserPrefix = process.env.GEYSER_PREFIX || '.'

export default function EditionIcon ({
  name,
  prefix = geyserPrefix,
  className = '',
  sizeClass = 'h-5 w-5 md:h-6 md:w-6',
  bedrockSizeClass,
  javaSizeClass,
  bedrockClassName = 'ml-2',
  javaClassName = 'ml-2'
}) {
  if (!name) return null
  const isBedrock = prefix && name.startsWith(prefix)

  if (isBedrock) {
    const finalSize = bedrockSizeClass || sizeClass
    const finalClassName = `${bedrockClassName} ${className}`.trim()
    return <BedrockIcon name={name} className={finalClassName} sizeClass={finalSize} />
  }

  const javaSrc = process.env.NEXT_PUBLIC_JAVA_ICON_PATH || '/images/java.png'
  const finalSize = javaSizeClass || sizeClass
  const finalClassName = `${javaClassName} ${className}`.trim()

  return (
    <span className={`inline-flex items-center ${finalClassName}`} title='Java Edition'>
      <img src={javaSrc} alt='Java' className={`${finalSize} align-baseline`} />
    </span>
  )
} 