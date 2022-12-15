<template>
  <div v-if="inputVisible" class="input-item">
    <el-input
      ref="input"
      v-model="inputValue"
      size="small"
      v-bind="$attrs"
      :type="$attrs.type"
      autosize
      autofocus
      placeholder="请输入"
      :maxlength="maxlength"
      show-word-limit
      @keyup.enter.native="handleInputConfirm"
    />
    <span class="buttons">
      <el-button type="primary" size="small" @click="handleInputConfirm">保存</el-button>
      <el-button type="info" size="small" @click="handleInputCancel">取消</el-button>
    </span>
  </div>
  <div v-else>
    <span v-if="isHtml" v-html="inputValue" />
    <span v-else>{{ inputValue }}</span>
    <i class="iconfont icon-edit edit-icon" @click="showInput">编辑</i>
  </div>

</template>

<script>
export default {
  props: {
    name: {
      type: String,
      default: 'name'
    },
    defaultValue: {
      type: String,
      default: ''
    },
    isHtml: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      inputValue: this.defaultValue,
      inputVisible: false
    }
  },
  computed: {
    maxlength() {
      return this.$attrs.type === 'textarea' ? 200 : 30
    }
  },
  watch: {
    defaultValue(newVal) {
      if (newVal) {
        this.inputValue = newVal
      } else {
        this.inputValue = this.defaultValue
      }
    }
  },
  methods: {
    handleInputCancel() {
      this.inputVisible = false
    },
    handleInputConfirm() {
      this.inputVisible = false
      this.$emit('submit', {
        inputValue: this.inputValue,
        name: this.name
      })
    },
    showInput() {
      this.inputVisible = true
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
  font-size: 14px;
  cursor: pointer;
}
.input-item{
  display: flex;
  flex: 1;
  .buttons{
    margin-left: 10px;
    display: flex;
  }

}
</style>
