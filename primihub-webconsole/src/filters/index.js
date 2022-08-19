const authTypeFilter = (type) => {
  const typeMap = {
    1: '公开',
    2: '私有',
    3: '指定机构可见'
  }
  return typeMap[type]
}

const sortTypeFilter = (type) => {
  const typeMap = {
    1: '银行',
    2: '电商',
    3: '媒体',
    4: '运营商',
    5: '保险'
  }
  return typeMap[type]
}

const fileSizeFilter = (limit) => {
  let size = ''
  if (limit < 0.1 * 1024) { // 如果小于0.1KB转化成B
    size = limit.toFixed(2) + 'B'
  } else if (limit < 0.1 * 1024 * 1024) { // 如果小于0.1MB转化成KB
    size = (limit / 1024).toFixed(2) + 'KB'
  } else if (limit < 0.1 * 1024 * 1024 * 1024) { // 如果小于0.1GB转化成MB
    size = (limit / (1024 * 1024)).toFixed(2) + 'MB'
  } else { // 其他转化成GB
    size = (limit / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
  }

  const sizeStr = size + ''
  const len = sizeStr.indexOf('\.')
  const dec = sizeStr.substr(len + 1, 2)
  if (dec === '00') { // 当小数点后为00时 去掉小数部分
    return sizeStr.substring(0, len) + sizeStr.substr(len + 3, 2)
  }
  return sizeStr
}

const sourceFilter = (source) => {
  const sourceMap = {
    1: '文件上传',
    2: '数据库'
  }
  return sourceMap[source]
}

// 文件处理状态
const statusFilter = (status) => {
  const sourceMap = {
    0: '处理中',
    1: '处理中',
    2: '已完成'
  }
  return sourceMap[status]
}
// 文件处理状态
const authStatusFilter = (status) => {
  const sourceMap = {
    0: '未授权',
    1: '已授权',
    2: '已拒绝'
  }
  return sourceMap[status]
}
const modelTypeFilter = (type) => {
  const typeMap = {
    1: '联邦学习ID对齐',
    2: 'V-XGBoost ',
    3: 'V-逻辑回归 ',
    4: '线性回归 '
  }
  return typeMap[type]
}

const projectAuditStatusFilter = (status) => {
  const sourceMap = {
    0: '审核中',
    1: '可用',
    2: '不可用'
  }
  return sourceMap[status]
}
const resourceAuditStatusFilter = (status) => {
  const sourceMap = {
    0: '审核中',
    1: '已授权',
    2: '已拒绝'
  }
  return sourceMap[status]
}

export default {
  authTypeFilter,
  sortTypeFilter,
  fileSizeFilter,
  sourceFilter,
  statusFilter,
  modelTypeFilter,
  authStatusFilter,
  projectAuditStatusFilter,
  resourceAuditStatusFilter
}
