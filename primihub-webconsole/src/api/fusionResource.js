import request from '@/utils/request'

export function getResourceList(params) {
  return request({
    url: '/data/fusionResource/getResourceList',
    method: 'get',
    showLoading: false,
    params
  })
}
export function getCoopResourceList(params) {
  return request({
    url: '/data/fusionResource/getCoopResourceList',
    method: 'get',
    showLoading: false,
    params
  })
}
export function getResourceTagList(params) {
  return request({
    url: '/data/fusionResource/getResourceTagList',
    method: 'get',
    params
  })
}
export function getDataResource(params) {
  return request({
    url: '/data/fusionResource/getDataResource',
    method: 'get',
    params
  })
}

export function getDataResourceAssignedToMe(params) {
  return request({
    url: '/data/resource/getDataResourceAssignedToMe',
    method: 'get',
    params
  })
}

