<template>
  <div class="app-container">
    <div class="graph-container">

      <!-- 左侧列表组件区域 -->
      <div class="shapes" :class="{'not-clickable': modelStartRun}">
        <h2>新建模型</h2>
        <p class="tips">拖拽组件到右侧画布区</p>
        <div id="stencil" />
      </div>

      <div id="flowContainer" ref="containerRef" class="container" />
      <!--右侧工具栏-->
      <right-drawer :list-loading="rightInfoLoading" :show-data-config="showDataConfig" :node-data="nodeData" :model-data="modelData" :select-cell="selectCell" :class="{'not-clickable': modelStartRun}" @change="handelChange" />
      <!--流程图工具栏-->
      <tool-bar @save="saveFn(1)" @zoomIn="zoomInFn" @zoomOut="zoomOutFn" @run="run" @clear="clearFn" />
    </div>
  </div>
</template>

<script>
import { Graph, Addon, FunctionExt, Shape } from '@antv/x6'
import '@antv/x6-vue-shape'
import DagNodeComponent from './components/DagNode.vue'
// import './graph/shape'
// import data from './graph/data'
import ToolBar from './components/ToolBar/index.vue'
import RightDrawer from './components/RightDrawer'
import { getModelComponent, saveModelAndComponent, getModelComponentDetail, deleteModel, getProjectResourceData, getProjectAuthedeList, runTaskModel, getTaskModelComponent } from '@/api/model'
// import { getProjectDetail } from '@/api/project'

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
      selectCell: {},
      nodeData: null,
      showDataConfig: false,
      components: {}, // 左侧组件列表
      saveParams: {}, // 保存接口需要参数
      modelComponents: [], // 草稿详情
      modelData: { // 右侧面板模型信息
        trainType: 1
      },
      modelId: 0,
      graphData: {},
      projectId: 0,
      isComplete: false,
      modelStartRun: false,
      timmer: null,
      canUndo: false,
      resourceList: [],
      taskTimer: null,
      saveComponents: [], // 记录保存时有几个组件，用来判断任务是否轮询完成
      hasReady: false,
      resourceId: 0,
      rightInfoLoading: false,
      selectComponentList: []
    }
  },
  async mounted() {
    await this.init()
    this.initToolBarEvent()
    this.getModelComponentDetail()

    this.taskTimer = window.setInterval(() => {
      if (this.modelStartRun) { // 模型开始运行
        setTimeout(this.getTaskModelComponent(), 0)
      }
    }, 1500)
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
            groups: {
              in: {
                position: 'top',
                attrs: {
                  circle: {
                    r: 4,
                    magnet: true,
                    stroke: '#C2C8D5',
                    strokeWidth: 1,
                    fill: '#fff'
                  }
                }
              },
              out: {
                position: 'bottom',
                attrs: {
                  circle: {
                    r: 4,
                    magnet: true,
                    stroke: '#C2C8D5',
                    strokeWidth: 1,
                    fill: '#fff'
                  }
                }
              }
            }
          }
        },
        true
      )
      // 获取左侧组件列表
      await this.getModelComponentsInfo()
      // 初始化画布
      const container = this.$refs.containerRef
      const width = container.offsetWidth
      const height = container.offsetHeight
      this.graph = new Graph({
        container: container,
        width: width || 800,
        height: height || 600,
        panning: { // 拖拽平移
          enabled: true,
          eventTypes: ['leftMouseDown', 'mouseWheel']
        },
        grid: {
          size: 10,
          visible: true,
          type: 'mesh',
          args: [
            {
              color: '#cccccc',
              thickness: 1
            },
            {
              color: '#cccccc',
              thickness: 1,
              factor: 4
            }
          ]
        },
        mousewheel: {
          enabled: true,
          modifiers: 'ctrl',
          factor: 1.1,
          maxScale: 1.5,
          minScale: 0.5
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
        clipboard: {
          enabled: true
        },
        keyboard: {
          enabled: true
        }
      })

      // 初始化左侧组件列表
      this.initStencil()
      this.initShape()

      // 画布事件初始化
      this.initEvent()
      // 居中显示画布
      this.graph.centerContent()
    },
    initStencil() {
      this.stencil = new Addon.Stencil({
        title: '',
        target: this.graph,
        stencilGraphWidth: 260,
        stencilGraphHeight: 600,
        x: 50,
        collapsable: true,
        getDragNode: (node) => {
          const current = node.store.data.data.componentCode
          this.selectComponentList.push(current)
          console.log('getDropNode', this.selectComponentList)
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
      const imageNodes = this.components.map((item) =>
        graph.createNode({
          id: item.componentCode,
          shape: 'dag-node',
          label: item.componentName,
          data: item,
          attrs: {
            label: {
              text: item.componentName
            }
          },
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
    // 画布事件初始化
    initEvent() {
      const { graph } = this
      const container = document.getElementById('flowContainer')
      graph.on('node:click', ({ node }) => {
        this.showDataConfig = true
        this.selectCellData = node.store.data.data
        this.nodeData = this.selectCellData

        const componentCode = this.nodeData.componentCode || ''
        const result = this.modelComponents.find(item => item.componentCode === componentCode)

        console.log('node:click1111', result)
        // 设置已保存值value
        result && result.componentValues.map(item => {
          this.nodeData.componentTypes.find(i => {
            if (i.typeCode === item.key && item.val !== '') {
              i.inputValue = item.val
            }
          })
        })
        if (componentCode === 'dataAlignment') { // 数据对齐，请求数据
          // this.getProjectAuthedeList()
          // if (this.projectId) {
          //   this.getProjectResourceData()
          // }
          this.projectId = Number(this.$route.query.projectId) || 0
          console.log('projectId', this.$route.query.projectId)
          this.getProjectResourceData()
        }
        // if (result && result.componentValues[0]) {
        //   this.nodeData.componentTypes[0].inputValue = result.componentValues[0].val
        // }
      })

      graph.on('blank:click', () => {
        this.showDataConfig = false
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
        console.log('selectComponentList 11', this.selectComponentList)
        if (cells.length) {
          const currentCode = cells[0].store.data.data.componentCode
          const index = this.selectComponentList.indexOf(currentCode)
          if (index !== -1) {
            this.selectComponentList.splice(index, 1)
          }

          console.log('selectComponentList 22', this.selectComponentList)
          graph.removeCells(cells)
        }
      })

      // graph.on('edge:connected', ({ edge }) => {
      //   edge.attr({
      //     line: {
      //       strokeDasharray: ''
      //     }
      //   })
      // })

      graph.on('node:change:data', ({ node }) => {
        const edges = graph.getIncomingEdges(node)
        const { componentState } = node.getData()
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
    getProjectAuthedeList() {
      this.rightInfoLoading = true
      const index = this.nodeData.componentTypes.findIndex(item => item.typeCode === 'projectId')
      this.projectId = Number(this.$route.query.projectId)
      getProjectAuthedeList({ projectId: this.projectId, pageSize: 20 }).then(res => {
        const projectList = res.result.data

        const options = []
        projectList.forEach(item => {
          options.push({
            key: item.projectId,
            val: item.projectName
          })
        })
        console.log('nodeData', this.nodeData)
        this.nodeData.componentTypes[index].inputValue = this.projectId
        this.nodeData.componentTypes[index].inputValues = options
        this.rightInfoLoading = false
      })
    },
    getProjectResourceData() {
      console.log('projectId', this.projectId)
      getProjectResourceData({ projectId: this.projectId }).then(res => {
        this.projectName = res.result.projectName
        this.resourceList = res.result.resource
        const index = this.nodeData.componentTypes.findIndex(item => item.typeCode === 'projectName')
        const dataIndex = this.nodeData.componentTypes.findIndex(item => item.typeCode === 'selectData')
        this.nodeData.componentTypes[index].inputValue = this.projectName
        this.resourceId = this.nodeData.componentTypes[dataIndex].inputValue
        console.log('resourceId', this.resourceId)
        const options = []
        this.resourceList.forEach(item => {
          options.push({
            key: item.resourceId.toString(),
            val: item.resourceName
          })
        })
        // 设置资源选项
        this.nodeData.componentTypes[dataIndex].inputValues = options
        const yOptions = []
        const yIndex = this.nodeData.componentTypes.findIndex(item => item.typeCode === 'yField')
        const curResource = this.resourceList.find(item => item.resourceId === this.resourceId)
        // 设置y值字段选项
        curResource && curResource.yfile.forEach(item => {
          yOptions.push({
            key: item.yid.toString(),
            val: item.yname
          })
        })
        this.nodeData.componentTypes[yIndex].inputValues = yOptions
        // const resourceId = this.nodeData.componentTypes[dataIndex].inputValues[0].key
        // this.nodeData.componentTypes[dataIndex].inputValue = resourceId
        // const yField = this.nodeData.componentTypes[yIndex].inputValues[0].key
        // this.nodeData.componentTypes[yIndex].inputValue = yField
      })
    },
    handelChange(typeCode, data) {
      this.setProjectData(typeCode, data)
    },
    setProjectData(typeCode, data) {
      const dataIndex = this.nodeData.componentTypes.findIndex(item => item.typeCode === 'selectData')
      const componentTypes = this.nodeData.componentTypes
      const yIndex = componentTypes.findIndex(item => item.typeCode === 'yField')
      if (typeCode === 'projectName') {
        this.projectId = data.inputValue
        console.log('111', this.projectId)
        this.getProjectDetail()
        // 切换项目时初始化资源和y值字段
        this.nodeData.componentTypes[dataIndex].inputValue = ''
        this.nodeData.componentTypes[dataIndex].inputValues = []
        this.nodeData.componentTypes[yIndex].inputValue = ''
        this.nodeData.componentTypes[yIndex].inputValues = []
      } else if (typeCode === 'selectData') {
        // 切换资源时初始化y值字段
        this.nodeData.componentTypes[yIndex].inputValue = ''
        this.nodeData.componentTypes[yIndex].inputValues = []
        const curResource = this.resourceList.find(item => item.resourceId === data.inputValue)
        const yOptions = []
        console.log('curResource', curResource)
        curResource && curResource.yfile.forEach(item => {
          yOptions.push({
            key: item.yid.toString(),
            val: item.yname
          })
        })
        this.nodeData.componentTypes[yIndex].inputValues = yOptions
      }
    },
    destroy() {
      this.graph.dispose()
    },
    // 工具组件事件
    // 放大
    zoomInFn() {
      this.graph.zoom(0.1)
      this.canZoomOut = true
    },
    // 缩小
    zoomOutFn() {
      if (!this.canZoomOut) return
      const Num = Number(this.graph.zoom().toFixed(1))

      if (Num > 0.1) {
        this.graph.zoom(-0.1)
      } else {
        this.canZoomOut = false
      }
    },
    // 清除画布
    clearFn() {
      console.log('clearFn', this.modelId)
      if (this.modelStartRun) { // 模型运行中不可操作
        this.$message({
          message: '模型运行中',
          type: 'warning'
        })
        return
      }
      if (!this.modelId) {
        this.$message({
          message: '当前画布为空，无可清除组件',
          type: 'warning'
        })
        return
      }
      deleteModel({ modelId: this.modelId }).then(res => {
        this.graph.clearCells()
        location.reload()
      })
    },
    initToolBarEvent() {
      const { history } = this.graph
      history.on('change', () => {
        this.canUndo = history.canUndo()
        this.canRedo = history.canRedo()
        if (!this.modelStartRun) { // 模型运行中不自动保存
          this.saveFn(0)
        }
      })
      this.graph.bindKey('ctrl+z', () => {
        if (history.canUndo()) {
          history.undo()
        }
        return false
      })
    },
    run() {
      if (this.modelStartRun) { // 模型运行中不可操作
        this.$message({
          message: '模型运行中',
          type: 'warning'
        })
        return
      }
      if (!this.modelId) {
        this.$message({
          message: '当前画布为空，无法运行，请绘制',
          type: 'warning'
        })
        return
      }
      runTaskModel({ modelId: this.modelId }).then(res => {
        this.modelStartRun = true
        clearInterval(this.timmer)
        this.$notify({
          message: '开始运行',
          type: 'info',
          duration: 5000
        })
      })
    },
    getTaskModelComponent() {
      getTaskModelComponent({ modelId: this.modelId }).then(res => {
        const result = res.result
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
            console.log('taskResult', taskResult)
            node.setData({
              ...data,
              componentState: componentState,
              complete: true
            })
          }
        })
        if (taskResult.length === result.length) { // 所有任务运行完成，停止轮询
          clearInterval(this.taskTimer)
          this.$notify({
            message: '模型运行完成',
            type: 'success',
            duration: 3000
          })
          // 跳转详情页？
          setTimeout(() => {
            this.toModelDetail(this.modelId)
          }, 3000)
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
      const res = await getModelComponent()
      this.components = res.result
      console.log('获取左侧组件 components', res.result)
    },
    // 获取模型组件详情
    async getModelComponentDetail() {
      const res = await getModelComponentDetail()
      if (res.result) {
        this.$confirm('当前用户存在历史记录，是否加载?', '提示', {
          confirmButtonText: '是',
          cancelButtonText: '否',
          closeOnClickModal: false,
          type: 'warning'
        }).then(() => {
          const { modelId, modelDesc, taskName = '', trainType, modelComponents, modelPointComponents } = res.result
          this.modelId = modelId
          console.log('getModelComponentDetail', res.result)
          this.modelComponents = modelComponents
          this.modelPointComponents = modelPointComponents
          console.log('trainType', trainType)
          console.log('getModelComponentDetail modelComponents', this.modelComponents)
          this.modelData = {
            modelId,
            taskName,
            modelDesc,
            trainType
          }
          this.initGraphShape()
        }).catch(() => {
          this.modelId = res.result.modelId
          this.clearFn()
        })
      }
    },
    initGraphShape() {
      const graphData = []
      this.modelComponents.forEach(item => {
        if (item.shape === 'dag-node') {
          const { componentCode, coordinateX, coordinateY, width, height, componentName } = item
          const { componentTypes, isMandatory } = this.components.find(item => {
            return item.componentCode === componentCode
          })
          graphData.push({
            id: item.frontComponentId,
            'position': {
              x: coordinateX,
              y: coordinateY
            },
            'size': {
              width,
              height
            },
            'attrs': {
              'text': {
                'text': componentName
              }
            },
            'shape': 'dag-node',
            'ports': ports,
            'data': {
              componentCode: item.componentCode,
              componentName: item.componentName,
              componentTypes: componentTypes,
              isMandatory: isMandatory
            },
            'zIndex': 1
          })
        }
      })

      this.modelPointComponents.forEach(item => {
        if (item.shape === 'edge') {
          graphData.push({
            id: item.frontComponentId,
            'shape': 'edge',
            'attrs': lineAttr,
            'zIndex': 0,
            'source': item.input,
            'target': item.output
          })
        }
      })
      this.graph.fromJSON(graphData)
      // this.graph.resetCells(graphData.cells)
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
      if (shape === 'dag-node') {
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
    // 保存模板文件
    saveFn(isDraft) {
      if (this.modelStartRun) { // 模型运行中不可操作
        this.$message({
          message: '模型运行中',
          type: 'warning'
        })
        return
      }
      if (isDraft && !this.modelId) {
        this.$message({
          message: '当前画布为空，无法保存，请绘制',
          type: 'warning'
        })
        return
      }
      this.saveParams.param = {
        modelId: this.modelId,
        taskName: this.modelData.taskName,
        modelName: this.modelData.modelName,
        modelDesc: this.modelData.modelDesc,
        trainType: this.modelData.trainType,
        isDraft: isDraft || 0, // 0 草稿
        modelComponents: this.modelComponents || []
      }
      const data = this.graph.toJSON()
      const { cells } = data

      const modelComponents = [] // 块数据
      const modelPointComponents = [] // 线数据

      const edgeList = cells.filter(item => item.shape === 'edge')
      for (let i = 0; i < cells.length; i++) {
        const item = cells[i]
        const { position, data, size, shape } = item
        const componentValues = []
        item.frontComponentId = item.id
        if (shape === 'dag-node') {
          const { componentCode, componentName, componentTypes } = data
          const { input, output } = this.filterFn(item, edgeList, cells)

          console.log('componentTypes', componentTypes)
          for (let i = 0; i < componentTypes.length; i++) {
            const item = componentTypes[i]
            // 判断所选组件值是否为空
            if (!item.inputValue && isDraft) {
              this.$message({
                message: `${componentName}不能为空`,
                type: 'error'
              })
              return
            }
            componentValues.push({
              key: item.typeCode,
              val: item.inputValue
            })
            if (item.typeCode === 'projectName') {
              componentValues.push({
                key: 'projectId',
                val: this.projectId
              })
            }
          }
          // format 参数
          modelComponents.push({
            frontComponentId: item.id,
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
        if (shape === 'edge') {
          modelPointComponents.push({
            frontComponentId: item.id,
            shape,
            input: item.source,
            output: item.target
          })
        }
      }
      this.saveParams.param.modelComponents = modelComponents
      this.saveParams.param.modelPointComponents = modelPointComponents
      console.log(this.saveParams)
      saveModelAndComponent(JSON.stringify(this.saveParams)).then(res => {
        this.modelId = res.result.modelId
        this.$notify({
          message: '保存成功',
          type: 'success',
          duration: '1000'
        })
      })
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
.x6-widget-stencil-title{
    background: transparent;
    display: none;
  }
.app-container {
  .mask{
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background: transparent;
    z-index: 100;
  }
  position: relative;
  height: calc(100vh - 50px);
  padding: 0;
  overflow: hidden;
  .graph-container{
    display: flex;
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
.container{
  flex: 1;
  height: 800px;
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
