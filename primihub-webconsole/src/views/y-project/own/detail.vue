<script>
import Info from '../components/detail-info.vue'
import OrganAndDataset from '../components/organ-datasets.vue'
import TaskFlow from '../components/task-flow.vue'
import TaskList from '../components/task-list.vue'
import ServicesAndModels from '../components/services-models.vue'
import { getProjectDetail } from '@/api/project'

export default {
  name: 'OwnResourceDetail',
  components: {
    Info,
    OrganAndDataset,
    TaskFlow,
    TaskList,
    ServicesAndModels,
  },
  data() {
    return {
      compTabs: [
        { comp: 'OrganAndDataset', label: '机构和数据集' },
        { comp: 'TaskFlow', label: '任务流' },
        { comp: 'TaskList', label: '任务列表' },
        { comp: 'ServicesAndModels', label: '服务和模型' },
      ],
      projectId: this.$route.params.id,
      currentComp: 'OrganAndDataset',
      projectData: {},
    }
  },
  computed: {
    currentProp() {
      switch (this.currentComp) {
        case 'OrganAndDataset':
          return {...this.projectData}
        case 'TaskFlow':
          return {...this.projectData}
        default:
          return this.projectId
      }
    }
  },
  created() {
    this.getProjectDetail()
  },
  methods: {
    /** query project detail */
    async getProjectDetail() {
      const { code, result } = await getProjectDetail({ id: this.projectId })
      if (code === 0) this.projectData = result
    },

    /** change component */
    handelTabChange(comp) {
      this.currentComp = comp
    }
  }
}
</script>

<template>
  <div class="app-container">
    <Info :data="projectData" />
    <div class="resource-detail-tabs-container">
      <ul class="resource-detail-tabs-list">
        <li
          v-for="{comp, label} in compTabs"
          :key="comp"
          :class="['resource-detail-tabs-item', { active: comp === currentComp }]"
          @click="handelTabChange(comp)"
        >{{ label }}</li>
      </ul>
      <div class="resource-detail-tabs-content">
        <keep-alive>
          <component :is="currentComp" :data="currentProp" @refresh="getProjectDetail()" />
        </keep-alive>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.resource-detail-tabs-container{
  min-height: 500px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background-color: #fff;
  overflow: hidden;
}
.resource-detail-tabs-content{
  min-height: 300px;
  padding: 30px 20px;
}
.resource-detail-tabs-list{
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 20px;
  font-size: 14px;
  font-weight: 600;
  line-height: 50px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #f5f7fa;
  color: #909399;
}
.resource-detail-tabs-item{
  position: relative;
  bottom: 1px;
  padding: 0 20px;
  border-bottom-width: 2px;
  border-bottom-style: solid;
  border-bottom-color: transparent;
  cursor: pointer;
  &.active{
    border-bottom-color: #409eff;
    color: #409eff;
  }
}
</style>
