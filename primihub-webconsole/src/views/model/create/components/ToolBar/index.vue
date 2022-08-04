<template>
  <div class="operating">
    <el-tooltip class="item" effect="light" content="运行" placement="bottom">
      <i class="el-icon-video-play" @click="runFn" />
    </el-tooltip>
    <el-tooltip class="item" effect="light" content="保存" placement="bottom">
      <svg-icon icon-class="save" @click="saveFn" />
    </el-tooltip>
    <!-- <el-tooltip class="item" effect="light" content="撤销" placement="bottom">
      <i class="el-icon-refresh-left" :class="{opacity: !canUndo}" @click="undoFn" />
    </el-tooltip>-->
    <el-tooltip class="item" effect="light" content="放大" placement="bottom">
      <i class="el-icon-zoom-in" @click="zoomInFn" />
    </el-tooltip>
    <el-tooltip class="item" effect="light" content="缩小" placement="bottom">
      <i class="el-icon-zoom-out" :class="{opacity: !canZoomOut}" @click="zoomOutFn" />
    </el-tooltip>
    <el-tooltip class="item" effect="light" content="清除" placement="bottom">
      <!-- <i class="el-icon-delete" /> -->
      <svg-icon icon-class="clear" @click="clearFn" />
    </el-tooltip>
    <!-- <el-tooltip class="item" effect="light" content="重做" placement="bottom">
      <i class="el-icon-refresh-right" :class="{opacity: !canRedo}" @click="redoFn" />
    </el-tooltip> -->

  </div>
</template>

<script>
export default {
  name: 'ToolBar',
  data() {
    return {
      canUndo: '',
      canRedo: '',
      canZoomOut: true
      // graph: null
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
