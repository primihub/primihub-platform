<template>
  <div v-loading="listLoading" class="container">
    <section class="infos">
      <el-descriptions :column="2" title="基本信息">
        <el-descriptions-item label="项目ID">
          {{ projectId }}
        </el-descriptions-item>
        <el-descriptions-item label="项目名称">
          <template v-if="creator">
            <EditInput style="width: 70%;" show-word-limit maxlength="20" :value="projectName" @change="handleProjectNameChange" />
          </template>
          <template v-else>{{ projectName }}</template>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          {{ creator? '项目发起方' : '项目协作方' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ createDate }}
        </el-descriptions-item>
        <el-descriptions-item label="项目描述">
          <template v-if="creator">
            <EditInput style="width: 70%;" type="textarea" show-word-limit maxlength="200" :value="projectDesc" @change="handleProjectDescChange" />
          </template>
          <template v-else>{{ projectDesc }}</template>
        </el-descriptions-item>
      </el-descriptions>
      <ProjectAudit v-if="isShowAuditForm" class="audit" :project-id="currentOrgan.id" />
    </section>
    <section>
      <el-tabs v-model="tabName" class="tab-container" @tab-click="handleTabClick">
        <el-tab-pane label="参与机构" name="organ">
          <el-button v-if="creator" type="primary" class="add-provider-button" :disabled="projectStatus === 2" @click="openProviderOrganDialog">新增协作者</el-button>
          <el-tabs v-model="activeName" type="border-card" class="tabs" @tab-click="handleClick">
            <el-tab-pane v-for="item in organs" :key="item.organId" :name="item.organId" :label="item.organId">
              <p slot="label">{{ item.participationIdentity === 1 ? '发起方：':'协作方：' }}{{ item.organName }}
                <span :class="statusStyle(item.auditStatus)">{{ item.creator?'': item.auditStatus === 0 ? '等待审核中':item.auditStatus === 2?'已拒绝':'' }}</span>
              </p>
              <el-button v-if="item.auditStatus === 1 && creator" type="primary" plain :disabled="projectStatus === 2" @click="openDialog(item.organId)">添加资源到此项目</el-button>
              <p v-if="item.participationIdentity !== 1 && item.auditOpinion" class="auditOpinion" :class="{'danger': item.auditStatus === 2}">审核建议：{{ formatEmoji(item.auditOpinion) }}</p>
              <ResourceTable
                max-height="480"
                :project-audit-status="projectAuditStatus"
                :this-institution="thisInstitution"
                :creator="creator"
                :show-preview-button="thisInstitution"
                :show-delete-button="showResourceDelBtn"
                :selected-data="selectedData"
                row-key="resourceId"
                :data="resourceList[selectedOrganId]"
                @preview="handlePreview"
                @remove="handleRemove"
                @handleRefused="handleResourceRefused"
                @handleAgree="handleAgree"
              />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <el-tab-pane label="任务列表" name="modelTask">
          <el-button v-if="creator" type="primary" class="add-provider-button" :disabled="projectStatus === 2" @click="toModelCreate">新建任务</el-button>
          <ModelTaskList :is-creator="creator" :project-status="projectStatus" />
        </el-tab-pane>
        <el-tab-pane label="衍生数据" name="derivedData">
          <DerivedDataTable v-if="tabName === 'derivedData'" max-height="480" :data="derivedDataResourceList" />
        </el-tab-pane>
      </el-tabs>
    </section>

    <!-- add resource dialog -->
    <ProjectResourceDialog
      ref="dialogRef"
      top="10px"
      width="800px"
      :selected-data="resourceList[selectedOrganId]"
      title="选择资源"
      :organ-id="selectedOrganId"
      :show-preview-button="thisInstitution"
      :visible="dialogVisible"
      @close="handleDialogCancel"
      @submit="handleDialogSubmit"
      @preview="handlePreview"
    />
    <!-- add provider organ dialog -->
    <ProviderOrganDialog v-show="providerOrganDialogVisible" :selected-data="providerOrganIds" :visible.sync="providerOrganDialogVisible" title="添加协作方" :data="organList" @submit="handleProviderOrganSubmit" @close="closeProviderOrganDialog" @delete="handleProviderOrganDelete" />
    <!-- resource refused dialog-->
    <ResourceApprovalDialog v-if="resourceApprovalDialogVisible" :visible="resourceApprovalDialogVisible" :resource-id="localResourceId" @close="handleResourceApprovalDialogClose" @submit="handleResourceApprovalDialogSubmit" />
    <!-- preview dialog -->
    <ResourcePreviewDialog
      :data="previewList"
      :visible.sync="previewDialogVisible"
      append-to-body
      width="1000px"
      @close="closeDialog"
    />
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex'
import { getProjectDetail, approval, saveProject, closeProject, removeResource, removeOrgan, getDerivationResourceList } from '@/api/project'
import { resourceFilePreview } from '@/api/resource'
import { deCodeEmoji } from '@/utils/emoji-regex'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'
import ProviderOrganDialog from '@/components/ProviderOrganDialog'
import ResourceApprovalDialog from '@/components/ResourceApprovalDialog'
import ResourceTable from '@/components/ResourceTable'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'
import ModelTaskList from '@/components/ModelTaskList'
import ProjectAudit from '@/components/ProjectAudit'
import DerivedDataTable from '@/components/DerivedDataTable'
import EditInput from '@/components/editInput'

export default {
  components: {
    ProjectResourceDialog,
    ProviderOrganDialog,
    ResourceTable,
    ResourcePreviewDialog,
    ModelTaskList,
    ProjectAudit,
    ResourceApprovalDialog,
    DerivedDataTable,
    EditInput
  },
  data() {
    return {
      tabName: 'organ',
      listLoading: false,
      isShowAuditForm: false,
      projectName: '',
      projectId: '',
      id: '',
      projectAuditStatus: '',
      projectDesc: '',
      userName: '',
      createDate: '',
      activeName: '',
      participationIdentity: '',
      selectedData: [],
      resourceList: [],
      derivedDataResourceList: [],
      providerOrganDialogVisible: false,
      previewDialogVisible: false,
      dialogVisible: false,
      resourceApprovalDialogVisible: false,
      previewList: [],
      selectedOrganId: null,
      organList: [],
      tableButtons: ['preview'],
      userInfo: [],
      organs: [],
      showResourceDelBtn: false,
      currentOrgan: [],
      differents: [],
      saveParams: {
        id: '',
        projectOrgans: [] // save params
      },
      creator: false,
      tabPosition: 'left',
      providerOrganIds: [],
      projectStatus: -1, // 项目状态 0审核中 1可用 2关闭 11 全部可用 12 部分可用,
      resourceId: 0,
      localResourceId: 0
    }
  },
  computed: {
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
  async created() {
    await this.fetchData()
  },
  methods: {
    handleProjectDescChange({ change, value }) {
      if (change) {
        this.saveParams.projectDesc = value
        this.saveProject(1)
      }
    },
    handleProjectNameChange({ change, value }) {
      if (change) {
        this.saveParams.projectName = value
        this.saveProject(1)
      }
    },
    async getDerivationResourceList() {
      this.loading = true
      this.derivedDataResourceList = []
      const params = {
        projectId: this.id
      }
      const { code, result } = await getDerivationResourceList(params)
      if (code === 0) {
        if (result.length > 0) {
          this.derivedDataResourceList = result
        }
      }
      this.loading = false
    },
    handleResourceApprovalDialogClose() {
      this.resourceApprovalDialogVisible = false
    },
    async handleResourceApprovalDialogSubmit() {
      await this.fetchData()
      this.resourceApprovalDialogVisible = false
    },
    formatEmoji(text) {
      return deCodeEmoji(text)
    },
    statusStyle(status) {
      return status === 0 ? 'status-0 el-icon-refresh' : status === 1 ? 'status-1 el-icon-circle-check' : status === 2 ? 'status-2 el-icon-circle-close' : ''
    },
    async openProviderOrganDialog() {
      this.providerOrganDialogVisible = true
    },
    showAuditForm() {
      return this.organs.filter(item => item.organId === this.selectedOrganId)[0].auditStatus === 0
    },
    handleAgree(row) {
      this.approval({
        type: 2,
        id: row.id,
        auditOpinion: '',
        auditStatus: 1
      })
      // location.reload()
    },
    handleResourceRefused(row) {
      this.localResourceId = row.id
      this.resourceApprovalDialogVisible = true
    },
    handleTabClick() {
      // this.tabName = label
      if (this.tabName === 'derivedData') {
        this.getDerivationResourceList()
      }
    },
    handleClick({ label = '' }) {
      this.differents = []
      this.saveParams.projectOrgans = []
      this.selectedOrganId = label
      this.currentOrgan = this.organs.filter(item => item.organId === this.selectedOrganId)[0]
      this.resourceList[this.selectedOrganId] = this.currentOrgan.resources
      this.isShowAuditForm = this.thisInstitution && this.currentOrgan.auditStatus === 0 && this.projectStatus !== 2
      this.projectAuditStatus = this.currentOrgan.auditStatus === 1
      this.selectedData = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
    },
    async handlePreview(row) {
      this.resourceId = row.resourceId
      await this.resourceFilePreview()
      this.previewDialogVisible = true
    },
    handleRemove({ organId, id }) {
      this.removeResource(id)
    },
    removeResource(id) {
      removeResource({ id }).then(({ code = -1 }) => {
        if (code === 0) {
          const index = this.resourceList[this.selectedOrganId].findIndex(item => item.id === id)
          this.resourceList[this.selectedOrganId].splice(index, 1)
          this.$message({
            message: '已移除',
            type: 'success'
          })
        }
      })
    },
    closeProject(id) {
      closeProject({ id }).then(({ code = -1 }) => {
        if (code === 0) {
          this.$message({
            message: '已移除',
            type: 'success'
          })
        }
      })
    },
    async resourceFilePreview() {
      const res = await resourceFilePreview({ resourceId: this.resourceId })
      this.previewList = res.result?.dataList
    },
    openDialog(id) {
      this.selectedOrganId = id
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      this.differents = this.getArrDifSameValue(this.selectedData, data, 'resourceId')
      if (this.differents.length > 0) {
        this.saveParams.projectOrgans.push({
          organId: this.selectedOrganId,
          participationIdentity: this.selectedOrganId === this.userOrganId ? 1 : 2,
          resourceIds: this.differents.map(r => r.resourceId)
        })
        this.saveProject()
      }
      this.selectedData = data
      this.dialogVisible = false
    },
    closeDialog() {
      this.previewDialogVisible = false
    },
    closeProviderOrganDialog() {
      this.providerOrganDialogVisible = false
    },
    handleProviderOrganSubmit(data) {
      const differents = this.getArrDifSameValue(this.providerOrganIds, data, 'globalId')
      console.log('handleProviderOrganSubmit differents', differents)
      if (differents.length > 0) {
        this.saveParams.projectOrgans = differents.map(item => {
          return {
            organId: item.globalId,
            participationIdentity: 2
          }
        })
        const success = this.saveProject()
        if (success) {
          data.map(item => {
            this.organs.push({
              id: item.id,
              organId: item.globalId,
              organName: item.globalName,
              participationIdentity: 2
            })
          })
        }
        this.providerOrganIds = data
      }
      this.providerOrganDialogVisible = false
    },
    handleProviderOrganDelete(ids) {
      // 全部取消
      if (ids.length > 1) {
        ids.map(id => {
          this.removeOrgan(id)
        })
      } else {
        this.removeOrgan(ids[0])
      }
    },
    removeOrgan(id) {
      removeOrgan({ id }).then(async res => {
        if (res.code === 0) {
          this.$message({
            message: '删除成功',
            type: 'success'
          })
          this.selectedOrganId = this.userOrganId
          await this.fetchData()
        }
      })
    },
    /**
     * type string
     * 0: add resource
     * 1: edit description
    */
    saveProject(type = 0) {
      // const { projectName, projectDesc, projectOrgans } = this
      this.saveParams.id = this.list.id
      saveProject(this.saveParams).then(res => {
        if (res.code === 0) {
          this.$message({
            message: type === 0 ? '添加成功' : '修改成功',
            type: 'success'
          })

          this.fetchData()
          return true
        } else {
          return false
        }
      })
    },
    getProjectOrgans() {
      const projectOrgans = []
      for (const key in this.resourceList) {
        const item = this.resourceList[key]
        projectOrgans.push({
          organId: key,
          participationIdentity: 2,
          resourceIds: item.map(r => r.resourceId)
        })
      }
      projectOrgans.filter(item => item.organId === this.dataForm.initiateOrganId)[0].participationIdentity = 1
      return projectOrgans
    },
    approval({ type, id, auditStatus, auditOpinion }) {
      const params = {
        type,
        id,
        auditStatus,
        auditOpinion
      }
      approval(params).then(({ code, result }) => {
        if (code === 0) {
          this.$message({
            type: 'success',
            message: auditStatus === 1 ? '授权成功' : ''
          })
          this.fetchData()
        }
      })
    },
    fetchData() {
      this.listLoading = true
      this.id = this.$route.params.id || this.list.id
      getProjectDetail({ id: this.id }).then(res => {
        if (res.code === 0) {
          this.listLoading = false
          this.list = res.result
          const { projectName, projectDesc, userName, createDate, organs, creator, status, projectId } = this.list
          this.projectId = projectId
          this.creator = creator
          this.projectName = projectName
          this.projectStatus = status
          this.projectDesc = projectDesc
          this.userName = userName
          this.createDate = createDate
          this.organs = organs
          // 发起方拥有资源删除权限
          this.showResourceDelBtn = this.userOrganId === this.organs.find(item => item.participationIdentity === 1).organId
          this.selectedOrganId = this.selectedOrganId || this.userOrganId
          this.activeName = this.selectedOrganId
          this.resourceList[this.selectedOrganId] = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
          this.selectedData = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
          this.currentOrgan = this.organs.filter(item => item.organId === this.selectedOrganId)[0]
          this.projectAuditStatus = this.currentOrgan.auditStatus === 1
          this.isShowAuditForm = this.projectStatus !== 2 && this.currentOrgan.auditStatus === 0
          this.providerOrganIds = this.organs.filter(item => item.participationIdentity === 2)
          this.providerOrganIds = this.providerOrganIds?.map(item => {
            return {
              id: item.id,
              globalId: item.organId,
              globalName: item.organName
            }
          })
          this.SET_STATUS(this.projectStatus)
        }
      }).catch(() => {
        this.listLoading = false
      })
    },
    // compare array diffrent
    getArrDifSameValue(arr1, arr2, key) {
      const result = []
      for (let i = 0; i < arr2.length; i++) {
        const obj = arr2[i]
        const id = obj[key]
        let isExist = false
        for (let j = 0; j < arr1.length; j++) {
          const aj = arr1[j]
          const n = aj[key]
          if (n === id) {
            isExist = true
            break
          }
        }
        if (!isExist) {
          result.push(obj)
        }
      }
      return result
    },
    toModelCreate() {
      this.$router.push({
        name: 'ModelCreate',
        query: { projectId: this.list.id }
      })
    },
    ...mapMutations('project', ['SET_STATUS'])
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
@import "~@/styles/resource.scss";
p{
  margin-block-start: 0;
    margin-block-end: 0;
}
h2{
  color: $mainColor;
  font-weight: normal;
  margin-block-start: 0;
  margin-block-end: 0.5em;
}
section{
  background-color: #ffffff;
  padding: 30px;
  margin-bottom: 30px;
}
::v-deep .el-table{
  margin-top: 20px;
}
::v-deep .tab-container .el-tabs__item {
  font-size: 16px;
}
.preview-dialog .el-dialog__header{
  padding: 0;
}
.infos-title{
  margin-bottom: 10px;
}
.infos-des{
  color: rgba(153, 153, 153, 0.85);
  font-size: 12px;
}
.identity{
  color: $dangerColor;
}
.danger{
  color: $dangerColor;
}
.organs{
  position: relative;
  .add-provider-button{
    position: absolute;
    top: 40px;
    right: 30px;
    z-index: 100;
  }
}
.top-container{
  width: 100%;
  display: flex;
  justify-content: space-between;
}
::v-deep .el-descriptions-item__container{
  margin: 5px 10px 0 0;
}
::v-deep .el-descriptions :not(.is-bordered) .el-descriptions-item__cell{
  padding-bottom: 0;
}
::v-deep .el-descriptions-row{
  margin-bottom: 20px;
}
::v-deep .el-descriptions-item__container{
  flex-wrap: wrap;
}
::v-deep .el-tabs--border-card>.el-tabs__content{
  background-color: #ffffff;
  min-height: 200px;
}
::v-deep .el-tabs--left .el-tabs__header.is-left{
  margin-right: 0;
}
::v-deep .el-tabs--left.el-tabs--border-card .el-tabs__item.is-left{
  margin: 0;
  border-top: none;
}
.audit{
  max-width: 500px;
}
.auditOpinion{
  font-size: 14px;
  margin: 10px 0;
}
.tabs{
  background-color: #F5F7FA;
  margin-top: 20px;
}
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
