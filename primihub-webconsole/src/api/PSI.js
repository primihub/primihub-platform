import request from '@/utils/request'

export function getPsiResourceList(params) {
  return request({
    url: '/data/psi/getPsiResourceList',
    method: 'get',
    params
  })
}

export function saveDataPsi(data) {
  return request({
    url: '/data/psi/saveDataPsi',
    method: 'post',
    data
  })
}

export function getPsiResourceDetails(params) {
  return request({
    url: '/data/psi/getPsiResourceDetails',
    method: 'get',
    params
  })
}
export function getPsiResourceAllocationList(params) {
  return request({
    url: '/data/psi/getPsiResourceAllocationList',
    method: 'get',
    params,
    showLoading: false
  })
}
export function getPsiTaskList(params) {
  return request({
    url: '/data/psi/getPsiTaskList',
    method: 'get',
    showLoading: false,
    params
  })
}
export function getPsiTaskDetails(params) {
  return request({
    url: '/data/psi/getPsiTaskDetails',
    method: 'get',
    params,
    showLoading: false
  })
}
export function getOrganPsiTask(params) {
  return request({
    url: '/data/psi/getOrganPsiTask',
    method: 'get',
    params
  })
}
export function downloadPsiTask(params) {
  return request({
    url: '/data/psi/downloadPsiTask',
    method: 'get',
    params
  })
}
export function cancelTask(taskId) {
  return request({
    url: '/data/task/cancelTask',
    method: 'get',
    params: {
      taskId
    }
  })
}
export function cancelPsiTask(params) {
  return request({
    url: '/data/psi/cancelPsiTask',
    method: 'get',
    params
  })
}
export function delPsiTask(params) {
  return request({
    url: '/data/psi/delPsiTask',
    method: 'get',
    params
  })
}
export function retryPsiTask(params) {
  return request({
    url: '/data/psi/retryPsiTask',
    method: 'get',
    params
  })
}

export function saveOrUpdatePsiResource(data) {
  return request({
    url: '/data/psi/saveOrUpdatePsiResource',
    method: 'post',
    data
  })
}
export function updateDataPsiResultName(data) {
  return request({
    url: '/data/psi/updateDataPsiResultName',
    method: 'post',
    data
  })
}
