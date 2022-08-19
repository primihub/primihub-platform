<template>
  <div class="app-container">
    <div class="graph-container">
      <!-- 左侧列表组件区域 -->
      <div class="shapes" :class="{'not-clickable': modelStartRun}">
        <h2>新建模型</h2>
        <p class="tips">拖拽组件到右侧画布区</p>
        <div id="stencil" />
      </div>
      <div class="center-container">
        <!--流程图工具栏-->
        <tool-bar ref="toolBarRef" @save="toolBarSave()" @zoomIn="zoomInFn" @zoomOut="zoomOutFn" @run="run" @clear="clearFn" />
        <div id="flowContainer" ref="containerRef" class="container" />
        <div ref="mapContainerRef" class="minimap-container" />
      </div>

      <!--右侧工具栏-->
      <right-drawer ref="drawerRef" :list-loading="rightInfoLoading" :show-data-config="showDataConfig" :node-data="nodeData" :class="{'not-clickable': modelStartRun}" @save="saveFn(0)" @change="handleChange" />

    </div>
  </div>
</template>

<script>
import { Graph, Addon, FunctionExt, Shape } from '@antv/x6'
import '@antv/x6-vue-shape'
import DagNodeComponent from './components/nodes/DagNode.vue'
import StartNodeComponent from './components/nodes/StartNode.vue'
import ToolBar from './components/ToolBar/index.vue'
import RightDrawer from './components/RightDrawer'
import { getModelComponent, saveModelAndComponent, getModelComponentDetail, getProjectResourceData, runTaskModel, getTaskModelComponent, getProjectResourceOrgan } from '@/api/model'

const lineAttr = { // 线样式
  'line': {
    'stroke': '#A2B1C3',
    'targetMarker': {
      'name': 'block',
      'width': 12,
      'height': 8
    }
  }
}
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
    ToolBar,
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
      graphData: { cells: [] },
      projectId: 0,
      projectName: '',
      isComplete: false,
      modelStartRun: false,
      canUndo: false,
      resourceList: [],
      yOptions: [],
      taskTimer: null,
      saveComponents: [], // 记录保存时有几个组件，用来判断任务是否轮询完成
      hasReady: false,
      resourceId: 0,
      rightInfoLoading: false,
      selectComponentList: [],
      componentCode: '',
      organs: [],
      destroyed: false,
      startNode: {},
      startData: [],
      componentsList: [],
      requiredComponents: [],
      container: null,
      modelRunValidated: true,
      isClear: false,
      isDraft: 0,
      isLoadHistory: true
    }
  },
  computed: {
    isEdit() {
      return !!this.$route.query.modelId
    }
  },
  async mounted() {
    this.isDraft = this.isEdit ? 1 : 0
    this.modelId = this.$route.query.modelId
    await this.init()
    this.initToolBarEvent()
    this.getModelComponentDetail()
  },
  destroyed() {
    clearTimeout(this.taskTimer)
    this.selectComponentList = []
    this.destroyed = true
    this.graph.dispose()
    console.log('destroyed model')
  },
  methods: {
    async init() {
      Graph.registerNode(
        'dag-node',
        {
          inherit: 'vue-shape',
          width: 180,
          height: 50,
          component: {
            template: `<dag-node-component />`,
            components: {
              DagNodeComponent
            }
          },
          ports: {
            ...ports
          }
        },
        true
      )
      // 获取左侧组件列表
      await this.getModelComponentsInfo()
      // 初始化画布
      this.container = this.$refs.containerRef
      const minimapContainer = this.$refs.mapContainerRef
      const width = this.container.scrollWidth
      const height = this.container.scrollHeight
      this.$refs.toolBarRef.width = width
      this.graph = new Graph({
        container: this.container,
        width: width,
        height: height,
        autoResize: true,
        snapline: true,
        scroller: {
          enabled: true,
          pageBreak: false,
          pannable: true
        },
        minimap: {
          enabled: true,
          container: minimapContainer,
          scalable: false,
          width: 200,
          height: 200,
          padding: 0
        },
        // panning: true,
        background: {
          color: '#F5F7FA'
        },
        grid: {
          type: 'doubleMesh',
          size: 10,
          visible: true,
          args: [
            {
              color: '#E7E8EA',
              thickness: 1
            },
            {
              color: '#CBCED3',
              thickness: 1,
              factor: 5
            }
          ]
        },
        mousewheel: {
          enabled: true,
          modifiers: ['ctrl', 'meta'],
          minScale: 0.5,
          maxScale: 2
        },
        highlighting: {
          magnetAdsorbed: {
            name: 'stroke',
            args: {
              attrs: {
                fill: '#fff',
                stroke: '#31d0c6',
                strokeWidth: 4
              }
            }
          }
        },
        connecting: {
          router: {
            name: 'manhattan',
            args: {
              padding: 1
            }
          },
          connector: {
            name: 'rounded',
            args: {
              radius: 8
            }
          },
          anchor: 'center',
          connectionPoint: 'anchor',
          allowBlank: false,
          snap: {
            radius: 20
          },
          validateMagnet({ magnet }) {
            return magnet.getAttribute('port-group') !== 'in'
          },
          createEdge() {
            return new Shape.Edge({
              attrs: {
                line: {
                  stroke: '#A2B1C3',
                  strokeWidth: 2,
                  targetMarker: {
                    name: 'block',
                    width: 12,
                    height: 8
                  }
                }
              },
              zIndex: 0
            })
          }
        },
        selecting: {
          enabled: true,
          multiple: true,
          rubberEdge: true,
          rubberNode: true,
          modifiers: 'shift',
          rubberband: true
        },
        history: true,
        clipboard: true,
        keyboard: true
      })

      if (this.components) {
        this.registerStartNode()
        this.addStartNode()
      }

      // 初始化左侧组件列表
      this.initStencil()
      this.initShape()

      // 画布事件初始化
      this.initEvent()
      // 居中显示画布
      // this.graph.centerContent()
      this.projectId = Number(this.$route.query.projectId) || 0
    },
    registerStartNode() {
      this.startNode = this.components.filter(item => item.componentCode === 'start')[0]
      this.nodeData = this.startNode
      Graph.registerNode(
        'start-node',
        {
          inherit: 'vue-shape',
          width: 120,
          height: 40,
          component: {
            template: `<start-node-component />`,
            components: {
              StartNodeComponent
            }
          },
          ports: { ...ports }
        },
        true
      )
    },
    addStartNode() {
      const width = this.container.scrollWidth
      // 60 = start node width
      const x = width * 0.5 - 60
      this.startData = this.components.filter(item => item.componentCode === 'start')[0]
      this.graph.addNode({
        x: x,
        y: 150,
        shape: 'start-node',
        componentCode: this.startData.componentCode,
        label: this.startData.componentName,
        data: this.startData,
        ports
      })
    },
    initStencil() {
      const height = document.querySelector('#stencil').offsetHeight
      this.stencil = new Addon.Stencil({
        title: '',
        target: this.graph,
        // stencilGraphWidth: 330,
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
      const { graph } = this
      const imageNodes = this.componentsList.map((item) =>
        graph.createNode({
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
    showPorts(ports, show) {
      for (let i = 0, len = ports.length; i < len; i = i + 1) {
        ports[i].style.visibility = show ? 'visible' : 'hidden'
      }
    },
    async getProjectResourceOrgan() {
      const res = await getProjectResourceOrgan({ projectId: this.projectId })
      if (res.code === 0) {
        this.organs = res.result
      }
    },
    // 画布事件初始化
    initEvent() {
      const { graph } = this
      const container = document.getElementById('flowContainer')
      graph.on('node:click', async({ node }) => {
        this.nodeData = node.store.data.data
      })

      graph.on(
        'node:mouseenter',
        FunctionExt.debounce(() => {
          const ports = container.querySelectorAll(
            '.x6-port-body'
          )
          this.showPorts(ports, true)
        }),
        500
      )
      graph.on('node:mouseleave', () => {
        const ports = container.querySelectorAll(
          '.x6-port-body'
        )
        this.showPorts(ports, false)
      })

      graph.on('node:collapse', ({ node, e }) => {
        e.stopPropagation()
        node.toggleCollapse()
        const collapsed = node.isCollapsed()
        const cells = node.getDescendants()
        cells.forEach((n) => {
          if (collapsed) {
            n.hide()
          } else {
            n.show()
          }
        })
      })
      graph.bindKey('backspace', () => {
        const cells = graph.getSelectedCells()
        if (cells.length) {
          const currentCode = this.nodeData.componentCode
          // remove duplicates
          this.selectComponentList = [...new Set(this.selectComponentList)]
          const index = this.selectComponentList.indexOf(currentCode)
          if (index !== -1) {
            this.selectComponentList.splice(index, 1)
          }
          graph.removeCells(cells)
        }
        this.nodeData = this.startNode
      })

      graph.on('edge:connected', ({ edge }) => {
        edge.attr({
          line: {
            strokeDasharray: ''
          }
        })
      })

      graph.on('node:change:data', ({ node }) => {
        const edges = graph.getIncomingEdges(node)
        const { componentState } = node.getData()
        // TODO line Animation ?
        edges && edges.forEach((edge) => {
          if (componentState === 2 || componentState === 0) {
            edge.attr('line/strokeDasharray', 5)
            edge.attr('line/style/animation', 'running-line 30s infinite linear')
          } else {
            edge.attr('line/strokeDasharray', '')
            edge.attr('line/style/animation', '')
          }
        })
      })
    },
    getProjectResourceData() {
      getProjectResourceData({ projectId: this.projectId }).then(res => {
        this.projectName = res.result.projectName
        this.resourceList = res.result.resource
      })
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
      const { cells } = this.graph.toJSON()
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
      this.isDraft = this.isEdit ? 1 : 0
      this.nodeData = this.startNode
      this.saveFn()
    },
    initToolBarEvent() {
      const { history } = this.graph
      history.on('change', () => {
        this.canUndo = history.canUndo()
        this.canRedo = history.canRedo()
        // model is running or destroyed, don't save the model history
        if (!this.modelStartRun && !this.destroyed) {
          this.saveFn()
        }
      })
      this.graph.bindKey(['ctrl+z', 'command+z'], () => {
        if (history.canUndo()) {
          history.undo()
        }
        return false
      })
    },
    checkRunValidated() {
      const data = this.graph.toJSON()
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
    run() {
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
          this.taskTimer = window.setInterval(() => {
            if (this.modelStartRun) { // 模型开始运行
              setTimeout(this.getTaskModelComponent(), 0)
            }
          }, 1500)
        }
      })
    },
    getTaskModelComponent() {
      getTaskModelComponent({ taskId: this.taskId }).then(res => {
        const result = res.result.components
        const taskState = res.result.taskState
        if (taskState === 3) { // task failed
          clearInterval(this.taskTimer)
          this.$notify.closeAll()
          this.$notify({
            message: '运行失败',
            type: 'error',
            duration: 3000
          })
          this.modelStartRun = false
          this.$confirm('任务运行失败, 是否重新运行?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.run()
          }).catch(() => {
          })
          return
        } else {
          const taskResult = []
          result && result.forEach((item) => {
            const { componentCode, complete, componentState } = item
            const nodes = this.graph.getNodes()
            const node = nodes.find(item => item.store.data.data.componentCode === componentCode)
            const data = node.getData()
            node.setData({
              ...data,
              componentState: componentState
            })
            if (complete) {
              taskResult.push(componentCode)
              node.setData({
                ...data,
                componentState: componentState,
                complete: true
              })
            }
          })
          if (taskResult.length === result.length) { // 所有任务运行完成，停止轮询
            clearInterval(this.taskTimer)
            this.$notify.closeAll()
            this.$notify({
              message: '运行完成',
              type: 'success',
              duration: 3000
            })
            // to model task detail page
            setTimeout(() => {
              this.toModelDetail(this.taskId)
            }, 3000)
          }
        }
      })
    },
    toModelDetail(id) {
      this.$router.push({
        path: `/model/detail/${id}`
      })
    },
    // 获取左侧组件
    async getModelComponentsInfo() {
      const { result, code } = await getModelComponent()
      if (code === 0) {
        this.components = result
        this.componentsList = result.slice(1)
        this.requiredComponents = result.filter(item => item.isMandatory === 0)
      }
    },
    // 获取模型组件详情
    async getModelComponentDetail() {
      const params = {
        modelId: this.modelId,
        projectId: this.projectId
      }
      const res = await getModelComponentDetail(params)
      const modelComponents = res.result?.modelComponents
      if (this.isEdit && modelComponents && modelComponents.length > 0) { // It's is edit page
        this.setComponentsDetail(res.result)
      } else if (modelComponents && modelComponents.length > 1) {
        this.$confirm('当前用户存在历史记录，是否加载?', '提示', {
          confirmButtonText: '是',
          cancelButtonText: '否',
          closeOnClickModal: false,
          type: 'warning'
        }).then(() => {
          this.setComponentsDetail(res.result)
        }).catch(() => {
          this.modelId = res.result.modelId
          this.isLoadHistory = false
          this.clearFn()
        })
      }
    },
    setComponentsDetail(data) {
      const { modelId, modelComponents, modelPointComponents } = data
      this.modelId = modelId
      this.modelPointComponents = modelPointComponents
      this.modelComponents = modelComponents
      for (let index = 0; index < this.modelComponents.length; index++) {
        const item = this.modelComponents[index]
        // save the selected components
        this.selectComponentList.push(item.componentCode)
        const posIndex = this.components.findIndex(c => c.componentCode === item.componentCode)
        item.componentValues.map(item => {
          this.components[posIndex].componentTypes.map(c => {
            if (c.typeCode === item.key && item.val !== '') {
              c.inputValue = item.val
            }
          })
        })
      }
      this.initGraphShape()
    },
    initGraphShape() {
      console.log('this.modelComponents', this.modelComponents)
      this.modelComponents && this.modelComponents.forEach(item => {
        const { frontComponentId, componentCode, coordinateX, coordinateY, width, height, componentName, shape } = item
        const { componentTypes, isMandatory } = this.components.find(item => {
          return item.componentCode === componentCode
        })
        this.graphData.cells.push({
          id: frontComponentId,
          frontComponentId,
          label: componentName,
          componentCode,
          position: {
            x: coordinateX,
            y: coordinateY
          },
          size: {
            width,
            height
          },
          shape,
          ports,
          data: {
            componentCode,
            componentName,
            componentTypes,
            isMandatory
          },
          zIndex: 1
        })
      })

      this.modelPointComponents?.forEach(item => {
        if (item.shape === 'edge') {
          this.graphData.cells.push({
            id: item.frontComponentId,
            'shape': 'edge',
            'attrs': lineAttr,
            'zIndex': 0,
            'source': item.input,
            'target': item.output
          })
        }
      })
      this.graph.fromJSON(this.graphData)
    },
    filterFn(item, edgeList, cells) {
      // 1. 在target中找，
      // 找到:有输入 source.cell input 存放
      // 没找到 :
      // 2. 在source中找，
      // 找到: 有输出 target.cell output 存放
      // 没找到: 没有输入 input =[]
      // 同时找到
      // 有输入 source.cell 有输出 target.cell
      const { shape } = item
      const obj = {
        input: [],
        output: []
      }
      if (shape !== 'edge') {
        const frontComponentId = item.frontComponentId
        const inputRes = edgeList.find(item => {
          return item.source.cell === frontComponentId
        })
        const outputRes = edgeList.find(item => {
          return item.target.cell === frontComponentId
        })
        if (inputRes && outputRes) {
          const inputId = outputRes.source.cell
          const outputId = inputRes.target.cell
          const outputData = cells.find(item => item.id === outputId)
          const inputData = cells.find(item => item.id === inputId)
          obj.output.push({
            frontComponentId: outputId,
            componentCode: outputData.data.componentCode,
            componentName: outputData.data.componentName,
            portId: outputRes.target.port,
            pointType: 'edge',
            pointJson: ''
          })
          obj.input.push({
            frontComponentId: inputId,
            componentCode: inputData.data.componentCode,
            componentName: inputData.data.componentName,
            portId: inputRes.source.port,
            pointType: 'edge',
            pointJson: ''
          })
        } else if (inputRes) {
          const outputId = inputRes.target.cell
          const outputData = cells.find(item => item.id === outputId)
          obj.output.push({
            frontComponentId: outputId,
            componentCode: outputData.data.componentCode,
            componentName: outputData.data.componentName,
            portId: inputRes.target.port,
            pointType: 'edge',
            pointJson: ''

          })
        } else if (outputRes) {
          const inputId = outputRes.source.cell
          const inputData = cells.find(item => item.id === inputId)
          obj.input.push({
            frontComponentId: inputId,
            componentCode: inputData.data.componentCode,
            componentName: inputData.data.componentName,
            portId: outputRes.source.port,
            pointType: 'edge',
            pointJson: ''
          })
        }
      }
      return obj
    },
    toolBarSave() {
      this.isDraft = 1
      this.saveFn()
    },
    // 保存模板文件
    saveFn() { // 0 草稿
      const data = this.graph.toJSON()
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
        this.graph.fromJSON(this.graphData)
        const startParams = modelComponents.filter(item => item.componentCode === 'start')[0]
        this.saveParams.param.modelComponents = []
        this.saveParams.param.modelComponents.push(startParams)
        this.selectComponentList = []
      }

      saveModelAndComponent(JSON.stringify(this.saveParams)).then(res => {
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
      })
    },
    handleChange(data) {
      const { cells } = this.graph.toJSON()
      const posIndex = cells.findIndex(item => item.componentCode === data.componentCode)
      cells[posIndex].data = data
      this.graph.fromJSON(cells)
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
