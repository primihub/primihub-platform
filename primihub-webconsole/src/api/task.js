import request from '@/utils/request'

export function cancelTask(taskId) {
  return request({
    url: '/data/task/cancelTask',
    method: 'get',
    params: {
      taskId
    }
  })
}
export function getTaskList(params) {
  return request({
    url: '/data/task/getTaskList',
    method: 'get',
    params,
    showLoading: false
  })
}
export function getTaskLogInfo(taskId) {
  return request({
    url: '/data/task/getTaskLogInfo',
    method: 'get',
    params: {
      taskId
    }
  })
}
export function deleteTask1(taskId) {
  return request({
    url: '/data/task/deleteTask',
    method: 'get',
    params: {
      taskId
    }
  })
}

export function deleteTask(taskId) {
  return request({
    url: '/data/task/deleteTask',
    method: 'post',
    data: { taskId }
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

export function updateTaskDesc(params) {
  return request({
    url: '/data/task/updateTaskDesc',
    method: 'get',
    params
  })
}
