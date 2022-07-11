import Cookies from 'js-cookie'
import md5 from 'js-md5'
const TokenKey = `${md5(window.location.host)}_primihub_token`

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}
