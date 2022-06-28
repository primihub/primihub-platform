<template>
  <div class="container">
    <section>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="infos">
            <div class="infos-title">
              <h2>{{ projectName }}</h2>
              <p class="infos-des">{{ projectId }}</p>
            </div>
            <el-descriptions :column="1" label-class-name="detail-title">
              <el-descriptions-item label="创建人">{{ userName }}</el-descriptions-item>
              <el-descriptions-item label="项目描述">{{ projectDesc }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ createDate }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-col>
        <el-col :span="8">
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
        </el-col>
      </el-row>
    </section>
    <section class="organs">
      <h3>参与机构</h3>
      <el-button type="primary" class="add-provider-button" @click="openProviderOrganDialog">添加更多协作者</el-button>
      <el-tabs v-model="activeName" type="border-card" class="tabs" @tab-click="handleClick">
        <el-tab-pane v-for="item in organs" :key="item.organId" :label="item.organId">
          <p slot="label">{{ item.participationIdentity === 1 ? '发起方：':'协作方：' }}{{ item.organName }}
            <span class="identity">{{ item.creator ? '&lt;创建者&gt;':'' }}</span>
            <span class="identity">{{ item.auditStatus === 0? '&lt;审核中&gt;':item.auditStatus === 2?'&lt;已拒绝&gt;':'' }}</span>
          </p>
          <el-button type="primary" plain @click="openDialog(item.organId)">添加资源到此项目</el-button>
          <p>{{ item.auditOpinion }}</p>
          <ResourceTable
            v-if="item.resources && item.resources.length >0"
            :project-audit-status="projectAuditStatus"
            :this-institution="thisInstitution"
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

export default {
  components: {
    ProjectResourceDialog,
    ProviderOrganDialog,
    ResourceTable,
    ResourcePreviewTable
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
      selectedOrganId: '',
      organList: [],
      tableButtons: ['preview'],
      userInfo: [],
      organs: [],
      currentOrgan: [],
      differents: [],
      saveParams: {
        id: '',
        projectOrgans: [] // save params
      }
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
    async openProviderOrganDialog() {
      await this.findMyGroupOrgan()
      this.providerOrganDialogVisible = true
    },
    async findMyGroupOrgan() {
      const { result } = await findMyGroupOrgan({ serverAddress: this.serverAddress })
      this.organList = result.dataList.organList
    },
    showAuditForm() {
      console.log(this.organs.filter(item => item.organId === this.selectedOrganId)[0].auditStatus)
      return this.organs.filter(item => item.organId === this.selectedOrganId)[0].auditStatus === 0
    },
    getTableButtons(item) {
      console.log(item.organId === this.userInfo.organIdList)
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
      this.currentOrgan = this.organs.filter(item => item.organId === this.selectedOrganId)[0]
      this.resourceList[this.selectedOrganId] = this.currentOrgan.resources
      this.isShowAuditForm = this.thisInstitution && this.currentOrgan.auditStatus === 0
      console.log(this.currentOrgan)
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
      const index = this.selectedData.findIndex(item => item.resourceId === id)
      this.removeResource(id)
      this.selectedData.splice(index, 1)
    },
    removeResource(id) {
      removeResource({ id }).then(({ code = -1 }) => {
        if (code === 0) {
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
        this.dataList = res.result.dataList
        this.previewList = res.result.fieldList
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
      console.log('333', this.differents)
      if (this.differents.length > 0) {
        this.saveParams.projectOrgans.push({
          organId: this.selectedOrganId,
          participationIdentity: this.selectedOrganId === this.userOrganId ? 1 : 2,
          resourceIds: this.differents.map(r => r.resourceId)
        })
        this.saveProject()
      }
      this.selectedData = data
      console.log('handleDialogSubmit', this.saveParams.projectOrgans)
      this.resourceList[this.selectedOrganId] = data.filter(item => item.organId === this.selectedOrganId)
      this.dialogVisible = false
    },
    closeDialog() {
      this.previewDialogVisible = false
    },
    closeProviderOrganDialog() {
      this.providerOrganDialogVisible = false
    },
    handleProviderOrganSubmit(data) {
      console.log('handleProviderOrganSubmit', data)
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
            participationIdentity: 2,
            creator: false
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
          const { projectName, projectId, projectDesc, userName, createDate, organs, serverAddress } = this.list
          this.serverAddress = serverAddress
          this.projectName = projectName
          this.projectId = projectId
          this.projectDesc = projectDesc
          this.userName = userName
          this.createDate = createDate
          this.organs = organs
          this.selectedOrganId = this.organs[0].organId
          this.resourceList[this.selectedOrganId] = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
          this.selectedData = this.organs.filter(item => item.organId === this.selectedOrganId)[0].resources
          this.currentOrgan = this.organs.filter(item => item.organId === this.selectedOrganId)[0]
          this.projectAuditStatus = this.currentOrgan.auditStatus === 0
          this.isShowAuditForm = this.projectAuditStatus === 0
        }
      })
    },
    // compare array diffrent
    getArrDifSameValue(arr1, arr2) {
      console.log(arr1, arr2)
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
.detail-panel {
  padding: 20px 0 20px 20px;
  border-top: 1px solid #f0f0f0;
  .card-box {
    display: flex;
    flex-wrap: wrap;
    .item {
      margin-right: 20px;
      margin-bottom: 20px;
    }
  }
}
::v-deep .el-descriptions-item__container{
  flex-wrap: wrap;

}
</style>
