import request from '@/utils/request'

export function getProjectList(params) {
  return request({
    url: '/data/project/getprojectlist',
    method: 'get',
    params
  })
}
export function getProjectDetail(params) {
  return request({
    url: '/data/project/getdataproject',
    method: 'get',
    params
  })
}
export function saveProject(data) {
  return request({
    url: '/data/project/saveproject',
    method: 'post',
    type: 'form',
    data
  })
}
