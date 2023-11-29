<template>
  <div class="container">
    <div class="graph-container">
      <!-- 左侧列表组件区域 -->
      <div class="shapes">
        <h2>新建流程</h2>
        <p class="tips">拖拽组件到右侧画布区</p>
        <div id="stencil" />
      </div>
      <div class="center-container">
        <TaskCanvas ref="canvasRef" :model-id="modelId" :options="taskOptions" :components-detail="componentDetail" @mounted="init" @selectComponents="handleSelectComponents" @saveParams="getSaveParams" />
      </div>
    </div>
    <div class="footer-buttons">
      <el-button type="primary" @click="importDraft">导入草稿</el-button>
      <el-button plain @click="saveDraft">保存草稿</el-button>
      <el-button plain @click="saveAsDraft">另存草稿</el-button>
    </div>
    <SaveDraftDialog v-if="draftDialogVisible" :draft-name="draftName" width="480px" :title="draftDialogTitle" :visible.sync="draftDialogVisible" @close="handleDraftDialogClose" @submit="handleDraftDialogSubmit" />
    <ImportDraftDialog :visible.sync="importDraftDialogVisible" @close="handleImportDraftDialogClose" @submit="handleImportDraftDialogSubmit" />
  </div>
</template>

<script>
import { Addon } from '@antv/x6'
import { getModelComponent, saveOrUpdateComponentDraft } from '@/api/model'
import TaskCanvas from '@/components/TaskCanvas'
import SaveDraftDialog from '@/components/SaveDraftDialog'
import ImportDraftDialog from '@/components/ImportDraftDialog'

export default {
  components: {
    TaskCanvas,
    SaveDraftDialog,
    ImportDraftDialog
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
      },
      draftDialogVisible: false,
      draftDialogTitle: '保存为草稿',
      componentImage: '',
      draftName: '',
      saveDraftType: 0, // 0:保存  1:另存
      importDraftDialogVisible: false,
      draftId: '',
      componentJson: {},
      componentDetail: null
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
  },
  destroyed() {
    clearTimeout(this.taskTimer)
    this.selectComponentList = []
    this.destroyed = true
    this.graph && this.graph.dispose()
    console.log('destroyed model')
  },
  methods: {
    // 保存模板文件
    saveOrUpdateComponentDraft() {
      const params = {
        draftId: this.saveDraftType === 0 ? this.draftId : '',
        draftName: this.draftName,
        componentJson: JSON.stringify(this.componentJson),
        componentImage: this.componentImage
      }
      saveOrUpdateComponentDraft(params).then(res => {
        if (res.code === 0) {
          this.draftId = res.result.draftId
          this.$message({
            message: '保存成功',
            type: 'success'
          })
        } else {
          this.$message({
            message: res.msg,
            type: 'error'
          })
        }
      })
    },
    importDraft() {
      this.importDraftDialogVisible = true
    },
    handleImportDraftDialogClose() {
      this.importDraftDialogVisible = false
    },
    handleImportDraftDialogSubmit(data) {
      this.draftId = data.draftId
      this.draftName = data.draftName
      this.componentJson = JSON.parse(data.componentJson)
      // only same project can load data
      if (this.componentJson.projectId !== this.projectId) {
        this.componentJson = this.deleteComponentsVal(this.componentJson)
      }
      this.componentDetail = this.componentJson
      this.importDraftDialogVisible = false
    },
    handleDraftDialogClose() {
      this.draftDialogVisible = false
    },
    handleDraftDialogSubmit(data) {
      this.draftDialogVisible = false
      this.draftName = data.draftName
      this.toPNG()
    },
    beforeSaveDraft() {
      // 只有开始组件不可保存
      if (this.componentJson.modelComponents && this.componentJson.modelComponents.length > 1) {
        return true
      } else {
        this.$message({
          message: '请先绘制流程',
          type: 'warning'
        })
        return false
      }
    },
    saveDraft() {
      if (this.beforeSaveDraft()) {
        this.draftDialogTitle = '保存为草稿'
        this.saveDraftType = 0
        this.draftDialogVisible = true
      }
    },
    saveAsDraft() {
      if (this.beforeSaveDraft() && this.draftId === '') {
        this.$message({
          message: '请先保存草稿',
          type: 'warning'
        })
        return
      }
      if (this.beforeSaveDraft()) {
        this.draftDialogTitle = '保存为新草稿'
        this.saveDraftType = 1
        this.draftDialogVisible = true
      }
    },
    toPNG() {
      window.graph.toPNG((dataUri) => {
        this.componentImage = dataUri
        this.saveOrUpdateComponentDraft()
      }, {
        width: 265,
        height: 160,
        backgroundColor: '#F5F7FA',
        padding: {
          top: 20,
          bottom: 20,
          right: 50,
          left: 50
        },
        // 默认true, 但是会抖动
        copyStyles: false,
        // 解决生成图片样式丢失问题，官方临时解决方案 https://antv-x6.gitee.io/zh/docs/tutorial/epilog#%E5%AF%BC%E5%87%BA%E5%9B%BE%E7%89%87%E6%A0%B7%E5%BC%8F%E7%BC%BA%E5%A4%B1
        stylesheet: `
            .start-node {
              color: #00a387;
              font-weight: bold;
              display: flex;
              padding: 0px 0 0 15px;
              width: 120px;
              height: 40px;
              line-height: 1;
              box-shadow: 0 2px 5px 1px rgba(0, 0, 0, 0.06);
            }
            .start-node-icon{
              margin-right: 5px;
              vertical-align: middle;
            }
            .start-node-label{
              height: 24px;
              vertical-align: middle;
              line-height: 24px;
            }
            .dag-node {
              display: flex;
              width: 181px;
              height: 52px;
              justify-content: flex-start;
              padding: 15px 0 0 10px;
              background: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAALUAAAA0CAYAAADMv3nUAAAACXBIWXMAAAsTAAALEwEAmpwYAAABuUlEQVR4nO3dv2pTcRiH8Se/o6UtzaFakBapUyXglqVkdLC9h96Eg1CQdra4FvFOnKqoqw4OQpD+2Rpoh/4JSUkODSf1FiwGDnl5PlfwHZ7h3d5aa/uO/a2T58B74BWQI02XHvAFeLveXDuu/fh13Ljoz/z89PtJ/udslm/vqt4n/buyHNPrDzg9u6QoRl2g9aBzPffh49en+eC26nnS/WVZ4tHiAnl9jvZRZ7EoRnvpoL300qA17bIsY3VlCWAzHZ7PPqx6kDQJeX0eYCEVo6qnSJORZQmAVPEOaeKMWuEYtcIxaoVj1ArHqBWOUSsco1Y4Rq1wjFrhGLXCMWqFY9QKx6gVjlErHKNWOEatcIxa4Ri1wjFqhWPUCseoFY5RKxyjVjhGrXCMWuEYtcIxaoVj1ArHqBWOUSsco1Y4Rq1wjFrhGLXCMWqFY9QKx6gVjlErHKNWOEatcJJPnBVFWY4BSI1lHzkrhl5/AHCTNl5cfp+fqXqO9H/KsqRzfgVwkFYfD1+/2TztNZ8VeIpo2pTlmOvuDe2jDsPhbRfYrbW279jfOmkAe8AGUK90pXR/feAzsLPeXDv8CwQWX2pVpe6nAAAAAElFTkSuQmCC') no-repeat;
              box-sizing: border-box;
            }
            .dag-node img {
              width: 15px;
              height: 15px;
              flex-shrink: 0;
              margin-left: 8px;
              margin-top: 3px;
              vertical-align: middle;
            }
            .dag-node .label {
              width: 90px;
              height: 20px;
              font-size: 14px;
              line-height: 20px;
              vertical-align: middle;
              text-align: center;
            }
            .require{
              color: red;
              margin: 0 5px 0 10px;
              font-size: 20px;
              height: 20px;
              vertical-align: middle;
            }
         `
      })
    },
    async init() {
      this.graph = this.$refs.canvasRef.graph
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
        target: this.graph,
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
        this.graph.createNode({
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
    },
    deleteComponentsVal(data, name = 'dataSet') {
      const dataSetComIndex = data?.modelComponents.findIndex(item => item.componentCode === name)
      data?.modelComponents[dataSetComIndex]?.componentValues.map(item => {
        item.val = ''
        return item
      })
      return data
    },
    getSaveParams(data) {
      this.componentJson = data
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
  border-color: #1677FF;
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
  height: calc(100vh - 80px - 103px);
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
.footer-buttons{
  display: flex;
  background: #fafafa;
  padding: 0 30px;
  height: 80px;
  align-items: center;
  border-top: 1px solid #e9e9e9;
}
</style>
