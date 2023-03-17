<template>
  <div>
    <el-input
      v-if="inputVisible"
      ref="inputRef"
      v-model="resourceName"
      size="small"
      v-bind="$attrs"
      @keyup.enter.native="handleInputConfirm"
      @blur="handleInputConfirm"
    />
    <span v-else>
      <span>{{ resourceName }}</span>
      <i class="edit-icon el-icon-edit-outline" @click="showInput" />
    </span>
  </div>
</template>

<script>
export default {
  name: 'EditInput',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      resourceName: this.value,
      inputVisible: false
    }
  },
  watch: {
    value(newValue, oldValue) {
      this.resourceName = oldValue
      if (newValue) {
        this.resourceName = newValue
      }
    }
  },
  methods: {
    handleInputConfirm() {
      this.inputVisible = false
      this.$emit('change', this.resourceName)
    },
    showInput() {
      this.inputVisible = true
      this.$nextTick(() => {
        this.$refs.inputRef.focus()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.edit-icon{
  color: $mainColor;
  vertical-align: middle;
  margin-left: 10px;
  display: inline-block;
  font-size: 18px;
  cursor: pointer;
}
</style>
