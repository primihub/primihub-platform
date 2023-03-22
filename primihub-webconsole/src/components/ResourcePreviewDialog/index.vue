<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <ResourcePreviewTable :data="data" style="width:100%;" max-height="500" />
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
::v-deep .el-dialog__header{
  padding: 0;
}
</style>
