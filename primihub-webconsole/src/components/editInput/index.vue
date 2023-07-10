<template>
  <div>
    <el-input
      v-if="inputVisible"
      ref="inputRef"
      v-model="inputValue"
      size="small"
      v-bind="$attrs"
      @keyup.enter.native="handleInputConfirm"
      @blur="handleInputConfirm"
    />
    <span v-else>
      <span>{{ inputValue }}</span>
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
      inputValue: this.value,
      inputVisible: false
    }
  },
  watch: {
    value(newValue, oldValue) {
      this.inputValue = oldValue
      if (newValue) {
        this.inputValue = newValue
      }
    }
  },
  methods: {
    handleInputConfirm() {
      if (/\[|\]/g.test(this.inputValue)) {
        this.$message.error('输入内容不能包含英文[]字符')
        return
      }
      this.inputVisible = false
      this.$emit('change', {
        change: this.value !== this.inputValue,
        value: this.inputValue
      })
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
