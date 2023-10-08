<template>
  <el-dialog
    :title="$attrs.title? $attrs.title: '数据资源预览'"
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <ResourcePreviewTable :data="data" :empty-text="emptyText" style="width:100%;" max-height="500" />
  </el-dialog>
</template>

<script>
import ResourcePreviewTable from '@/components/ResourcePreviewTable'
export default {
  name: 'ResourcePreviewDialog',
  components: {
    ResourcePreviewTable
  },
  props: {
    data: {
      type: Array,
      default: () => []
    },
    emptyText: {
      type: String,
      default: '暂无数据'
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close')
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-dialog__body{
  padding: 20px;
}
</style>
