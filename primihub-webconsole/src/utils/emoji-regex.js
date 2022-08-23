const emojiRegex = require('emoji-regex')

export function matchEmoji(text) {
  const regex = emojiRegex()
  // length>0: 匹配到 null:未匹配到
  return text.match(regex)
}

export function encodeEmoji(str) {
  const regex = emojiRegex()
  return str.replace(regex, p => `emoji(${p.codePointAt(0)})`)
}

export function deCodeEmoji(str) {
  const emojiDecodeRegex = /emoji\(\d+\)/g
  return str.replace(emojiDecodeRegex, p => {
    const filterP = p.replace(/[^\d]/g, '')
    return String.fromCodePoint(filterP)
  })
}
