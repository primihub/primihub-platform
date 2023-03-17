<template>
  <div v-loading="loading" element-loading-text="开始运算" class="form-container">
    <el-steps :active="active" finish-status="success" align-center>
      <el-step title="查询条件" />
      <el-step title="查询结果" />
    </el-steps>
    <div class="task-container">
      <el-descriptions :column="1" :colon="false">
        <el-descriptions-item><i class="icon el-icon-success" />发起方： {{ createdOrgan }}</el-descriptions-item>
        <el-descriptions-item><i class="icon el-icon-success" /> 参与方： {{ providerOrganName }}</el-descriptions-item>
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
      <div class="desc-container">
        <el-descriptions :column="1">
          <el-descriptions-item label="推理服务名称">{{ dataList.reasoningName }}</el-descriptions-item>
          <el-descriptions-item label="推理服务ID">{{ dataList.reasoningId }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ dataList.reasoningType }}方推理</el-descriptions-item>
          <el-descriptions-item label="上线时间">{{ dataList.releaseDate }}</el-descriptions-item>
          <el-descriptions-item label="状态"><StatusIcon :status="dataList.reasoningState" /> {{ dataList.reasoningState | reasoningStateFilter }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="dataList.reasoningState === 1" class="result">
          <!-- <h3>运算结果</h3> -->
          <!-- <p class="el-icon-error icon-error" />
          <p>存在欺诈</p> -->
          <img src="../../../assets/result-img.png" width="200" alt="" srcset="">
        </div>

        <!-- <div v-if="dataList.reasoningState === 1" style="background-color: #fafafa;padding: 10px 20px 10px 20px;">
          <table>
            <tbody>
              <tr>
                <td>pred_y</td>
                <td>0.719003345484603</td>
                <td>0.738941730799224</td>
                <td>0.43052668344885</td>
                <td>0.43052668344885</td>
              </tr>
            </tbody>
          </table>
        </div> -->
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { saveReasoning, getReasoning } from '@/api/reasoning'
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
      providerOrganName: '',
      serverAddress: ''
    }
  },
  async created() {
    await this.setDefault()
  },
  methods: {
    closeDialog() {
      this.visible = false
    },
    async getReasoning() {
      this.loading = true
      const res = await getReasoning({ id: this.reasoningId })
      if (res.code === 0) {
        this.dataList = res.result
        this.dataList.releaseDate = parseTime(new Date().getTime())
        this.reasoningState = this.dataList.reasoningState
        this.dataList.reasoningState = 2
        setTimeout(() => {
          this.dataList.reasoningState = 1
          this.loading = false
        }, 1500)
      }
    },
    async setDefault() {
      this.createdOrganId = this.$store.getters.userOrganId
      this.providerOrganName = '互联网公司B'
      this.createdOrgan = '电信运营商A'
      this.modelName = 'XGB'
      this.reasoningName = '隐私计算反欺诈应用'
      this.reasoningDesc = '隐私计算反欺诈应用'
      if (window.location.origin.indexOf('https://node1') !== -1) {
        console.log('pro env node1')
        this.taskId = 299
        this.providerOrganId = '3abfcb2a-8335-4bcc-b6f9-704a92e392fd'
        this.projectId = 51
        this.modelId = 161
        this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
        this.createdResourceId = 'ea5fd5f5f9f0-00bccdeb-d400-4498-b609-8985e84effd6'
        this.providerResourceId = '704a92e392fd-c49f9170-9d6c-4c4e-bf21-eadafdb5bb2c'
      } else if (window.location.origin.indexOf('https://node2') !== -1) {
        console.log('pro env node2')
        this.taskId = 90
        this.providerOrganId = '7aeeb3aa-75cc-4e40-8692-ea5fd5f5f9f0'
        this.projectId = 51
        this.modelId = 71
        this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
        this.createdResourceId = '704a92e392fd-383fd8fa-4fb8-4a46-b4b3-dfffa69ef10f'
        this.providerResourceId = 'ea5fd5f5f9f0-69065341-bd34-4f11-a944-e868da7aae2c'
      } else if (window.location.origin.indexOf('https://node3') !== -1) {
        console.log('pro env node3')
        this.taskId = 28
        this.providerOrganId = '7aeeb3aa-75cc-4e40-8692-ea5fd5f5f9f0'
        this.projectId = 22
        this.modelId = 7
        this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
        this.createdResourceId = '794e41ba0e63-f7f15a33-435a-4518-bf6c-706dcc229927'
        this.providerResourceId = 'ea5fd5f5f9f0-69065341-bd34-4f11-a944-e868da7aae2c'
      } else {
        console.log('test env')
        this.taskId = 1706
        this.providerOrganId = '2cad8338-2e8c-4768-904d-2b598a7e3298'
        this.projectId = 409
        this.modelId = 1185
        this.serverAddress = 'http://fusion.primihub.svc.cluster.local:8080/'
        this.createdResourceId = 'ece67278c395-4501b886-9d9c-4f90-afa9-488b0f4dc90d'
        this.providerResourceId = '2b598a7e3298-7fecfced-0e44-4212-920f-e1efc14e43df'
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
    async onSubmit() {
      this.active = 1
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
    },
    handleClose() {
      this.dialogVisible = false
    },
    openModelDialog() {
      this.dialogVisible = true
    }
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
