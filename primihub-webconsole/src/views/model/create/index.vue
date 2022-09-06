<template>
  <div class="app-container">
    {{ modelId }}
    <div class="graph-container">
      <!-- 左侧列表组件区域 -->
      <div class="shapes">
        <h2>新建流程</h2>
        <p class="tips">拖拽组件到右侧画布区</p>
        <div id="stencil" />
      </div>
      <div class="center-container">
        <TaskCanvas class="canvas-container" :model-id="modelId" :options="taskOptions" :save="autoSave" @change="handleNodeChange" @selectComponents="handleSelectComponents" />
        <!--右侧工具栏-->
        <right-drawer v-if="showDataConfig" ref="drawerRef" :node-data="nodeData" />
      </div>
    </div>
  </div>
</template>

<script>
import { Addon } from '@antv/x6'
// import '@antv/x6-vue-shape'
import TaskCanvas from '@/components/TaskCanvas'

import RightDrawer from './components/RightDrawer'
import { getModelComponent, saveModelAndComponent, runTaskModel } from '@/api/model'

const ports = { // 圆点样式
  'groups': {
    'in': {
      'position': 'top',
      'attrs': {
        'circle': {
          'r': 4,
          'magnet': true,
          'stroke': '#5F95FF',
          'strokeWidth': 1,
          'fill': '#fff',
          'style': {
            'visibility': 'hidden'
          }
        }
      }
    },
    'out': {
      'position': 'bottom',
      'attrs': {
        'circle': {
          'r': 4,
          'magnet': true,
          'stroke': '#5F95FF',
          'strokeWidth': 1,
          'fill': '#fff',
          'style': {
            'visibility': 'hidden'
          }
        }
      }
    }
  },
  'items': [
    {
      'group': 'in',
      'id': 'port1'
    },
    {
      'group': 'out',
      'id': 'port2'
    }
  ]
}

export default {
  components: {
    TaskCanvas,
    RightDrawer
  },
  data() {
    return {
      graph: null,
      stencil: null,
      dnd: null,
      showRight: false,
      nodeData: null,
      showDataConfig: true,
      components: null, // 左侧组件列表
      saveParams: {}, // 保存接口需要参数
      modelComponents: [], // 草稿详情
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
      modelRunValidated: true,
      isClear: false,
      isDraft: 0,
      isLoadHistory: true,
      taskOptions: {
        isRun: false,
        isEditable: true,
        showTime: false,
        showDraft: true,
        showComponentsDetails: this.isCopy
      },
      autoSave: false
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
    // this.initToolBarEvent()
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
          data: item,
          ports
        })

      )
      this.stencil.load(imageNodes)
    },
    // 工具组件事件
    // 放大
    zoomInFn() {
      this.graph.zoom(0.1)
    },
    // 缩小
    zoomOutFn() {
      const Num = Number(this.graph.zoom().toFixed(1))

      if (Num > 0.1) {
        this.graph.zoom(-0.1)
      }
    },
    // 清除画布
    clearFn() {
      const { cells } = window.graph.toJSON()
      if (this.modelStartRun) { // 模型运行中不可操作
        this.$message({
          message: '模型运行中',
          type: 'warning'
        })
        return
      }
      if (cells.length <= 1 && this.isLoadHistory) {
        this.$message({
          message: '当前画布为空，无可清除组件',
          type: 'warning'
        })
        return
      }
      this.isClear = true
      // this.isDraft = this.isEdit ? 1 : 0
      this.nodeData = this.startNode
      this.saveFn()
    },
    // 重置画布
    resetFn() {
      this.graph.zoomTo(1)
    },
    toolBarSave() {
      this.saveFn()
    },
    initToolBarEvent() {
      const { history } = window.graph
      history.on('change', () => {
        this.canUndo = history.canUndo()
        this.canRedo = history.canRedo()
        // model is running or destroyed, don't save the model history
        if (!this.modelStartRun && !this.destroyed) {
          this.saveFn()
        }
      })
      window.graph.bindKey(['ctrl+z', 'command+z'], () => {
        if (history.canUndo()) {
          history.undo()
        }
        return false
      })
    },
    checkRunValidated() {
      const data = window.graph.toJSON()
      const { cells } = data
      const startCom = cells.filter(item => item.componentCode === 'start')[0]
      const modelName = startCom.data.componentTypes.filter(item => item.typeCode === 'modelName')[0].inputValue
      const dataSetCom = cells.filter(item => item.componentCode === 'dataSet')
      const value = dataSetCom.length && dataSetCom[0]?.data.componentTypes[0].inputValue !== '' ? JSON.parse(dataSetCom[0]?.data.componentTypes[0].inputValue) : ''
      const initiateResource = value && value.filter(v => v.participationIdentity === 1)[0]
      const providerResource = value && value.filter(v => v.participationIdentity === 2)[0]
      // model is running, can't run again
      if (this.modelStartRun) {
        this.$message({
          message: '模型运行中',
          type: 'warning'
        })
        this.modelRunValidated = false
      } else if (!this.modelId && this.isCopy) { // copy model task, must save
        this.$message({
          message: '模型未保存，请保存后再次尝试',
          type: 'warning'
        })
        this.modelRunValidated = false
      } else if (cells.length === 1) { // model is empty or cleared, can't run
        this.$message({
          message: '当前画布为空，无法运行，请绘制',
          type: 'warning'
        })
        this.modelRunValidated = false
      } else if (modelName === '') {
        this.$message({
          message: `运行失败：请输入模型名称`,
          type: 'error'
        })
        this.modelRunValidated = false
      } else if (!dataSetCom) {
        this.$message({
          message: `请选择数据集`,
          type: 'error'
        })
        this.modelRunValidated = false
      } else if (!(initiateResource && providerResource)) {
        const msg = !initiateResource ? '请选择发起方数据集' : !providerResource ? '请选择协作方数据集' : '请选择数据集'
        this.$message({
          message: msg,
          type: 'error'
        })
        this.modelRunValidated = false
      } else {
        this.modelRunValidated = true
      }
    },
    async run() {
      // 运行前触发保存
      this.isDraft = 1
      await this.saveFn()
      this.checkRunValidated()
      if (!this.modelRunValidated) return
      runTaskModel({ modelId: this.modelId }).then(res => {
        if (res.code !== 0) {
          this.$message({
            message: res.msg,
            type: 'error'
          })
          return
        } else {
          this.taskId = res.result.taskId
          this.modelStartRun = true
          this.$notify.closeAll()
          this.$notify({
            message: '开始运行',
            type: 'info',
            duration: 5000
          })
          // to model task detail page
          setTimeout(() => {
            this.toModelDetail(this.taskId)
          }, 1000)
        }
      })
    },
    toModelDetail(id) {
      this.$router.push({
        name: `ModelDetail`,
        params: { taskId: id },
        query: { from: '0', modelId: this.modelId } // 跳转携带参数，用于预览tab展示 0 创建任务 1 生成模型
      })
    },
    // 获取左侧组件
    async getModelComponentsInfo() {
      const { result, code } = await getModelComponent()
      if (code === 0) {
        this.components = result
        this.componentsList = result.slice(1)
      }
    },
    // 保存模板文件
    async saveFn() { // 0 草稿
      const data = window.graph.toJSON()
      const { cells } = data
      if (this.modelStartRun) { // 模型运行中不可操作
        this.$message({
          message: '模型运行中',
          type: 'warning'
        })
        return
      }
      if ((this.isDraft && !this.isEdit) && cells.length <= 1) {
        this.$message({
          message: '当前画布为空，无法保存，请绘制',
          type: 'warning'
        })
        return
      }

      this.saveParams.param = {
        projectId: this.projectId,
        modelId: this.modelId,
        isDraft: this.isDraft
      }
      this.saveParams.param.modelComponents = this.modelComponents

      const modelComponents = [] // 块数据
      const modelPointComponents = [] // 线数据

      const edgeList = cells.filter(item => item.shape === 'edge')
      for (let i = 0; i < cells.length; i++) {
        const item = cells[i]
        const { position, data, size, shape } = item
        const componentValues = []
        item.frontComponentId = item.id
        if (shape === 'edge') {
          modelPointComponents.push({
            frontComponentId: item.frontComponentId,
            shape,
            input: item.source,
            output: item.target
          })
        } else {
          const { componentCode, componentName, componentTypes } = data
          const { input, output } = this.filterFn(item, edgeList, cells)

          for (let i = 0; i < componentTypes.length; i++) {
            const item = componentTypes[i]
            componentValues.push({
              key: item.typeCode,
              val: item.inputValue
            })
          }
          // format 参数
          modelComponents.push({
            frontComponentId: item.frontComponentId,
            coordinateX: position.x,
            coordinateY: position.y,
            width: size.width,
            height: size.height,
            shape,
            componentCode,
            componentName,
            componentValues,
            input: input,
            output: output
          })
        }
      }
      this.saveParams.param.modelComponents = modelComponents

      this.saveParams.param.modelPointComponents = modelPointComponents
      if (this.isClear) {
        this.startData = cells.filter(item => item.componentCode === 'start')[0]
        this.graphData.cells = []
        this.graphData.cells.push(this.startData)
        window.graph.fromJSON(this.graphData)
        const startParams = modelComponents.filter(item => item.componentCode === 'start')[0]
        this.saveParams.param.modelComponents = []
        this.saveParams.param.modelComponents.push(startParams)
        this.selectComponentList = []
      }

      const res = await saveModelAndComponent(JSON.stringify(this.saveParams))
      if (res.code === 0) {
        this.modelId = res.result.modelId
        this.$notify.closeAll()
        this.$notify({
          message: '保存成功',
          type: 'success',
          duration: '1000'
        })
      } else {
        this.$message({
          message: res.msg,
          type: 'error'
        })
      }
      this.isClear = false
      this.isDraft = this.isEdit ? 1 : 0
    },
    handleSelectComponents(data) {
      this.selectComponentList = data
    },
    handleNodeChange(data) {
      this.nodeData = data
      this.showDataConfig = true
    },
    handleChange(data) {
      const { cells } = window.graph.toJSON()
      const posIndex = cells.findIndex(item => item.componentCode === data.componentCode)
      cells[posIndex].data = data
      window.graph.fromJSON(cells)
      this.saveFn()
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
  .container{
    width: 100%;
    height: 100%;
  }
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
.app-container {
  position: relative;
  height: calc(100vh - 50px);
  padding: 0;
  overflow: hidden;
  .graph-container{
    display: flex;
    height: 100%;
    // position: relative;
  }
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
