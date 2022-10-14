import emojiRegex from 'emoji-regex'

export function matchEmoji(text) {
  const regex = emojiRegex()
  text = typeof text === 'string' ? text.toString() : text
  // length>0: 匹配到 null:未匹配到
  return text.match(regex)
}

export function encodeEmoji(str) {
  const regex = emojiRegex()
  str = typeof str === 'string' ? str.toString() : str
  return str.replace(regex, p => `emoji(${p.codePointAt(0)})`)
}

export function deCodeEmoji(str) {
  const emojiDecodeRegex = /emoji\(\d+\)/g
  return str.replace(emojiDecodeRegex, p => {
    const filterP = p.replace(/[^\d]/g, '')
    return String.fromCodePoint(filterP)
  })
}
