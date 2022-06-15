import request from '@/utils/request'

export function getValidatePublicKey() {
  return request({
    url: '/sys/common/getValidatePublicKey?',
    method: 'GET',
    showLoading: false
  })
}

export function login(data) {
  return request({
    url: '/sys/user/login',
    method: 'POST',
    data
  })
}

// export function getInfo(token) {
//   return Promise.resolve({ 'code': 0, 'data': { 'roles': ['admin'], 'introduction': 'I am a super administrator', 'avatar': 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif', 'name': 'Super Admin' }})
// }

export function logout() {
  return Promise.resolve({})
}
