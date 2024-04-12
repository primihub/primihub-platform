<script>
import Info from './components/detail-info.vue'
import FieldInfo from '../components/edit-resource-table.vue'
import SampleInfo from '@/components/ResourcePreviewTable'
import VisiblePartner from './components/visible-partner.vue'
import InvolvedProjects from './components/involved-projects.vue'
import CallLogging from './components/call-logging.vue'
import { getResourceDetail } from '@/api/resource'

export default {
  name: 'OwnResourceDetail',
  components: {
    Info,
    FieldInfo,
    SampleInfo,
    VisiblePartner,
    InvolvedProjects,
    CallLogging
  },
  data() {
    return {
      compTabs: [
        { comp: 'FieldInfo', label: '字段信息' },
        { comp: 'SampleInfo', label: '样本信息' },
        { comp: 'VisiblePartner', label: '可见合作方' },
        { comp: 'InvolvedProjects', label: '参与项目' },
        { comp: 'CallLogging', label: '调用记录' }
      ],
      resourceId: this.$route.params.id,
      currentComp: 'FieldInfo',
      resourceData: {},
      dataList: [], // Sample Info
      fieldList: [], // Field Info
      fusionOrganList: [] // Visible Partner
    }
  },
  computed: {
    currentProp() {
      switch (this.currentComp) {
        case 'FieldInfo':
          return this.fieldList
        case 'SampleInfo':
          return this.dataList
        case 'VisiblePartner':
          return this.fusionOrganList
        default:
          return this.resourceId
      }
    }
  },
  created() {
    this.getResourceDetail()
  },
  methods: {
    /** query resource detail */
    async getResourceDetail() {
      const { code, result } = await getResourceDetail(this.resourceId)
      if (code === 0) {
        const { dataList, fieldList, fusionOrganList, resource } = result
        this.resourceData = resource
        this.dataList = dataList
        this.fieldList = fieldList
        this.fusionOrganList = fusionOrganList
      }
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
    <Info :data="resourceData" />
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
          <component :is="currentComp" ref="stepRef" :data="currentProp" :is-editable="false" />
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
