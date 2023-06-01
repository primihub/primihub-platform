<template>
  <div v-loading="loading" element-loading-text="开始运算" class="form-container">
    <el-steps :active="active" finish-status="success" align-center>
      <el-step title="查询条件" />
      <el-step title="查询结果" />
    </el-steps>
    <div class="task-container">
      <el-descriptions :column="1" :colon="false">
        <el-descriptions-item><i class="icon el-icon-success" />发起方： {{ createdOrgan }}</el-descriptions-item>
        <el-descriptions-item><i class="icon el-icon-success" /> 协作方： {{ providerOrganName }}</el-descriptions-item>
        <el-descriptions-item><i class="icon el-icon-success" /> 推理使用模型： <el-tag type="primary">{{ modelName }}</el-tag></el-descriptions-item>
        <el-descriptions-item><i class="icon el-icon-success" /> 推理服务名称：{{ reasoningName }}</el-descriptions-item>
      </el-descriptions>
      <div class="buttons">
        <el-button class="submit-button" type="primary" @click="onSubmit">开始运算</el-button>
      </div>
    </div>

    <el-dialog
      :visible="visible"
      width="500px"
      class="dialog"
      :before-close="closeDialog"
      title="隐私计算反欺诈结果"
    >
      <div v-loading="resultLoading" class="desc-container">
        <el-descriptions :column="1">
          <el-descriptions-item label="推理服务名称">{{ dataList.reasoningName }}</el-descriptions-item>
          <el-descriptions-item label="推理服务ID">{{ dataList.reasoningId }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ dataList.reasoningType }}方推理</el-descriptions-item>
          <el-descriptions-item label="上线时间">{{ dataList.releaseDate }}</el-descriptions-item>
          <el-descriptions-item label="状态"><StatusIcon :status="dataList.reasoningState" /> {{ dataList.reasoningState | reasoningStateFilter }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="dataList.reasoningState === 1" class="result">
          <img src="../../../assets/result-img.png" width="200" alt="" srcset="">
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import { saveReasoning } from '@/api/reasoning'
import StatusIcon from '@/components/StatusIcon'
import { parseTime } from '@/utils/index'

export default {
  name: 'ModelInferenceTask',
  components: {
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
      resultLoading: false,
      active: 0,
      taskState: '',
      taskTimer: null,
      reasoningId: '',
      dataList: {},
      visible: false,
      loading: false,
      taskId: 0,
      selectedOrganId: '',
      createdOrgan: '',
      createdOrganId: '',
      providerOrganId: '',
      providerOrganName: ''
    }
  },
  computed: {
    ...mapState('application', ['origin'])
  },
  async created() {
    this.getLocationOrigin()
    await this.setDefault()
  },
  methods: {
    closeDialog() {
      this.visible = false
    },
    async setDefault() {
      const data = {
        'node1': {
          taskId: 299,
          providerOrganId: '3abfcb2a-8335-4bcc-b6f9-704a92e392fd',
          projectId: 51,
          modelId: 161,
          createdResourceId: 'ea5fd5f5f9f0-00bccdeb-d400-4498-b609-8985e84effd6',
          providerResourceId: '704a92e392fd-c49f9170-9d6c-4c4e-bf21-eadafdb5bb2c'
        },
        'node2': {
          taskId: 90,
          providerOrganId: '7aeeb3aa-75cc-4e40-8692-ea5fd5f5f9f0',
          projectId: 51,
          modelId: 71,
          createdResourceId: '704a92e392fd-383fd8fa-4fb8-4a46-b4b3-dfffa69ef10f',
          providerResourceId: 'ea5fd5f5f9f0-69065341-bd34-4f11-a944-e868da7aae2c'
        },
        'node3': {
          taskId: 28,
          providerOrganId: '7aeeb3aa-75cc-4e40-8692-ea5fd5f5f9f0',
          projectId: 22,
          modelId: 7,
          createdResourceId: '794e41ba0e63-f7f15a33-435a-4518-bf6c-706dcc229927',
          providerResourceId: 'ea5fd5f5f9f0-69065341-bd34-4f11-a944-e868da7aae2c'
        },
        'test1': {
          taskId: 1706,
          providerOrganId: '2cad8338-2e8c-4768-904d-2b598a7e3298',
          projectId: 409,
          modelId: 1185,
          createdResourceId: 'ece67278c395-4501b886-9d9c-4f90-afa9-488b0f4dc90d',
          providerResourceId: '2b598a7e3298-7fecfced-0e44-4212-920f-e1efc14e43df'
        },
        'other': {
          providerOrganName: '互联网公司B',
          createdOrgan: '电信运营商A',
          modelName: 'XGB',
          reasoningName: '隐私计算反欺诈应用',
          reasoningDesc: '隐私计算反欺诈应用'
        }
      }
      const { taskId, providerOrganId, projectId, modelId, createdResourceId, providerResourceId } = data[this.origin]
      this.createdOrganId = this.$store.getters.userOrganId
      this.providerOrganName = '互联网公司B'
      this.createdOrgan = '电信运营商A'
      this.modelName = 'XGB'
      this.reasoningName = '隐私计算反欺诈应用'
      this.reasoningDesc = '隐私计算反欺诈应用'
      this.taskId = taskId
      this.providerOrganId = providerOrganId
      this.projectId = projectId
      this.modelId = modelId
      this.createdResourceId = createdResourceId
      this.providerResourceId = providerResourceId
    },
    async onSubmit() {
      if (this.origin === 'other') {
        this.resultLoading = true
        this.active = 2
        this.visible = true
        setTimeout(() => {
          this.dataList = {
            reasoningId: 'f313210c-e82d-4a80-8675-5e5224746348',
            reasoningName: '隐私计算反欺诈应用',
            reasoningDesc: '隐私计算反欺诈应用',
            reasoningType: 2,
            reasoningState: 1,
            runTaskId: 2185,
            taskId: 1706,
            releaseDate: parseTime(new Date().getTime())
          }

          this.resultLoading = false
        }, 1500)
      } else {
        this.resourceList = [
          {
            participationIdentity: 1,
            resourceId: this.createdResourceId
          }, {
            participationIdentity: 2,
            resourceId: this.providerResourceId
          }
        ]
        const { code, result } = await saveReasoning({
          taskId: this.taskId,
          resourceList: this.resourceList,
          reasoningName: this.reasoningName,
          reasoningDesc: this.reasoningDesc
        })
        if (code === 0) {
          this.reasoningId = result.id
          this.visible = true
          this.getReasoning()
          this.active = 2
        }
      }
    },
    handleClose() {
      this.dialogVisible = false
    },
    openModelDialog() {
      this.dialogVisible = true
    },
    ...mapActions('application', ['getLocationOrigin'])
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-descriptions-item__content{
  font-size: 16px;
}
.dialog ::v-deep .el-descriptions-item__content{
  font-size: 14px;
}

.form-container{
  padding-top: 40px;
}
.icon{
  color: #67C23A;
  display: inline-block;
  margin-right: 10px;
}
.task-container{
  width: 300px;
  margin: 0 auto;
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
  justify-content: center;
  margin-top: 40px;
}
table,th,td {
  border: 1px solid #ebeef5;
}

table {
  width: 100%;
  margin: 0 auto;
  display: block;
  overflow-x: auto;
  border-spacing: 0;
}

tbody {
  white-space: nowrap;
}

th,
td {
  padding: 5px 10px;
  border-top-width: 0;
  border-left-width: 0;
}

th {
  position: sticky;
  top: 0;
  background: #fff;
  vertical-align: bottom;
}

th:last-child,
td:last-child {
  border-right-width: 0;
}

tr:last-child td {
  border-bottom-width: 0;
}

.dialog ::v-deep .el-descriptions__body{
  // background-color: #fafafa;
  padding: 10px 20px 20px 20px;
}
.dialog h3{
  font-size: 16px;
  color: #303133;
}
.result{
  text-align: center;
  color: #F56C6C;
  transform: rotate(-35deg);
  position: absolute;
  bottom: 0;
  right: 0;
}
p{
  font-size: 20px;
  margin: 10px auto;
  text-align: center;
}
.icon-success{
  color: #67C23A;
  font-size: 80px;
}
.icon-error{
  color: #F56C6C;
  font-size: 100px;
}
</style>
