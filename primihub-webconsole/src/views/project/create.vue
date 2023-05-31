<template>
  <div class="app-container">
    <el-form
      ref="dataForm"
      :model="dataForm"
      :rules="dataRules"
      label-width="100px"
      class="demo-dataForm"
    >
      <el-form-item label="项目名称" prop="projectName">
        <div class="item-wrap-normal">
          <el-input
            v-model="dataForm.projectName"
            maxlength="20"
            minlength="3"
            show-word-limit
          />
        </div>
      </el-form-item>
      <el-form-item label="项目描述" prop="projectDesc">
        <div class="item-wrap-normal">
          <el-input
            v-model="dataForm.projectDesc"
            type="textarea"
            maxlength="200"
            minlength="3"
            show-word-limit
          />
        </div>
      </el-form-item>
      <el-form-item label="发起方" prop="initiateOrganId">
        <p class="organ"><i class="el-icon-office-building" />  {{ dataForm.initiateOrganName }}</p>
        <el-button type="primary" plain @click="openDialog(dataForm.initiateOrganId)">添加资源到此项目</el-button>
        <ResourceTable v-if="filterData(dataForm.initiateOrganId).length>0" :creator="true" :show-status="false" row-key="resourceId" :data="filterData(dataForm.initiateOrganId)" @remove="handleRemove" @preview="handlePreview" />
      </el-form-item>
      <el-form-item label="协作方" prop="providerOrganIds">
        <el-button plain @click="openProviderOrganDialog">添加协作者</el-button>
        <div v-for="(organ,index) in dataForm.providerOrganIds" :key="organ.globalId" class="organ-item">
          <p>
            <span class="organ"><i class="el-icon-office-building" />  {{ organ.globalName }} </span>
            <i class="el-icon-delete remove-provider" @click="handleProviderRemove(index)" />
          </p>
          <el-button v-if="selectedData" type="primary" plain @click="openDialog(organ.globalId)">添加资源到此项目</el-button>
          <ResourceTable v-if="filterData(organ.globalId).length>0" :show-status="false" :creator="true" :this-institution="thisInstitution" row-key="resourceId" :data="filterData(organ.globalId)" @remove="handleRemove" @preview="handlePreview" />
        </div>

      </el-form-item>
      <el-form-item>
        <el-button
          :loading="loading"
          type="primary"
          :disabled="dataForm.providerOrganIds.length === 0"
          @click="submitForm"
        >
          立即创建
        </el-button>
        <el-button @click="goBack()">返回</el-button>
      </el-form-item>
    </el-form>
    <!-- add resource dialog -->
    <ProjectResourceDialog
      ref="dialogRef"
      top="10px"
      width="800px"
      :selected-data="resourceList[selectedOrganId]"
      title="添加资源"
      :show-preview-button="thisInstitution"
      :organ-id="selectedOrganId"
      :visible="dialogVisible"
      @close="handleDialogCancel"
      @submit="handleDialogSubmit"
      @preview="handlePreview"
    />
    <!-- add provider organ dialog -->
    <ProviderOrganDialog :selected-data="dataForm.providerOrganIds" :visible.sync="providerOrganDialogVisible" title="添加协作方" @submit="handleProviderOrganSubmit" @close="closeProviderOrganDialog" />
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
import { saveProject } from '@/api/project'
import { resourceFilePreview } from '@/api/resource'
import { getLocalOrganInfo } from '@/api/center'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'
import ResourceTable from '@/components/ResourceTable'
import ProviderOrganDialog from '@/components/ProviderOrganDialog'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'

export default {
  components: { ProjectResourceDialog, ResourceTable, ProviderOrganDialog, ResourcePreviewDialog },
  data() {
    return {
      loading: false,
      dataForm: {
        projectName: '',
        projectDesc: '',
        initiateOrganId: '',
        providerOrganIds: []
      },
      dialogVisible: false,
      previewDialogVisible: false,
      providerOrganDialogVisible: false,
      resourceList: [],
      selectedOrganId: '',
      initiateOrganId: '',
      providerOrganId: '',
      initiateOrganName: '',
      providerOrganName: '',
      organList: [],
      selectedData: [],
      previewList: [],
      fileId: '',
      dataRules: {
        projectName: [
          { required: true, message: '请输入项目名称', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        projectDesc: [
          { required: true, message: '请输入项目描述', trigger: 'blur' },
          { min: 0, max: 200, message: '长度200字符以内', trigger: 'blur' }
        ],
        initiateOrganId: [
          { required: true, message: '请选择发起方', trigger: 'blur' }
        ],
        providerOrganIds: [
          { required: true, message: '请选择协作方', trigger: 'blur' }
        ]
      },
      saveParams: {
        id: '',
        projectOrgans: [] // save params
      }
    }
  },
  computed: {
    thisInstitution() {
      return this.$store.getters.userOrganId === this.selectedOrganId
    }
  },
  async created() {
    await this.getLocalOrganInfo()
  },
  methods: {
    async openProviderOrganDialog() {
      this.providerOrganDialogVisible = true
    },
    openDialog(id) {
      this.selectedOrganId = id
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      console.log('handleDialogSubmit', data)
      this.resourceList[this.selectedOrganId] = data.filter(item => item.organId === this.selectedOrganId)
      this.dialogVisible = false
    },
    filterData(organId) {
      return this.resourceList[organId] || []
    },
    submitForm() {
      const { projectName, projectDesc } = this.dataForm
      this.getProjectOrgans()
      const params = {
        projectName,
        projectDesc,
        projectOrgans: this.saveParams.projectOrgans
      }
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          this.loading = true
          saveProject(params).then(res => {
            if (res.code === 0) {
              this.loading = false
              const id = res.result.id
              this.$router.push({
                name: 'ProjectDetail',
                params: { id }
              })
            }
          }).catch(() => {
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    getProjectOrgans() {
      this.saveParams.projectOrgans = this.dataForm.providerOrganIds.map(item => {
        return {
          organId: item.globalId,
          participationIdentity: 2
        }
      })
      this.saveParams.projectOrgans.push({
        organId: this.dataForm.initiateOrganId,
        participationIdentity: 1
      })
      for (const key in this.resourceList) {
        const item = this.resourceList[key]
        if (this.resourceList[key].length > 0) {
          const current = this.saveParams.projectOrgans.filter(item => item.organId === key)
          current[0].resourceIds = item.map(r => r.resourceId)
        }
      }
    },
    handleRemove({ organId, resourceId }) {
      const index = this.resourceList[organId].findIndex(item => item.resourceId === resourceId)
      this.resourceList[organId].splice(index, 1)
    },
    goBack() {
      this.$router.replace({
        name: 'ProjectList'
      })
    },
    async getLocalOrganInfo() {
      const { result = {}} = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      this.dataForm.initiateOrganId = this.sysLocalOrganInfo?.organId
      this.dataForm.initiateOrganName = this.sysLocalOrganInfo?.organName
    },
    closeProviderOrganDialog(data) {
      this.dataForm.providerOrganIds = data
      this.providerOrganDialogVisible = false
    },
    handleProviderOrganSubmit(data) {
      this.dataForm.providerOrganIds = data
      this.providerOrganDialogVisible = false
    },
    handleProviderRemove(index) {
      this.dataForm.providerOrganIds.splice(index, 1)
    },
    async handlePreview(row) {
      this.resourceId = row.resourceId
      await this.resourceFilePreview()
      this.previewDialogVisible = true
    },
    async resourceFilePreview() {
      const res = await resourceFilePreview({ resourceId: this.resourceId })
      this.previewList = res?.result.dataList
    },
    closeDialog() {
      this.previewDialogVisible = false
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
  p{
    margin-block-start: 0;
      margin-block-end: 0;
  }
  .item-wrap-normal {
    width: 400px;
  }
  .organ-item{
    .el-button{
      margin-bottom: 10px;
    }
  }
  .organ{
    font-size: 16px;
    color: #666;
    font-weight: bold;
  }
  .remove-provider{
    color: $dangerColor;
  }
  ::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px!important;
  }
  ::v-deep .el-table th.el-table__cell{
    font-size: 14px;
  }
  ::v-deep .el-table th.el-table__cell{
    padding: 5px 0!important;
  }
  ::v-deep .el-table .cell{
    font-size: 12px;
  }
</style>
