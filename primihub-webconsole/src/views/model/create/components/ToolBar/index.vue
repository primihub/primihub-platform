<template>
  <div class="operating">
    <el-tooltip class="item" effect="light" content="运行" placement="bottom">
      <i class="el-icon-video-play" @click="runFn" />
    </el-tooltip>
    <el-tooltip class="item" effect="light" content="保存" placement="bottom">
      <i @click="saveFn"><svg t="1650776987670" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4361" width="21" height="21"><path d="M928 896V314.24c0-8.32-3.488-16.64-9.6-22.72l-187.84-186.24a31.36 31.36 0 0 0-22.4-9.28H672v160c0 52.8-43.2 96-96 96H256c-52.8 0-96-43.2-96-96V96H128c-17.6 0-32 14.4-32 32v768c0 17.6 14.4 32 32 32h64v-288c0-52.8 43.2-96 96-96h448c52.8 0 96 43.2 96 96v288h64c17.632 0 32-14.4 32-32z m-160 32v-288c0-17.6-14.368-32-32-32H288c-17.6 0-32 14.4-32 32v288h512zM224 96v160c0 17.6 14.4 32 32 32h320c17.632 0 32-14.4 32-32V96H224z m739.52 150.08c18.272 17.92 28.48 42.88 28.48 68.16V896c0 52.8-43.2 96-96 96H128c-52.8 0-96-43.2-96-96V128c0-52.8 43.2-96 96-96h580.16c25.632 0 49.632 9.92 67.52 27.84l187.84 186.24zM512 256a32 32 0 0 1-32-32V160a32 32 0 0 1 64 0v64a32 32 0 0 1-32 32z" p-id="4362" fill="#515a6e" /></svg></i>
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
      <i @click="clearFn"><svg t="1650771220006" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2186" width="26" height="25"><path d="M872 385.6c0-1.6-1.6-1.6-1.6-3.2v-1.6c0-1.6-1.6-3.2-3.2-4.8-1.6-1.6-1.6-3.2-3.2-3.2-1.6-1.6-3.2-1.6-3.2-3.2h-1.6c-1.6-1.6-3.2-1.6-4.8-1.6h-1.6l-158.4-57.6 46.4-204.8c3.2-17.6-6.4-35.2-24-38.4s-35.2 6.4-38.4 24L633.6 288l-198.4-72c-1.6 0-3.2-1.6-4.8-1.6h-4.8-1.6-1.6c-16 1.6-28.8 14.4-28.8 32 0 11.2-4.8 272-256 420.8h-1.6c-1.6 0-1.6 1.6-3.2 1.6l-1.6 1.6c-3.2-1.6-3.2 0-3.2 0l-1.6 1.6c0 1.6-1.6 1.6-1.6 3.2s-1.6 1.6-1.6 3.2v3.2c0 1.6 0 3.2-1.6 3.2v14.4c0 1.6 1.6 3.2 1.6 4.8 0 1.6 0 1.6 1.6 3.2l1.6 1.6c0 1.6 1.6 1.6 1.6 3.2l6.4 6.4 1.6 1.6H140.8l649.6 236.8c16 6.4 32-1.6 40-16v-1.6c132.8-260.8 46.4-540.8 41.6-553.6zM452.8 288L816 420.8c6.4 22.4 16 67.2 20.8 126.4l-401.6-145.6c-3.2-1.6-4.8-1.6-8-1.6 14.4-44.8 22.4-84.8 25.6-112z m-48 168c3.2 1.6 4.8 3.2 8 4.8L832 612.8c3.2 1.6 4.8 1.6 8 1.6 0 81.6-12.8 179.2-54.4 273.6l-177.6-64 57.6-84.8c9.6-14.4 6.4-35.2-8-44.8-14.4-9.6-35.2-6.4-44.8 8l-67.2 99.2-123.2-44.8 65.6-97.6c9.6-14.4 6.4-35.2-8-44.8-14.4-9.6-35.2-6.4-44.8 8l-75.2 112-139.2-51.2c94.4-68.8 150.4-152 184-227.2z" p-id="2187" fill="#515a6e" /></svg></i>
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
  position: absolute;
  bottom: 50px;
  left: 305px;
  z-index: 999;
  background-color: #ffffff;
  padding: 10px;
  box-shadow: 1px 1px 4px 0 #0a0a0a2e;
  i {
    font-size: 24px;
    height: 26px;
    cursor: pointer;
    margin: 0 10px;
    color: #515a6e;
    display: inline-block;
    vertical-align: middle;
    &:hover {
      color: #2d8cf0;
    }
    &.opacity {
      opacity: .5;
    }
  }
}
</style>
