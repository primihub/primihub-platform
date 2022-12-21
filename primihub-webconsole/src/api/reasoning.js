import request from '@/utils/request'

export function getReasoningList(params) {
  return request({
    url: '/data/reasoning/getReasoningList',
    method: 'get',
    params,
    showLoading: false
  })
}
export function getReasoning(params) {
  return request({
    url: '/data/reasoning/getReasoning',
    method: 'get',
    params,
    showLoading: false
  })
}

export function saveReasoning(data) {
  return request({
    url: '/data/reasoning/saveReasoning',
    method: 'post',
    showLoading: false,
    data
  })
}

