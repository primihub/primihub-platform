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

export function logout() {
  return Promise.resolve({})
}
