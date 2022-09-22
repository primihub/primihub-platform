<template>
  <i :class="statusStyle(status)" />
</template>

<script>
export default {
  name: 'TaskStatus',
  props: {
    status: {
      type: Number,
      default: 0
    }
  },
  methods: {
    statusStyle(status) {
      return status === 0 ? 'status-default el-icon-error' : status === 1 ? 'status-end el-icon-success' : status === 2 ? 'status-processing el-icon-loading' : status === 3 ? 'status-error el-icon-error' : 'status-default  el-icon-error'
    }
  }
}
</script>

<style lang="scss" scoped>
.status-default,.status-processing,.status-error,.status-end{
  margin-right: 3px;
  font-size: 12px;
}
.status-default{
  color: #909399;
}
.status-end{
  color: #67C23A;
}
.status-processing{
  color: #909399;
}
.status-error{
  color: #F56C6C;
}
.error-tips{
  display: block;
  white-space:normal;
  color: #F56C6C;
  font-size: 12px;
  line-height: 1.2;
}
</style>
