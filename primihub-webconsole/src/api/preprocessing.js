import request from '@/utils/request'

export const getExamTaskListRequest = (params) => request({ url: '/data/examine/getExamTaskList', method: 'get', params })

export const saveExamTaskRequest = (data) => request({ url: '/data/examine/saveExamTask', method: 'post', type: 'json', data })

export const submitExamTaskRequest = (data) => request({ url: '/data/examine/submitExamTask', method: 'post', type: 'json', data })

export const getExamTaskDetailRequest = (taskId) => request({ url: '/data/examine/getExamTaskDetail', method: 'get', params: { taskId }})

export const processExamTaskRequest = (data) => request({ url: '/data/examine/processExamTask', method: 'post', type: 'json', data })

export const finishExamTaskRequest = (data) => request({ url: '/data/examine/finishExamTask', method: 'post', type: 'json', data })
