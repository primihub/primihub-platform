<template>
  <div>
    <el-dialog
      :visible.sync="visible"
      :before-close="closeDialog"
      v-bind="$attrs"
    >
      <slot />
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'PHDialog',
  props: {
    visible: {
      type: Boolean,
      default: false,
      require: true
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close')
    }
  }
}
</script>
