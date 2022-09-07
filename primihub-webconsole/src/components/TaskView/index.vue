<template>
  <div>
    <TaskCanvas class="canvas-container" :model-data="modelComponent" :model-id="modelId" :state="state" :options="taskOptions" :restart-run="restartRun" @change="handleNodeChange" @success="handleTaskComplete" />
    <!--右侧工具栏-->
    <right-drawer v-if="showDataConfig" ref="drawerRef" :node-data="nodeData" :options="drawerOptions" />
  </div>
</template>

<script>
import TaskCanvas from '@/components/TaskCanvas'
import RightDrawer from '@/components/TaskCanvas/RightDrawer'

export default {
  name: 'TaskView',
  components: {
    TaskCanvas,
    RightDrawer
  },
  props: {
    modelId: {
      type: Number,
      default: 0
    },
    state: {
      type: Number,
      default: 0
    },
    modelComponent: {
      type: Array,
      default: () => {}
    },
    restartRun: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showDataConfig: false,
      nodeData: null,
      taskOptions: {
        showTime: true,
        showMinimap: false,
        isEditable: false,
        isRun: true,
        showDraft: false,
        showComponentsDetails: true,
        toolbarOptions: {
          background: false,
          position: 'right',
          buttons: ['zoomIn', 'zoomOut', 'reset']
        }
      },
      drawerOptions: {
        showSaveButton: false,
        isEditable: false
      }
    }
  },
  methods: {
    handleNodeChange(data) {
      this.nodeData = data
      this.showDataConfig = true
    },
    handleTaskComplete() {
      this.$emit('success')
    }
  }

}
</script>
<style lang="scss" scoped>
// .container{
//   position: relative;
//   width: 100%;
// }
</style>
