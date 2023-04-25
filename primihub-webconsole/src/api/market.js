import request from '@/utils/request'

export function getVisitUsers() {
  return request({
    url: '/data/market/getvisitingusers',
    method: 'GET'
  })
}

export function submitVisitUsers(data) {
  return request({
    url: '/data/market/submitvisitingusers',
    method: 'POST',
    type: 'json',
    data
  })
}

export function readNumber(params) {
  return request({
    url: '/data/market/display',
    method: 'GET',
    params
  })
}
