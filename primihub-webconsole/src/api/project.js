import request from '@/utils/request'

export function getDerivationResourceList(params) {
  return request({
    url: '/data/project/getDerivationResourceList',
    method: 'get',
    params
  })
}

export function getProjectList(params) {
  return request({
    url: '/data/project/getProjectList',
    method: 'get',
    params
  })
}
export function getResourceList(params) {
  return request({
    url: '/data/project/getResourceList',
    method: 'get',
    showLoading: false,
    params
  })
}
export function getListStatistics(params) {
  return request({
    url: '/data/project/getListStatistics',
    method: 'get',
    params
  })
}
export function getProjectDetail(params) {
  return request({
    url: '/data/project/getProjectDetails',
    method: 'get',
    params
  })
}
export function saveProject(data) {
  return request({
    url: '/data/project/saveOrUpdateProject',
    method: 'post',
    type: 'json',
    data
  })
}
export function approval(data) {
  return request({
    url: '/data/project/approval',
    method: 'post',
    data
  })
}
export function closeProject(data) {
  return request({
    url: '/data/project/closeProject',
    method: 'post',
    data
  })
}
export function openProject(data) {
  return request({
    url: '/data/project/openProject',
    method: 'post',
    data
  })
}
export function removeResource(data) {
  return request({
    url: '/data/project/removeResource',
    method: 'post',
    data
  })
}
export function removeOrgan(data) {
  return request({
    url: '/data/project/removeOrgan',
    method: 'post',
    data
  })
}
