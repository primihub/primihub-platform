import request from '@/utils/request'

export function getModelList(params) {
  return request({
    url: '/data/model/getmodellist',
    method: 'get',
    params
  })
}
export function getModelDetail(params) {
  return request({
    url: '/data/model/getdatamodel',
    method: 'get',
    params
  })
}

export function createModel(data) {
  return request({
    url: '/data/model/savemodel',
    method: 'post',
    type: 'form',
    data
  })
}

// 查询模型左侧组件列表接口
export function getModelComponent(params) {
  return request({
    url: '/data/model/getModelComponent',
    method: 'get',
    params
  })
}

// 模型实时草稿和保存接口接口
export function saveModelAndComponent(data) {
  return request({
    url: '/data/model/saveModelAndComponent',
    method: 'post',
    type: 'json',
    showLoading: false,
    data
  })
}

// 模型组件详情接口
export function getModelComponentDetail(params) {
  return request({
    url: '/data/model/getModelComponentDetail',
    method: 'get',
    params
  })
}

// 模型删除接口
export function deleteModel(params) {
  return request({
    url: '/data/model/deleteModel',
    method: 'get',
    params
  })
}
// 根据项目id查询资源、y字段接口
export function getProjectResourceData(params) {
  return request({
    url: '/data/project/getProjectResourceData',
    method: 'get',
    showLoading: false,
    params
  })
}
export function getProjectResourceOrgan(params) {
  return request({
    url: '/data/project/getProjectResourceOrgan',
    method: 'get',
    showLoading: false,
    params
  })
}
// 模型运行
export function runTaskModel(params) {
  return request({
    url: '/data/model/runTaskModel',
    method: 'get',
    params
  })
}
// 模型轮询查询状态
export function getTaskModelComponent(params) {
  return request({
    url: '/data/model/getTaskModelComponent',
    method: 'get',
    showLoading: false,
    params
  })
}
// 模型轮询查询状态
export function getModelPrediction(params) {
  return request({
    url: '/data/model/getModelPrediction',
    method: 'get',
    params
  })
}
// 模型任务列表
export function getModelTaskList(params) {
  return request({
    url: '/data/task/getModelTaskList',
    method: 'get',
    showLoading: false,
    params
  })
}

