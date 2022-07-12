import request from '@/utils/request'

export function getProjectList(params) {
  return request({
    url: '/data/project/getProjectList',
    method: 'get',
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
export function removeResource(data) {
  return request({
    url: '/data/project/removeResource',
    method: 'post',
    data
  })
}
