
const taskTypeFilter = (status) => {
  status = status || 0
  const statusMap = {
    1: '联合建模',
    2: '安全求交',
    3: '隐匿查询',
    4: '联合预测',
    5: '联合统计'
  }
  return statusMap[status]
}

const taskStatusFilter = (status) => {
  // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
  status = status || 0
  const statusMap = {
    0: '未开始',
    1: '已完成',
    2: '执行中',
    3: '任务失败',
    4: '已取消',
    5: '已删除'
  }
  return statusMap[status]
}

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
  limit = Number(limit)
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
    2: '数据库导入',
    3: '衍生数据'
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
    2: '纵向-XGBoost',
    3: '横向-逻辑回归',
    4: 'MPC-逻辑回归',
    5: '纵向-逻辑回归',
    6: '横向-NN(分类)',
    7: '横向-NN(回归)',
    8: '横向-线性回归',
    9: '纵向-线性回归'
  }
  return typeMap[type]
}

const projectAuditStatusFilter = (status) => {
  const sourceMap = {
    0: '审核中',
    1: '可用',
    2: '关闭',
    11: '全部可用',
    12: '部分可用'
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
const timeFilter = (time) => {
  if (!time) {
    return '00:00:00'
  } else {
    const days = parseInt(time / (60 * 60 * 24))
    const hour = parseInt(time / 3600) < 10 ? '0' + parseInt(time / 3600) : parseInt(time / 3600)
    const min = parseInt(time % 3600 / 60) < 10 ? '0' + parseInt(time % 3600 / 60) : parseInt(time % 3600 / 60)
    const sec = parseInt(time % 3600 % 60) < 10 ? '0' + parseInt(time % 3600 % 60) : parseInt(time % 3600 % 60)
    if (days > 0) {
      return days + '天' + hour + ':' + min + ':' + sec
    } else {
      return hour + ':' + min + ':' + sec
    }
  }
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
  resourceAuditStatusFilter,
  timeFilter,
  taskStatusFilter,
  taskTypeFilter
}
