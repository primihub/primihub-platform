<template>
  <div class="container">
    <el-row :gutter="20">
      <el-col :span="6">
        <section class="infos">
          <div class="infos-title">
            <h2>{{ projectName }}</h2>
            <p class="infos-des">{{ projectId }}</p>
          </div>
          <el-descriptions :column="1" label-class-name="detail-title">
            <el-descriptions-item label="创建人">{{ userName }}</el-descriptions-item>
            <el-descriptions-item label="项目描述">{{ projectDesc }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ createDate }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="isShowAuditForm" class="audit">
            <el-form ref="auditForm" :model="auditForm">
              <el-form-item label="参与合作审核意见:">
                <el-input ref="auditInput" v-model="auditForm.auditOpinion" type="textarea" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="handleSubmit">同意</el-button>
                <el-button type="danger" size="small" @click="handleRefused">拒绝</el-button>
              </el-form-item>
            </el-form>
          </div>
        </section>

      </el-col>
      <el-col :span="18">
        <section class="organs">
          <h3>参与机构</h3>
          <el-button v-if="creator" type="primary" class="add-provider-button" @click="openProviderOrganDialog">添加更多协作者</el-button>
          <el-tabs v-model="activeName" type="border-card" class="tabs" @tab-click="handleClick">
            <el-tab-pane v-for="item in organs" :key="item.organId" :name="item.organId" :label="item.organId">
              <p slot="label">{{ item.participationIdentity === 1 ? '发起方：':'协作方：' }}{{ item.organName }}
                <span v-if="item.creator" class="identity">{{ item.creator ? '&lt;创建者&gt;':'' }}</span>
                <span v-else :class="statusStyle(item.auditStatus)">{{ item.creator?'': item.auditStatus === 0 ? '等待审核中':item.auditStatus === 2?'已拒绝':'' }}</span>
                <!-- <span class="identity">{{ item.auditStatus === 0? '&lt;等待审核中&gt;':item.auditStatus === 2?'&lt;已拒绝&gt;':'' }}</span> -->
              </p>
              <el-button v-if="item.auditStatus === 1 && creator" type="primary" plain @click="openDialog(item.organId)">添加资源到此项目</el-button>
              <p v-if="item.participationIdentity !== 1 && item.auditOpinion" class="auditOpinion" :class="{'danger': item.auditStatus === 2}">审核建议：{{ item.auditOpinion }}</p>
              <ResourceTable
                v-if="item.auditStatus === 1 && (item.resources && item.resources.length >0 || resourceList[selectedOrganId].length>0)"
                :project-audit-status="projectAuditStatus"
                :this-institution="thisInstitution"
                :creator="creator"
                :server-address="serverAddress"
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
        </section>
        <section class="organs">
          <h3>模型列表</h3>
          <el-button v-if="creator" type="primary" class="add-provider-button" @click="toModelCreate">添加模型</el-button>
          <Model />
        </section>
      </el-col>
    </el-row>

    <!-- add resource dialog -->
    <ProjectResourceDialog ref="dialogRef" top="10px" :selected-data="resourceList[selectedOrganId]" title="添加资源" :server-address="serverAddress" :organ-id="selectedOrganId" :visible="dialogVisible" @close="handleDialogCancel" @submit="handleDialogSubmit" />
    <!-- add provider organ dialog -->
    <ProviderOrganDialog :selected-provider-organ-list="selectedData" :visible.sync="providerOrganDialogVisible" title="添加协作方" :data="organList" @submit="handleProviderOrganSubmit" @close="closeProviderOrganDialog" />
    <!-- preview dialog -->
    <el-dialog
      :visible.sync="previewDialogVisible"
      :before-close="closeDialog"
      append-to-body
      width="1000px"
    >
      <ResourcePreviewTable :data="previewList" height="500" />
    </el-dialog>
    <!-- preview dialog -->
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import { getProjectDetail, approval, saveProject, closeProject, removeResource } from '@/api/project'
import { resourceFilePreview } from '@/api/resource'
import { findMyGroupOrgan } from '@/api/center'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'
import ProviderOrganDialog from '@/components/ProviderOrganDialog'
import ResourceTable from '@/components/ResourceTable'
import ResourcePreviewTable from '@/components/ResourcePreviewTable'
import Model from '@/components/Model'

export default {
  components: {
    ProjectResourceDialog,
    ProviderOrganDialog,
    ResourceTable,
    ResourcePreviewTable,
    Model
  },
  data() {
    return {
      serverAddress: '',
      auditForm: {
        auditOpinion: ''
      },
      isShowAuditForm: false,
      projectName: '',
      projectId: '',
      projectAuditStatus: '',
      projectDesc: '',
      userName: '',
      createDate: '',
      activeName: '',
      participationIdentity: '',
      selectedData: [],
      resourceList: [],
      providerOrganDialogVisible: false,
      previewDialogVisible: false,
      dialogVisible: false,
      previewList: [],
      selectedOrganId: null,
      organList: [],
      tableButtons: ['preview'],
      userInfo: [],
      organs: [],
      currentOrgan: [],
      differents: [],
      saveParams: {
        id: '',
        projectOrgans: [] // save params
      },
      creator: false,
      tabPosition: 'left'
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
    this.fetchData()
  },
  methods: {
    statusStyle(status) {
      return status === 0 ? 'status-0 el-icon-refresh' : status === 1 ? 'status-1 el-icon-circle-check' : status === 2 ? 'status-2 el-icon-circle-close' : ''
    },
    async openProviderOrganDialog() {
      await this.findMyGroupOrgan()
      this.providerOrganDialogVisible = true
    },
    async findMyGroupOrgan() {
      const { result } = await findMyGroupOrgan({ serverAddress: this.serverAddress })
      this.organList = result.dataList.organList
    },
    showAuditForm() {
      return this.organs.filter(item => item.organId === this.selectedOrganId)[0].auditStatus === 0
    },
    getTableButtons(item) {
      if (item.participationIdentity === 1) { // The initiator
        return ['preview', 'remove']
      } else if (item.auditStatus === 1) {
        return ['preview', 'remove']
      } else {
        return ['remove']
      }
    },
    handleSubmit() {
      this.$confirm('同意加入发起方的此次项目合作', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.isShowAuditForm = false
        this.approval({
          type: 1,
          id: this.currentOrgan.id,
          auditOpinion: this.auditForm.auditOpinion,
          auditStatus: 1
        })
        this.$message({
          type: 'success',
          message: '加入成功'
        })
        location.reload()
      }).catch(() => {})
    },
    handleRefused() {
      if (this.auditForm.auditOpinion === '') {
        this.$message({
          type: 'warning',
          message: '请填写审核意见'
        })
        this.$refs.auditInput.focus()
      } else {
        this.$confirm('拒绝与发起方的此次项目合作?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.approval({
            type: 1,
            id: this.currentOrgan.id,
            auditOpinion: this.auditForm.auditOpinion,
            auditStatus: 2
          })
          this.isShowAuditForm = false
          this.$message({
            type: 'success',
            message: '拒绝与发起方的此次项目合作'
          })
          location.reload()
        }).catch(() => {})
      }
    },
    handleAgree(row) {
      this.approval({
        type: 2,
        id: row.id,
        auditOpinion: '',
        auditStatus: 1
      })
      this.$message({
        type: 'success',
        message: '授权成功'
      })
      this.isShowAuditForm = false
      location.reload()
    },
    handleResourceRefused(row) {
      this.$prompt('请输入拒绝原因', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => {
        this.approval({
          type: 2,
          id: row.id,
          auditOpinion: value,
          auditStatus: 2
        })
        this.$message({
          type: 'success',
          message: '已拒绝'
        })
        this.isShowAuditForm = false
        location.reload()
      }).catch(() => {})
    },
    handleClick({ label = '' }) {
      this.differents = []
      this.saveParams.projectOrgans = []
      this.selectedOrganId = label
      console.log('handleClick', this.selectedOrganId)
      this.currentOrgan = this.organs.filter(item => item.organId === this.selectedOrganId)[0]
      this.resourceList[this.selectedOrganId] = this.currentOrgan.resources
      this.isShowAuditForm = this.thisInstitution && this.currentOrgan.auditStatus === 0
      this.projectAuditStatus = this.currentOrgan.auditStatus === 1
      this.selectedData = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
    },
    handleModelClick(id) {
      this.$router.push({
        name: 'ModelDetail',
        params: { id }
      })
    },
    handlePreview(row) {
      this.resourceId = row.resourceId
      this.resourceFilePreview()
      this.previewDialogVisible = true
    },
    handleRemove({ organId, id }) {
      this.removeResource(id)
    },
    removeResource(id) {
      removeResource({ id }).then(({ code = -1 }) => {
        if (code === 0) {
          const index = this.resourceList[this.selectedOrganId].findIndex(item => item.id === id)
          console.log(index, id)
          this.resourceList[this.selectedOrganId].splice(index, 1)
          console.log('remove', this.resourceList[this.selectedOrganId])
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
    resourceFilePreview() {
      this.fieldListLoading = true
      resourceFilePreview({ resourceId: this.resourceId }).then(res => {
        this.previewList = res?.result.dataList
        this.fieldListLoading = false
      })
    },
    openDialog(id) {
      this.selectedOrganId = id
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      this.differents = this.getArrDifSameValue(this.selectedData, data)
      if (this.differents.length > 0) {
        this.saveParams.projectOrgans.push({
          organId: this.selectedOrganId,
          participationIdentity: this.selectedOrganId === this.userOrganId ? 1 : 2,
          resourceIds: this.differents.map(r => r.resourceId)
        })
        this.saveProject()
      }
      // this.selectedData = data
      // this.resourceList[this.selectedOrganId] = data.filter(item => item.organId === this.selectedOrganId)
      // console.log('handleDialogSubmit', this.resourceList[this.selectedOrganId])
      this.dialogVisible = false
    },
    closeDialog() {
      this.previewDialogVisible = false
    },
    closeProviderOrganDialog() {
      this.providerOrganDialogVisible = false
    },
    handleProviderOrganSubmit(data) {
      data.map(item => {
        this.saveParams.projectOrgans.push({
          organId: item.globalId,
          participationIdentity: 2
        })
      })

      this.projectOrgans = data.map(item => {
        return {
          organId: item.globalId,
          participationIdentity: 2
        }
      })
      const success = this.saveProject()
      if (success) {
        data.map(item => {
          this.organs.push({
            organId: item.globalId,
            organName: item.globalName,
            participationIdentity: 2
          })
        })

        this.providerOrganIds = data
        this.providerOrganDialogVisible = false
      }
    },
    saveProject() {
      // const { projectName, projectDesc, projectOrgans } = this
      this.saveParams.id = this.list.id
      const params = JSON.stringify(this.saveParams)
      saveProject(params).then(res => {
        console.log(res)
        if (res.code === 0) {
          this.$message({
            message: '添加成功',
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
      approval(params).then(({ result }) => {

      })
    },
    fetchData() {
      this.listLoading = true
      this.projectId = this.$route.params.id || this.list.id
      getProjectDetail({ id: this.projectId }).then(res => {
        if (res.code === 0) {
          this.listLoading = false
          this.list = res.result
          const { projectName, projectId, projectDesc, userName, createDate, organs, serverAddress, creator } = this.list
          this.serverAddress = serverAddress
          this.creator = creator
          this.projectName = projectName
          this.projectId = projectId
          this.projectDesc = projectDesc
          this.userName = userName
          this.createDate = createDate
          this.organs = organs
          this.selectedOrganId = this.selectedOrganId || this.userOrganId
          this.activeName = this.selectedOrganId
          this.resourceList[this.selectedOrganId] = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
          this.selectedData = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
          this.currentOrgan = this.organs.filter(item => item.organId === this.selectedOrganId)[0]
          this.projectAuditStatus = this.currentOrgan.auditStatus === 1
          this.isShowAuditForm = !this.projectAuditStatus
        }
      })
    },
    // compare array diffrent
    getArrDifSameValue(arr1, arr2) {
      const result = []
      for (let i = 0; i < arr2.length; i++) {
        const obj = arr2[i]
        const id = obj.resourceId
        let isExist = false
        for (let j = 0; j < arr1.length; j++) {
          const aj = arr1[j]
          const n = aj.resourceId
          if (n === id) {
            isExist = true
            break
          }
        }
        if (!isExist) {
          result.push(obj)
        }
      }
      console.log('result', result)
      return result
    },
    toModelCreate() {
      this.$router.push({
        path: '/model/create',
        query: { projectId: this.list.id }
      })
    },
    ...mapActions('user', ['getInfo'])
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
  border-radius: $sectionBorderRadius;
  background-color: #ffffff;
  padding: 30px;
  margin-bottom: 30px;
}
.infos{
  height: 100vh;
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
.auditOpinion{
  font-size: 14px;
  margin: 10px 0;
}

.tabs{
  background-color: #F5F7FA;
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
