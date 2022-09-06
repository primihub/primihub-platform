<template>
  <div class="operating" :class="[style]">
    <el-tooltip v-if="options.buttons.includes('run')" class="item" effect="light" content="运行" placement="bottom">
      <i class="el-icon-video-play" @click="runFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('save')" class="item" effect="light" content="保存" placement="bottom">
      <svg-icon icon-class="save" @click="saveFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('reset')" class="item" effect="light" content="重置" placement="bottom">
      <i class="el-icon-full-screen" @click="resetFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('zoomIn')" class="item" effect="light" content="放大" placement="bottom">
      <i class="el-icon-zoom-in" @click="zoomInFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('zoomOut')" class="item" effect="light" content="缩小" placement="bottom">
      <i class="el-icon-zoom-out" @click="zoomOutFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('clear')" class="item" effect="light" content="清空" placement="bottom">
      <svg-icon icon-class="clear" @click="clearFn" />
    </el-tooltip>
  </div>
</template>

<script>
export default {
  name: 'ToolBar',
  props: {
    options: {
      type: Object,
      default: () => {
        return {
          position: 'center', // 显示位置,默认center 'center'/'right'/'left
          background: true, // 是否有背景色
          buttons: ['run', 'save', 'zoomIn', 'zoomOut', 'reset', 'clear'] // 按钮列表
        }
      }
    }
  },
  data() {
    return {
      canUndo: '',
      canRedo: '',
      canZoomOut: true
    }
  },
  computed: {
    style() {
      console.log('options', this.options)
      return {
        'hasBackgroundColor': this.options.background,
        'left': this.options.position === 'left',
        'right': this.options.position === 'right',
        'center': this.options.position === 'center' // default
      }
    }
  },
  methods: {
    // 放大
    zoomInFn() {
      this.canZoomOut = true
      this.$emit('zoomIn')
    },
    // 缩小
    zoomOutFn() {
      this.$emit('zoomOut')
    },
    // 重做
    redoFn() {
      this.$emit('redo')
    },
    // 撤消
    runFn() {
      this.$emit('run')
    },
    // 清除
    clearFn() {
      this.$emit('clear')
    },
    // 保存模板文件
    saveFn() {
      this.$emit('save')
    },
    // 重置
    resetFn() {
      this.$emit('reset')
    }
  }
}
</script>

<style lang="scss" scoped>
.operating {
  display: flex;
  align-items: center;
  position: absolute;
  justify-content: center;
  z-index: 999;
  padding: 10px 20px;
  &.left{
    left: 0;
    top: 0;
  }
  &.right{
    right: 0px;
    top: 0;
  }
  &.center{
    top: 10px;
    left: 50%;
    transform: translate3d(-50%,0px,0);
  }
  .el-tooltip {
    margin: 0 10px;
    display: inline-block;
    vertical-align: middle;
    text-align: center;
    width: 30px;
    height: 30px;
    line-height: 30px;
    font-size: 18px;
    color: #fff;
    background-color: rgba(0,0,0,.5);
    border-radius: 50%;
    cursor: pointer;
  }
  &.hasBackgroundColor{
    background-color: #fff;
    box-shadow: 1px 1px 4px rgba(0,0,0,.08);
    .el-tooltip{
      font-size: 22px;
      height: 22px;
      line-height: 22px;
      color: #515a6e;
      background: none;
      border-radius: 0;
    }
  }
}
</style>
