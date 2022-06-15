import request from '@/utils/request'

/**
 * 获取机构分页
 *
 * @export
 * @param {*} params
 * pOrganId:父节点id 非必填 默认为0
 * organName:机构名称 非必填
 * pageNum:页数 非必填 默认为1
 * pageSize:每页条数 非必填 默认为10
 * @return {*}
 */
export function getOrgans(params) {
  return request({
    url: '/sys/organ/findOrganPage',
    method: 'get',
    params
  })
}

/**
 * 创建机构
 * @export
 * @param {*}
 * organName:机构名称
 * pOrganId:父节点id
 * organIndex: 顺序
 * @return {*}
 */
export function createOrganNode(data) {
  return request({
    url: '/sys/organ/createOrganNode',
    method: 'post',
    data
  })
}

/**
 * 更改机构节点
 * @export
 * @param {*} data
 * organId:机构id
 * organName:机构名称
 * @return {*}
 */
export function alterOrganNodeStatus(data) {
  return request({
    url: '/sys/organ/alterOrganNodeStatus',
    method: 'post',
    data
  })
}

/**
 * 删除机构节点
 * @export
 * @param {*} params
 * organId:节点id
 * @return {*}
 */
export function deleteOrganNode(params) {
  return request({
    url: '/sys/organ/deleteOrganNode',
    method: 'get',
    params
  })
}
