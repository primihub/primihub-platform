import request from '@/utils/request'

export function getPirTaskDetail(params) {
  return request({
    url: '/data/pir/getPirTaskDetail',
    method: 'get',
    params,
    showLoading: false
  })
}
export function getPirTaskList(params) {
  return request({
    url: '/data/pir/getPirTaskList',
    method: 'get',
    params,
    showLoading: false
  })
}
export function pirSubmitTask(params) {
  return request({
    url: '/data/pir/pirSubmitTask',
    method: 'get',
    params
  })
}

export const submitPirPhase1Request = (data) => request({ url: '/data/pir/submitPirPhase1', method: 'post', data })


export const executePirPhase2Request = (data) => request({ url: '/data/pir/submitPirPhase2', method: 'post', data })

