<template>
  <div>
    <el-input
      v-model.trim="tagValue"
      class="tag-input"
      maxlength="9"
      :disabled="disabledInput"
      :clearable="true"
      show-word-limit
      placeholder="请添加标签，方便搜索"
      @keyup.enter.native="addTag"
    />
    <div class="tag-container">
      <div v-for="(item, index) in tagValueList" :key="index" class="tag-item" @mouseenter="mouseItem($event)" @mouseleave="mouseLeaveItem($event)">
        {{ item }}
        <i class="el-icon-circle-close icon-pd-10" @click="removeTag(index)" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: Array,
      default: () => []
    }
  },

  data() {
    return {
      tagValue: '',
      disabledInput: false
    }
  },
  computed: {
    tagValueList() {
      this.setDisabled(this.selectedData.length >= 5)
      return this.selectedData
    }
  },

  methods: {
    setDisabled(data) {
      this.disabledInput = !!data
    },
    addTag() {
      if (this.tagValueList.length < 5 && this.tagValue.trim() !== '') {
        this.tagValueList.push(this.tagValue)
        this.tagValue = ''
      } else if (this.tagValueList.length >= 5) {
        this.disabledInput = true
      }
      this.$emit('change', this.tagValueList)
    },
    mouseItem(e) {
      const targetDom = e.target.querySelector('.icon-pd-10')
      if (targetDom) {
        targetDom.classList.remove('el-icon-circle-close')
        targetDom.classList.add('el-icon-error')
      }
    },
    mouseLeaveItem(e) {
      const targetDom = e.target.querySelector('.icon-pd-10')
      if (targetDom) {
        targetDom.classList.remove('el-icon-error')
        targetDom.classList.add('el-icon-circle-close')
      }
    },
    removeTag(index) {
      this.tagValueList.splice(index, 1)
      this.$emit('change', this.tagValueList)
    }
  }
}
</script>

<style lang="scss" scoped>
.tags-input {
  :deep(.el-input .el-input__count .el-input__count-inner) {
    background-color: transparent;
  }
}
.tag-container {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  .tag-item {
    margin-top: 10px;
    height: 40px;
    line-height: 40px;
    padding: 3px 16px 3px 16px;
    background-color: #F6F6F6;
    margin-right: 10px;
    color: #666;
    border-radius: 5px;
    display: inline-flex;
    align-items: center;
    user-select: none;
    &:last-child {
      margin-right: 0px;
    }
    &:hover{
      color: #333;
    }
    .icon-pd-10 {
      cursor: pointer;
      padding-left: 8px;
      transition: all .3s;
    }
  }
}
</style>
