<template>
  <div class="graph-container">
    <!-- 左侧列表组件区域 -->
    <div class="shapes">
      <h2>新建流程</h2>
      <p class="tips">拖拽组件到右侧画布区</p>
      <div id="stencil" />
    </div>
    <div class="center-container">
      <TaskCanvas :model-id="modelId" :options="taskOptions" @selectComponents="handleSelectComponents" />
    </div>
  </div>
</template>

<script>
import { Addon } from '@antv/x6'
import { getModelComponent } from '@/api/model'
import TaskCanvas from '@/components/TaskCanvas'

export default {
  components: {
    TaskCanvas
    // RightDrawer
  },
  data() {
    return {
      graph: null,
      stencil: null,
      nodeData: null,
      showDataConfig: true,
      components: null, // 左侧组件列表
      modelId: 0,
      taskId: 0,
      projectId: 0,
      projectName: '',
      modelStartRun: false,
      canUndo: false,
      selectComponentList: [],
      componentCode: '',
      destroyed: false,
      startNode: {},
      startData: [],
      componentsList: [],
      container: null,
      isClear: false,
      isDraft: 0,
      taskOptions: {
        isRun: false,
        isEditable: true,
        showTime: false,
        showDraft: true,
        center: true,
        showMinimap: true,
        showSaveButton: true,
        showComponentsDetails: this.isCopy
      }
    }
  },
  computed: {
    isEdit() {
      return this.$route.query.modelId && !this.isCopy
    },
    isCopy() {
      return this.$route.query.isCopy
    }
  },
  async mounted() {
    this.isDraft = this.isEdit ? 1 : 0
    this.modelId = Number(this.$route.query.modelId) || 0
    await this.init()
  },
  destroyed() {
    clearTimeout(this.taskTimer)
    this.selectComponentList = []
    this.destroyed = true
    window.graph.dispose()
    console.log('destroyed model')
  },
  methods: {
    async init() {
      this.graph = window.graph
      // 获取左侧组件列表
      await this.getModelComponentsInfo()

      // 初始化左侧组件列表
      this.initStencil()
      this.initShape()
      this.projectId = Number(this.$route.query.projectId) || 0
    },
    initStencil() {
      const height = document.querySelector('#stencil').offsetHeight
      this.stencil = new Addon.Stencil({
        title: '',
        target: window.graph,
        stencilGraphHeight: height,
        x: 50,
        collapsable: true,
        getDropNode: (node) => {
          const current = node.store.data.data.componentCode
          this.selectComponentList.push(current)
          return node.clone({ keepId: true })
        },
        validateNode: (node) => {
          const current = node.store.data.data.componentCode
          const currentArr = this.selectComponentList.filter(item => item === current)
          if (currentArr.length > 1) { // 存在两个
            this.$message({
              message: '当前组件已存在，请勿重复拖拽',
              type: 'warning'
            })
            return false
          } else {
            return true
          }
        },
        animation: true,
        layoutOptions: {
          columns: 1,
          columnWidth: 180,
          rowHeight: 65,
          dx: 40,
          dy: 110
        }
      })
      const stencilContainer = document.getElementById('stencil')
      stencilContainer.appendChild(this.stencil.container)
    },
    initShape() {
      const imageNodes = this.componentsList?.map((item) =>
        window.graph.createNode({
          id: item.frontComponentId,
          componentCode: item.componentCode,
          shape: 'dag-node',
          label: item.componentName,
          data: item
          // ports
        })

      )
      this.stencil.load(imageNodes)
    },
    // 获取左侧组件
    async getModelComponentsInfo() {
      const { result, code } = await getModelComponent()
      if (code === 0) {
        this.components = result
        this.componentsList = result.slice(1)
      }
    },
    handleSelectComponents(data) {
      this.selectComponentList = data
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .x6-widget-stencil-title{
  background: transparent
}

::v-deep .x6-widget-stencil{
  background: transparent;
}

::v-deep .x6-widget-minimap-viewport{
  border-color: #409EFF;
}
.center-container{
  flex: 1;
  overflow: hidden;
  display: flex;
  height: 100%;
  position: relative;
}
.flowContainer{
  position: relative;
}
.minimap-container{
  position: absolute;
  bottom: 0;
  right: 0;
}
.x6-widget-stencil-title{
  background: transparent;
  display: none;
}
.graph-container {
  position: relative;
  height: calc(100vh - 50px);
  padding: 0;
  overflow: hidden;
  display: flex;
  background-color: #fff;
  .shapes{
    position: relative;
    .tips{
      position: absolute;
      top: 60px;
      left: 0;
      right: 0;
      z-index: 10;
      color: #999;
      font-size: 12px;
      text-align: center;
      line-height: 40px;
    }
    h2{
      position: absolute;
      left: 0;
      top: 10px;
      height: 50px;
      z-index: 10;
      padding-left: 20px;
    }
  }
  .shapes, .components{
    width: 260px;
    position: relative;
  }
}
.dnd-rect{
  width: 180px;
  height: 40px;
  border: 1px solid #000;
}
@keyframes running-line {
  to {
    stroke-dashoffset: -1000;
  }
}
.not-clickable{
  cursor: default;
  pointer-events: none;
}
</style>
