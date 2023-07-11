<template>
  <div class="container">
    <el-form ref="form" :model="form" :rules="rules" class="form-container">
      <el-form-item label="推理使用模型" prop="modelName">
        <el-input v-model="form.modelName" class="model-name" placeholder="请选择模型" clearable @focus="openModelDialog" />
      </el-form-item>
      <el-form-item>
        <p><strong class="required">*</strong>发起方：<span>{{ createdOrgan }}</span></p>
        <el-button class="select-button" :disabled="form.modelName === ''" size="small" type="primary" @click="handleResourceSelect(createdOrganId,1)">选择资源</el-button>
        <ResourceTable v-if="selectedResource[createdOrganId]" :show-preview-button="false" :this-institution="false" :creator="true" :show-status="false" row-key="resourceId" :data="selectedResource[createdOrganId]" @remove="handleRemove(createdOrganId,1)" />
      </el-form-item>
      <el-form-item v-for="(item,index) in providerOrgans" :key="index">
        <p><strong class="required">*</strong>协作方：<span>{{ item.organName }}</span></p>
        <el-button class="select-button" :disabled="form.modelName === ''" size="small" type="primary" @click="handleResourceSelect(item.organId, 2)">选择资源</el-button>
        <ResourceTable v-if="selectedResource[item.organId]" :show-preview-button="false" :this-institution="false" :creator="true" :show-status="false" row-key="resourceId" :data="selectedResource[item.organId]" @remove="handleRemove(item.organId,2)" />
      </el-form-item>
      <el-form-item label="推理服务名称" prop="reasoningName">
        <el-input
          v-model="form.reasoningName"
          placeholder="请输入推理服务名称"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="推理服务描述" prop="reasoningDesc">
        <el-input
          v-model="form.reasoningDesc"
          type="textarea"
          placeholder="请输入推理服务描述，限100字"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item class="form-footer">
        <el-button type="primary" @click="onSubmit">导入</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- model select dialog -->
    <ModelSelectDialog :visible="dialogVisible" @submit="handleModelSubmit" @close="handleClose" />
    <!-- add resource dialog -->
    <ResourceDialog
      ref="dialogRef"
      top="10px"
      width="800px"
      title="选择资源"
      :show-status="false"
      :selected-data="selectedResourceId"
      :table-data="resourceList"
      :visible="resourceDialogVisible"
      :pagination-options="paginationOptions"
      @close="handleResourceDialogCancel"
      @submit="handleResourceDialogSubmit"
      @pagination="handlePagination"
    />
  </div>
</template>

<script>
import { getResourceList } from '@/api/project'
import { saveReasoning } from '@/api/reasoning'
import ModelSelectDialog from '@/components/ModelSelectDialog'
import ResourceTable from '@/components/ResourceTable'
import ResourceDialog from '@/components/ResourceDialog'

export default {
  name: 'ModelInferenceTask',
  components: {
    ResourceTable,
    ResourceDialog,
    ModelSelectDialog
  },
  data() {
    return {
      trainType: 0,
      dialogVisible: false,
      resourceDialogVisible: false,
      selectedResourceId: '',
      resourceList: [],
      taskId: 0,
      selectedOrganId: '',
      createdOrgan: '',
      createdOrganId: '',
      providerOrgans: [{
        organId: '',
        userOrganName: ''
      }],
      form: {
        modelName: '',
        taskId: '',
        resourceId: '',
        resourceList: [],
        reasoningName: '',
        reasoningDesc: '',
        createdResourceId: '',
        providerResourceId: ''
      },
      participationIdentity: 1,
      selectedResource: [], // 选中
      rules: {
        modelName: [
          { required: true, message: '请选择模型', trigger: 'change' }
        ],
        reasoningName: [
          { required: true, message: '请输入推理服务名称' }
        ],
        reasoningDesc: [
          { required: true, message: '请输入推理服务描述，限100字' }
        ],
        resourceId: [
          { required: true }
        ]
      },
      pageSize: 100,
      pageNo: 1,
      paginationOptions: {
        pageSize: 5,
        pageNo: 1,
        total: 0,
        pageCount: 0
      }
    }
  },
  methods: {
    reset() {
      this.form.modelName = ''
      this.form.taskId = ''
      this.form.createdResourceId = ''
      this.form.providerResourceId = ''
      this.form.reasoningName = ''
      this.form.reasoningDesc = ''
      this.resourceList = []
      this.selectedResource = []
      this.$refs.form.resetFields()
    },
    handleRemove(organId, participationIdentity) {
      this.paginationOptions.pageNo = 1
      this.selectedOrganId = organId
      this.selectedResource[organId].splice(0)

      if (participationIdentity === 1) {
        this.form.createdResourceId = ''
      } else if (participationIdentity === 2) {
        this.form.providerResourceId = ''
      }
    },
    async onSubmit() {
      const { taskId, reasoningName, reasoningDesc, createdResourceId, providerResourceId } = this.form
      if (createdResourceId === '') {
        this.$message({
          message: '请输入发起方方资源',
          type: 'warning'
        })
        return
      } else if (providerResourceId === '' && this.trainType === 0) {
        this.$message({
          message: '请输入协作方资源',
          type: 'warning'
        })
        return
      }
      this.$refs['form'].validate(async valid => {
        if (valid) {
          this.form.resourceList.push({
            participationIdentity: 1,
            resourceId: createdResourceId
          })
          if (providerResourceId !== '') {
            this.form.resourceList.push({
              participationIdentity: 2,
              resourceId: providerResourceId
            })
          }
          const { code, result } = await saveReasoning({
            taskId,
            resourceList: this.form.resourceList,
            reasoningName,
            reasoningDesc
          })
          if (code === 0) {
            this.$message({
              message: '导入成功',
              type: 'success'
            })
            this.$router.push({
              name: 'ModelReasoningDetail',
              params: { id: result.id }
            })
          }
        }
      })
    },
    async handleResourceSelect(organId, participationIdentity) {
      if (organId !== this.selectedOrganId) {
        this.paginationOptions.pageNo = 1
      }
      this.selectedOrganId = organId
      this.participationIdentity = participationIdentity
      // set select resource value
      if (this.selectedResource[this.selectedOrganId] && this.selectedResource[this.selectedOrganId][0]) {
        this.selectedResourceId = this.selectedResource[this.selectedOrganId][0].resourceId
      } else {
        this.selectedResourceId = ''
      }
      await this.getResourceList()

      this.resourceDialogVisible = true
    },
    handleClose() {
      this.dialogVisible = false
    },
    handleModelNameChange(val) {
      console.log(this.form.modelName)
      console.log(val)
    },
    openModelDialog() {
      this.dialogVisible = true
    },
    async handleModelSubmit(data) {
      if (data) {
        this.trainType = data.trainType
        this.form.taskId = data.taskId
        this.providerOrgans = this.trainType === 0 ? data.providerOrgans : []
        this.createdOrgan = data.createdOrgan
        this.createdOrganId = data.createdOrganId
        this.projectId = data.projectId
        this.form.modelName = data.modelName
        this.modelId = data.modelId
      }
      this.dialogVisible = false
    },
    handleResourceDialogCancel() {
      this.resourceDialogVisible = false
    },
    handleResourceDialogSubmit(data) {
      if (data.resourceId) {
        this.selectedResource[this.selectedOrganId] = [data]
        this.selectedResourceId = data.resourceId
        if (this.participationIdentity === 1) {
          this.form.createdResourceId = data.resourceId
        } else {
          this.form.providerResourceId = data.resourceId
        }
      }

      this.resourceDialogVisible = false
    },
    async handlePagination(pageNo) {
      this.paginationOptions.pageNo = pageNo
      await this.getResourceList()
    },
    async getResourceList() {
      // this.resourceList = []
      const res = await getResourceList({
        modelId: this.modelId,
        organId: this.selectedOrganId,
        pageSize: this.paginationOptions.pageSize,
        pageNo: this.paginationOptions.pageNo })
      if (res.code === 0) {
        this.resourceList = res.result.data
        this.paginationOptions.pageCount = res.result.totalPage
        this.paginationOptions.total = res.result.total
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container{
  background: #fff;
  padding: 30px 0;
  height: 100%;
  .form-container{
    width: 700px;
    margin: 0 auto;
  }
}
.select-button{
  width: 100%;
}
.model-name{
  cursor:default
}
.form-footer{
  text-align: center;
}
.required{
  color: #F56C6C;
  margin-right: 5px;
}
</style>
