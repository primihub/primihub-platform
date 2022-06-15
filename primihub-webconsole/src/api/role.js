import request from '@/utils/request'

export function getRoles(params) {
  return request({
    url: '/sys/role/findRolePage',
    method: 'get',
    showLoading: false,
    params
  })
}

export function saveOrUpdateRole(data) {
  return request({
    url: '/sys/role/saveOrUpdateRole',
    method: 'post',
    data
  })
}

export function deleteRole(data) {
  return request({
    url: '/sys/role/deleteSysRole',
    method: 'post',
    data
  })
}

export function getAuthTree(params) {
  return request({
    url: '/sys/auth/getAuthTree',
    method: 'get',
    params
  })
}

export function getRoleTree(params) {
  return request({
    url: '/sys/role/getRoleAuthTree',
    method: 'get',
    params
  })
}
