import request from '@/utils/request'

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
