
<script>
export default {
  name: 'ViewBoxForm',
  props: {
    width: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      scopedSlots: undefined
    }
  },
  mounted() {
    this.scopedSlots = this.$scopedSlots
  }
}
</script>

<template>
  <div class="yy-form-container">
    <div class="yy-form-center" :style="{'max-width': width || '620px'}">
      <slot name="container" />
      <div v-if="scopedSlots && scopedSlots.footer" class="yy-form-btn-container">
        <slot name="footer" />
      </div>
      <div v-else class="yy-form-btn-container">
        <el-button>取消</el-button>
        <el-button type="primary">保存</el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.yy-form-container{
  padding: 20px;
  background-color: #fff;
}
.yy-form-center{
  margin: 0 auto;
  min-width: 300px;
  max-width: 620px;
}
.yy-form-btn-container{
  padding: 20px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
