import request from '@/utils/request'

export function getAuthList() {
  return request({
    url: '/sys/oauth/getAuthList',
    method: 'GET'
  })
}
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

export function authLogin(data) {
  return request({
    url: '/sys/oauth/authLogin',
    method: 'POST',
    data
  })
}
export function register(data) {
  return request({
    url: '/sys/user/register',
    method: 'POST',
    data
  })
}
export function forgetPassword(data) {
  return request({
    url: '/sys/user/forgetPassword',
    method: 'POST',
    data
  })
}
export function updatePassword(data) {
  return request({
    url: '/sys/user/updatePassword',
    method: 'POST',
    data
  })
}
export function sendVerificationCode(data) {
  return request({
    url: '/sys/user/sendVerificationCode',
    method: 'POST',
    showLoading: false,
    data
  })
}

export function authRegister(data) {
  return request({
    url: '/sys/oauth/authRegister',
    method: 'POST',
    showLoading: false,
    data
  })
}
export function changeUserAccount(data) {
  return request({
    url: '/sys/user/changeUserAccount',
    method: 'POST',
    data
  })
}
export function relieveUserAccount(data) {
  return request({
    url: '/sys/user/relieveUserAccount',
    method: 'POST',
    data
  })
}
export function getCaptcha(data) {
  return request({
    url: '/sys/captcha/get',
    method: 'POST',
    type: 'json',
    showLoading: false,
    data
  })
}
export function checkCaptcha(data) {
  return request({
    url: '/sys/captcha/check',
    method: 'POST',
    type: 'json',
    data
  })
}

export function logout() {
  return Promise.resolve({})
}
