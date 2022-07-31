import request from '@/utils/request'

export function pirSubmitTask(params) {
  return request({
    url: '/data/pir/pirSubmitTask',
    method: 'get',
    params
  })
}
export function getTaskData(params) {
  return request({
    url: '/data/task/getTaskData',
    method: 'get',
    showLoading: false,
    params
  })
}

