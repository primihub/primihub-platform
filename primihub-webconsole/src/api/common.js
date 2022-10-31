import request from '@/utils/request'

export function getTrackingID(params) {
  return request({
    url: '/sys/common/getTrackingID',
    method: 'get',
    params
  })
}
