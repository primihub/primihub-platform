<template>
  <div v-loading="loading" class="container">
    <el-form ref="form" :model="form" :rules="rules" class="form-container">
      <el-form-item label="推理使用模型" prop="modelName">
        <el-input v-model="form.modelName" disabled class="model-name" placeholder="请选择模型" clearable @focus="openModelDialog" />
      </el-form-item>
      <el-form-item>
        <p><strong class="required">*</strong>发起方：<span>{{ createdOrgan }}</span></p>
        <ResourceTable v-if="selectedResource[createdOrganId]" :show-buttons="false" :this-institution="false" :creator="true" :show-status="false" row-key="resourceId" :data="selectedResource[createdOrganId]" />
      </el-form-item>
      <el-form-item>
        <p><strong class="required">*</strong>参与方：<span>{{ providerOrganName }}</span></p>
        <ResourceTable v-if="selectedResource" :show-buttons="false" :this-institution="false" :creator="true" :show-status="false" row-key="resourceId" :data="selectedResource[providerOrganId]" />
      </el-form-item>
      <el-form-item label="推理服务名称" prop="reasoningName">
        <el-input
          v-model="form.reasoningName"
          placeholder="请输入推理服务名称"
          maxlength="20"
          show-word-limit
          disabled
        />
      </el-form-item>
      <el-form-item label="推理服务描述" prop="reasoningDesc">
        <el-input
          v-model="form.reasoningDesc"
          disabled
          type="textarea"
          placeholder="请输入推理服务描述，限100字"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item class="form-footer">
        <el-button type="primary" @click="onSubmit">开始运算</el-button>
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
    <el-dialog
      title="运算结果"
      :visible="visible"
      :before-close="closeDialog"
    >
      <div class="desc-container">
        <el-descriptions :column="2">
          <el-descriptions-item label="推理服务名称">{{ dataList.reasoningName }}</el-descriptions-item>
          <el-descriptions-item label="推理服务描述">{{ dataList.reasoningDesc }}</el-descriptions-item>
          <el-descriptions-item label="推理服务ID">{{ dataList.reasoningId }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ dataList.reasoningType }}方推理</el-descriptions-item>
          <el-descriptions-item label="上线时间">{{ dataList.releaseDate }}</el-descriptions-item>
          <el-descriptions-item label="状态"><StatusIcon :status="dataList.reasoningState" /> {{ dataList.reasoningState | reasoningStateFilter }}</el-descriptions-item>
        </el-descriptions>
        <div class="buttons">
          <el-button v-if="dataList.reasoningState === 1" type="primary" icon="el-icon-download" @click="download">下载结果</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getResourceList } from '@/api/project'
import { saveReasoning, getReasoning } from '@/api/reasoning'
import { getModelTaskSuccessList } from '@/api/model'
import { getTaskData } from '@/api/task'
import ModelSelectDialog from '@/components/ModelSelectDialog'
import ResourceTable from '@/components/ResourceTable'
import ResourceDialog from '@/components/ResourceDialog'
import { getToken } from '@/utils/auth'
import StatusIcon from '@/components/StatusIcon'

export default {
  name: 'ModelInferenceTask',
  components: {
    ResourceTable,
    ResourceDialog,
    ModelSelectDialog,
    StatusIcon
  },
  filters: {
    reasoningStateFilter(status) {
      status = status || 0
      const statusMap = {
        0: '未开始',
        1: '完成',
        2: '运行中',
        3: '运行失败',
        4: '取消',
        5: '已删除'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      taskState: '',
      taskTimer: null,
      reasoningId: '',
      dataList: {},
      visible: false,
      loading: false,
      dialogVisible: false,
      resourceDialogVisible: false,
      selectedResourceId: '',
      resourceList: [],
      taskId: 0,
      selectedOrganId: '',
      createdOrgan: '',
      createdOrganId: '',
      providerOrganId: '',
      providerOrganName: '',
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
      serverAddress: '',
      participationIdentity: 1,
      selectedResource: [], // 选中
      rules: {
        modelName: [
          { required: true, message: '请选择模型' }
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
  async created() {
    await this.setDefault()
  },
  methods: {
    closeDialog() {
      this.visible = false
    },
    async download() {
      const taskId = this.dataList.runTaskId || ''
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    async getReasoning() {
      const res = await getReasoning({ id: this.reasoningId })
      if (res.code === 0) {
        this.dataList = res.result
        this.reasoningState = this.dataList.reasoningState
        if (this.reasoningState === 2) {
          this.taskTimer = window.setInterval(() => {
            setTimeout(this.getTaskData(), 0)
          }, 1000)
        }
      }
    },
    getTaskData() {
      getTaskData({ taskId: this.reasoningId }).then(res => {
        if (res.code === 0) {
          this.taskState = res.result.taskState
          if (this.taskState === 3) {
            clearInterval(this.taskTimer)
            this.fail = true
            this.$message.error(res.result.taskErrorMsg)
          } else if (this.taskState !== 2) {
            this.taskData.taskState = this.taskState
            clearInterval(this.taskTimer)
          }
          this.dataList.reasoningState = this.taskState
        }
        this.loading = false
      }).catch(err => {
        console.log(err)
        this.loading = false
      })
    },
    async setDefault() {
      await this.getModelTaskSuccessList()
      this.loading = true
      const createdOrganList = await this.getResourceList(this.createdOrganId)
      this.selectedResource[this.createdOrganId] = [this.resourceList[0]]
      this.form.createdResourceId = this.resourceList[0].resourceId
      this.providerOrganId = this.providerOrgans[0].organId
      this.providerOrganName = this.providerOrgans[0].organName
      const providerOrganList = await this.getResourceList(this.providerOrganId)
      this.selectedResource[this.providerOrganId] = [this.resourceList[0]]
      this.form.providerResourceId = this.resourceList[0].resourceId
      Promise.all([createdOrganList, providerOrganList]).then(() => {
        this.loading = false
      })
      this.form.reasoningName = '隐私计算反欺诈应用'
      this.form.reasoningDesc = '隐私计算反欺诈应用'
    },
    async getModelTaskSuccessList() {
      this.listLoading = true
      const res = await getModelTaskSuccessList()
      if (res.code === 0) {
        this.handleModelSubmit(res.result.data[0])
      }
    },
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
      this.selectedResource[organId].splice(0)
      if (participationIdentity === 1) {
        this.form.createdResourceId = ''
      } else if (participationIdentity === 2) {
        this.form.providerResourceId = ''
      }
    },
    async onSubmit() {
      const { taskId, reasoningName, reasoningDesc, createdResourceId, providerResourceId } = this.form
      console.log(createdResourceId)
      if (createdResourceId === '' || providerResourceId === '') {
        this.$message({
          message: createdResourceId === '' ? '请输入发起方方资源' : '请输入参与方资源',
          type: 'warning'
        })
        return
      }
      this.form.resourceList = [
        {
          participationIdentity: 1,
          resourceId: createdResourceId
        }, {
          participationIdentity: 2,
          resourceId: providerResourceId
        }
      ]

      this.$refs['form'].validate(async valid => {
        if (valid) {
          const { code, result } = await saveReasoning({
            taskId,
            resourceList: this.form.resourceList,
            reasoningName,
            reasoningDesc
          })
          if (code === 0) {
            this.reasoningId = result.id
            this.visible = true
            this.getReasoning()
          }
        }
      })
    },
    async handleResourceSelect(organId, participationIdentity) {
      this.selectedOrganId = organId
      this.participationIdentity = participationIdentity
      await this.getResourceList()
    },
    handleClose() {
      this.dialogVisible = false
    },
    openModelDialog() {
      this.dialogVisible = true
    },
    async handleModelSubmit(data) {
      console.log(data)
      if (data) {
        this.form.taskId = data.taskId
        this.providerOrgans = data.providerOrgans
        this.createdOrgan = data.createdOrgan
        this.createdOrganId = data.createdOrganId
        this.projectId = data.projectId
        this.form.modelName = data.modelName
        this.modelId = data.modelId
        this.serverAddress = data.serverAddress
      }
      console.log()
      this.dialogVisible = false
    },
    handleResourceDialogCancel() {
      this.selectedResourceId = ''
      this.resourceDialogVisible = false
    },
    handleResourceDialogSubmit(data) {
      console.log('handleResourceDialogSubmit', data)
      if (data.resourceId) {
        this.selectedResource[this.selectedOrganId] = [data]
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
    async getResourceList(organId) {
      console.log(organId)
      this.resourceList = []

      const res = await getResourceList({
        modelId: this.modelId,
        serverAddress: this.serverAddress,
        organId,
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
.buttons{
  display: flex;
  justify-content: flex-end;
}
</style>
