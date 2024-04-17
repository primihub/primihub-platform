<script>
import ResourceTable from '@/components/ResourceTable'
import ProviderOrganDialog from '@/components/ProviderOrganDialog'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'
import { mapGetters } from 'vuex'
import { removeResource, saveProject } from '@/api/project'
import { resourceFilePreview } from '@/api/resource'
import { getArrDifSameValue } from '@/utils/common'

export default {
  name: '',
  components: {
    ResourceTable,
    ProviderOrganDialog,
    ResourcePreviewDialog,
    ProjectResourceDialog
  },
  props: {
    data: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      id: '',
      organs: [],
      selectedOrganId: '',
      isCreator: false,
      projectStatus: -1, // 项目状态 0审核中 1可用 2关闭 11 全部可用 12 部分可用,
      showResourceDelBtn: false,
      previewDialogVisible: false,
      resourceDialogVisible: false,
      resourceList: [],
      previewList: [],
      projectOrgans: []
    }
  },
  computed: {
    currentOrgan() {
      return this.organs.filter(item => item.organId === this.selectedOrganId)[0]
    },
    thisInstitution() {
      return this.userOrganId === this.selectedOrganId
    },
    hasAddPermission() {
      return this.buttonPermissionList.includes('ModelCreate')
    },
    ...mapGetters([
      'buttonPermissionList',
      'userOrganId'
    ])
  },
  watch: {
    data: {
      handler (val) {
        if (!val.id) return
        const { status, creator, organs, id } = val
        this.isCreator = creator
        this.organs = organs
        this.projectStatus = status
        this.id = id
        this.showResourceDelBtn = this.userOrganId === organs.find(item => item.participationIdentity === 1).organId
        this.resourceList[this.selectedOrganId] = organs.filter(item => item.organId === this.selectedOrganId)[0].resources
      },
      immediate: true
    }
  },
  mounted() {
    this.selectedOrganId = this.userOrganId
  },
  methods: {
    /** switch organ */
    handleSwitchOrgan() {
      this.projectOrgans = []
      this.resourceList[this.selectedOrganId] = this.currentOrgan.resources
    },

    /** open add resource dialog */
    handelOpenResourceDialog(id) {
      this.selectedOrganId = id
      this.resourceDialogVisible = true
    },

    /** close add resource dialog */
    handleCloseResourceDialog() {
      this.resourceDialogVisible = false
    },

    /** submit add resource dialog */
    handleSubmitResourceDialog(data) {
      const different = getArrDifSameValue(this.currentOrgan.resources, data, 'resourceId')
      if (different.length) {
        this.projectOrgans.push({
          organId: this.selectedOrganId,
          participationIdentity: this.selectedOrganId === this.userOrganId ? 1 : 2,
          resourceIds: different.map(r => r.resourceId)
        })
        this.saveProject()
      }
      this.handleCloseResourceDialog()
    },

     /**
     * type string
     * 0: add resource
     * 1: edit description
    */
    async saveProject(type = 0) {
      const params = {id: this.id, projectOrgans: this.projectOrgans }
      const { code } = await saveProject(params)
      if (code === 0) {
        this.$message.success(type ? '修改成功' : '添加成功')
        this.$emit('refresh')
      }
    },

    /** Click the button in the Resource Table to preview resources */
    async handleResourcePreview({ resourceId }) {
      const { result } = await resourceFilePreview({ resourceId })
      this.previewList = result?.dataList
      this.previewDialogVisible = true
    },

    /** Click the button in the Resource Table to remove resource */
    handleResourceRemove({ id }) {
      removeResource({ id }).then(({ code = -1 }) => {
        if (code === 0) {
          const index = this.resourceList[this.selectedOrganId].findIndex(item => item.id === id)
          this.resourceList[this.selectedOrganId].splice(index, 1)
          this.$message.success('已移除')
        }
      })
    },

    statusStyle(status) {
      const statusStyleObj = {
        0: 'status-0 el-icon-refresh',
        1: 'status-1 el-icon-circle-check',
        2: 'status-2 el-icon-circle-close'
      }
      return statusStyleObj[status]
    },
  },
}
</script>
<template>
  <div>
    <el-tabs v-model="selectedOrganId" type="border-card" class="tabs" @tab-click="handleSwitchOrgan">
      <el-tab-pane v-for="item in organs" :key="item.organId" :name="item.organId" :label="item.organId">
        <p slot="label">{{ item.participationIdentity === 1 ? '发起方：':'协作方：' }}{{ item.organName }}
          <span :class="statusStyle(item.auditStatus)">{{ item.creator?'': item.auditStatus === 0 ? '等待审核中':item.auditStatus === 2?'已拒绝':'' }}</span>
        </p>
        <el-button v-if="isCreator" type="primary" plain :disabled="projectStatus === 2" @click="handelOpenResourceDialog(item.organId)">添加资源到此项目</el-button>
        <ResourceTable
          max-height="480"
          :project-audit-status="currentOrgan.auditStatus === 1"
          :this-institution="thisInstitution"
          :creator="isCreator"
          :show-preview-button="thisInstitution"
          :show-delete-button="showResourceDelBtn"
          :selected-data="currentOrgan.resources"
          row-key="resourceId"
          :data="resourceList[selectedOrganId]"
          @preview="handleResourcePreview"
          @remove="handleResourceRemove"
        />
      </el-tab-pane>
    </el-tabs>

     <!-- add resource dialog -->
     <ProjectResourceDialog
      ref="dialogRef"
      top="10px"
      width="800px"
      :selected-data="resourceList[selectedOrganId]"
      title="选择资源"
      :organ-id="selectedOrganId"
      :show-preview-button="thisInstitution"
      :visible="resourceDialogVisible"
      @close="handleCloseResourceDialog"
      @submit="handleSubmitResourceDialog"
      @preview="handleResourcePreview"
    />

    <!-- preview dialog -->
    <ResourcePreviewDialog
      :data="previewList"
      :visible.sync="previewDialogVisible"
      append-to-body
      width="1000px"
      @close="previewDialogVisible = false"
    />
  </div>
</template>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
@import "~@/styles/resource.scss";
.status-0{
  color: $mainColor;
}
.status-1{
  color: #67C23A;
}
.status-2{
  color: #F56C6C;
}
</style>
