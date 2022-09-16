<template>
  <div class="operating">
    <el-tooltip v-if="options.buttons.includes('run')" class="item" effect="light" content="运行" placement="bottom">
      <i class="el-icon-video-play" @click="runFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('save')" class="item" effect="light" content="保存" placement="bottom">
      <svg-icon icon-class="save" @click="saveFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('zoomIn')" class="item" effect="light" content="放大" placement="bottom">
      <i class="el-icon-zoom-in" @click="zoomInFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('zoomOut')" class="item" effect="light" content="缩小" placement="bottom">
      <i class="el-icon-zoom-out" :class="{opacity: !canZoomOut}" @click="zoomOutFn" />
    </el-tooltip>
    <el-tooltip v-if="options.buttons.includes('reset')" class="item" effect="light" content="重置" placement="bottom">
      <i class="el-icon-full-screen" @click="resetFn" />
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
          show: true,
          buttons: ['run', 'save', 'zoomIn', 'zoomOut', 'reset', 'clear']
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
  // width: 100%;
  // height: 35px;
  display: flex;
  align-items: center;
  position: absolute;
  justify-content: center;
  top: 10px;
  left: 50%;
  transform: translate3d(-50%,0px,0);
  z-index: 999;
  // background-color: #f7f9fb;
  background-color: #fff;
  // border-bottom: 1px solid rgba(0,0,0,.08);
  box-shadow: 1px 1px 4px rgba(0,0,0,.08);
  padding: 10px 20px;
  .el-tooltip {
    font-size: 22px;
    height: 22px;
    margin: 0 10px;
    color: #515a6e;
    display: inline-block;
    vertical-align: middle;
    &.opacity {
      opacity: .5;
    }
  }
  .el-tooltip{
    cursor: pointer;
  }
}
</style>
