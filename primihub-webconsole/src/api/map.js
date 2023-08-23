import request from '@/utils/request'

export function getOrgInfo() {
  return request({
    url: 'sys/organ/getLocalOrganInfo',
    method: 'get'
  })
}

export function getCoordinates(params) {
  return request({
    url: 'sys/common/getCollectList',
    method: 'get',
    params: {
      key: 'Qg7T3TgGBtYIF2XJOViTgWSuohNnkakU',
      page: 1
    }
  })
}
