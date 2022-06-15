import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/sys/user/findUserPage',
    method: 'get',
    params
  })
}

export function saveOrUpdateUser(data) {
  return request({
    url: '/sys/user/saveOrUpdateUser',
    method: 'post',
    data
  })
}

export function deleteUser(data) {
  return request({
    url: '/sys/user/deleteSysUser',
    method: 'post',
    data
  })
}
export function initPassword(data) {
  return request({
    url: '/sys/user/initPassword',
    method: 'post',
    data
  })
}
