import request from '@/utils/request'

export function getLocalOrganInfo(params) {
  return request({
    url: '/sys/organ/getLocalOrganInfo',
    method: 'get',
    params
  })
}

export function changeLocalOrganInfo(data) {
  return request({
    url: '/sys/organ/changeLocalOrganInfo',
    method: 'post',
    data
  })
}

export function healthConnection(params) {
  return request({
    url: '/sys/fusion/healthConnection',
    method: 'get',
    params
  })
}
export function registerConnection(params) {
  return request({
    url: '/sys/fusion/registerConnection',
    method: 'get',
    params
  })
}
export function deleteConnection(params) {
  return request({
    url: '/sys/fusion/deleteConnection',
    method: 'get',
    params
  })
}
export function findOrganInGroup(params) {
  return request({
    url: '/sys/fusion/findOrganInGroup',
    method: 'get',
    params
  })
}
export function findAllGroup(params) {
  return request({
    url: '/sys/fusion/findAllGroup',
    method: 'get',
    showLoading: false,
    params
  })
}
export function createGroup(params) {
  return request({
    url: '/sys/fusion/createGroup',
    method: 'get',
    params
  })
}
export function joinGroup(params) {
  return request({
    url: '/sys/fusion/joinGroup',
    method: 'get',
    params
  })
}
export function exitGroup(params) {
  return request({
    url: '/sys/fusion/exitGroup',
    method: 'get',
    params
  })
}
export function findMyGroupOrgan(params) {
  return request({
    url: '/sys/fusion/findMyGroupOrgan',
    method: 'get',
    params
  })
}

