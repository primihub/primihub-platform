import request from '@/utils/request'

export function getDataResourceAssignedToMe(params) {
  return request({
    url: '/data/resource/getDataResourceAssignedToMe',
    method: 'get',
    params
  })
}
export function getDerivationResourceData(params) {
  return request({
    url: '/data/resource/getDerivationResourceData',
    method: 'get',
    params
  })
}
export function getDerivationResourceList(params) {
  return request({
    url: '/data/resource/getDerivationResourceList',
    method: 'get',
    params
  })
}
export function displayDatabaseSourceType(params) {
  return request({
    url: '/data/resource/displayDatabaseSourceType',
    method: 'get',
    params
  })
}
export function getDataResourceUsage(params) {
  return request({
    url: '/data/resource/getDataResourceUsage',
    method: 'get',
    params
  })
}

export function healthConnection(data) {
  return request({
    url: '/data/dbsource/healthConnection',
    method: 'post',
    type: 'json',
    data
  })
}
export function saveDataResourceAssignment(data) {
  return request({
    url: '/data/resource/saveDataResourceAssignment',
    method: 'post',
    type: 'json',
    data
  })
}
export function saveDataResourceAssignLocal(data) {
  return request({
    url: '/data/resource/saveDataResourceAssignLocal',
    method: 'post',
    type: 'json',
    data
  })
}

export function tableDetails(data) {
  return request({
    url: '/data/dbsource/tableDetails',
    method: 'post',
    type: 'json',
    data
  })
}

export function resourceStatusChange(data) {
  return request({
    url: '/data/resource/resourceStatusChange',
    method: 'post',
    data
  })
}

export function getResourceList(params) {
  return request({
    url: '/data/resource/getdataresourcelist',
    method: 'get',
    params
  })
}
export function getDataResourceToApply(params) {
  return request({
    url: '/data/resource/getDataResourceToApply',
    method: 'get',
    params
  })
}
export function getDataResourceAssignmentDetail(params) {
  return request({
    url: '/data/resource/getDataResourceAssignmentDetail',
    method: 'get',
    params
  })
}
export function saveDataResourceUserAssignment(params) {
  return request({
    url: '/data/resource/saveDataResourceUserAssignment',
    method: 'post',
    data: params,
    type: 'json'
  })
}
export function saveDataResourceAssign(params) {
  return request({
    url: '/data/resource/saveDataResourceAssign',
    method: 'post',
    data: params,
    type: 'json'
  })
}
export function changeDataResourceAuthStatus(params) {
  return request({
    url: '/data/resource/changeDataResourceAuthStatus',
    method: 'post',
    data: params
  })
}
export function getDataSetList(params) {
  return request({
    url: '/dataShare/list',
    method: 'get',
    params
  })
}
export function getResourceDetail(resourceId) {
  return request({
    url: '/data/resource/getdataresource',
    method: 'get',
    params: {
      resourceId
    }
  })
}
// export function getResourceDetail(resourceId) {
//   return request({
//     url: '/dataShare/info/{resourceFusionId}',
//     method: 'get',
//     params: {
//       resourceId
//     }
//   })
// }
export function getOriginList(params) {
  return Promise.resolve([])
}
export function saveResource(data) {
  return request({
    url: '/data/resource/saveorupdateresource',
    method: 'post',
    type: 'json',
    timeout: 600000,
    data
  })
}
export function deleteResource(resourceId) {
  return request({
    url: '/data/resource/deldataresource',
    method: 'get',
    params: {
      resourceId
    }
  })
}
export function getAuthorizationList(params) {
  return request({
    url: '/data/resource/getauthorizationlist',
    method: 'get',
    params
  })
}

export function resourceApproval(data) {
  return request({
    url: '/data/resource/approval',
    method: 'post',
    data
  })
}

export function getDataResourceFieldPage(params) {
  return request({
    url: '/data/resource/getDataResourceFieldPage',
    method: 'get',
    params
  })
}

export function updateDataResourceField(data) {
  return request({
    url: '/data/resource/updateDataResourceField',
    method: 'post',
    // type: 'json',
    data
  })
}

export function resourceFilePreview(params) {
  return request({
    url: '/data/resource/resourceFilePreview',
    method: 'get',
    params
  })
}

export function getResourceTags(params) {
  return request({
    url: '/data/resource/getResourceTags',
    method: 'get',
    params
  })
}

