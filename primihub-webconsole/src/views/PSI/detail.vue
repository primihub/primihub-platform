<template>
  <div v-loading="loading" class="container">
    <div class="detail">
      <h3>基础信息</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">任务名称:</div>
          <div class="desc-content">{{ taskData.taskName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">任务ID:</div>
          <div class="desc-content">{{ taskData.taskIdName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">结果输出方式:</div>
          <div class="desc-content">{{ taskData.outputFormat === '0'? '资源文件(csv)': '' }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">结果名称:</div>
          <div class="desc-content">{{ taskData.resultName }}</div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>数据配置</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">发起方:</div>
          <div class="desc-content">{{ taskData.ownOrganName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">协作方:</div>
          <div class="desc-content">{{ taskData.otherOrganName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">资源表:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toResourceDetailPage(taskData.ownResourceId)">{{ taskData.ownResourceName }}</el-link>
          </div>
        </div>
        <div class="desc-col">
          <div class="desc-label">资源表:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toUnionResourceDetailPage(taskData.otherResourceId)">{{ taskData.otherResourceName }}</el-link>
          </div>
        </div>
        <div class="desc-col">
          <div class="desc-label">关联键:</div>
          <div class="desc-content">{{ taskData.ownKeyword }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">关联键:</div>
          <div class="desc-content">{{ taskData.otherKeyword }}</div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>实现方法</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">输出内容:</div>
          <div class="desc-content">{{ taskData.outputContent=== 0 ? '交集': '差集' }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">实现方法:</div>
          <div class="desc-content">{{ taskData.tag === 1 ? 'KKRT' : taskData.tag === 2 ? 'TEE' : 'ECDH' }}</div>
        </div>
        <div v-if="taskData.tag === 2" class="desc-col flex" style="width: 100%;">
          <div class="desc-label">可信计算节点:</div>
          <div class="desc-content">
            {{ taskData.teeOrganName }}
            <p style="font-size: 12px; margin-top: 5px;">注：第三方可信计算节点为双方提供数据机密性和完整性保护的前提，并支持计算</p>
          </div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>具体实现</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">任务发起时间:</div>
          <div class="desc-content">{{ taskData.createDate }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">任务耗时:</div>
          <div class="desc-content">{{ taskData.consuming | timeFilter }}</div>
        </div>
        <div class="desc-col align-items-center" style="width: 100%;">
          <div class="desc-label">实现过程:</div>
          <div class="desc-content">
            <el-steps :active="stepActive" simple finish-status="success" process-status="wait">
              <el-step v-for="(step) in stepData" :key="step.step" :title="step.title" :status="step.status" @mouseover.native="handleStepOver(step)" @mouseleave.native="showError = false" />
            </el-steps>
            <div v-if="showError" class="task-error">
              <p>错误信息：</p>
              <p v-for="(item,index) in taskError" :key="index">
                {{ item }}
              </p>
            </div>
          </div>
        </div>
        <div v-if="taskData.taskState === 1" class="desc-col" style="width: 100%;">
          <div class="desc-label">计算结果:</div>
          <div class="desc-content flex">
            <el-link :underline="false" class="margin-right-10" type="primary" @click="downloadPsiTask">{{ taskData.resultName }}.csv <svg-icon icon-class="download" /></el-link>
            <el-button size="small" type="primary" plain @click="downloadPsiTask">下载文档</el-button>
            <el-button size="small" type="primary" plain @click="handlePreview">在线预览</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- preview dialog -->
    <ResourcePreviewDialog
      :data="previewList"
      :visible.sync="previewDialogVisible"
      append-to-body
      @close="closeDialog"
    />
  </div>
</template>

<script>
import { getPsiTaskDetails } from '@/api/PSI'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'
import { getToken } from '@/utils/auth'

export default {
  components: {
    ResourcePreviewDialog
  },
  data() {
    return {
      loading: false,
      showError: false,
      timer: null,
      previewList: [],
      previewDialogVisible: false,
      taskData: [],
      taskId: this.$route.params.id,
      taskState: '',
      stepActive: 1,
      stepFinishStatus: 'success',
      stepData: [{
        step: 1,
        title: '提交任务',
        status: 'success'
      }, {
        step: 2,
        title: '任务运行',
        status: 'wait'
      }, {
        step: 3,
        title: '等待结果',
        status: 'wait'
      }]
    }
  },
  async created() {
    await this.fetchData()
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    handleStepOver() {
      if (this.taskState !== 3) {
        this.showError = false
      } else {
        this.showError = true
      }
    },
    async downloadPsiTask() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/psi/downloadPsiTask?taskId=${this.taskData.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    async fetchData() {
      this.loading = true
      getPsiTaskDetails({ taskId: this.taskId }).then(res => {
        if (res.code === 0) {
          this.taskData = res.result
          this.taskState = this.taskData.taskState
          if (this.taskState === 2) {
            this.timer = window.setInterval(() => {
              setTimeout(this.fetchData(), 0)
            }, 3000)
          } else {
            clearInterval(this.timer)
          }
          this.previewList = this.taskData.dataList
          this.taskError = this.taskData.taskError ? this.taskData.taskError.split('\n') : ''
          switch (this.taskState) {
            case 1:
              this.stepData[1].title = '任务运行'
              this.stepData[1].status = 'success'
              this.stepData[2].title = '运行成功'
              this.stepData[2].status = 'success'
              clearInterval(this.timer)
              this.stepActive = 3
              break
            case 2:
              this.stepData[1].text = '任务运行'
              this.stepData[1].status = 'process'
              this.stepActive = 2
              break
            case 3:
              this.stepData[1].text = '任务运行'
              this.stepData[1].status = 'error'
              this.stepData[2].title = '运行失败'
              this.stepData[2].status = 'error'
              clearInterval(this.timer)
              this.stepActive = 3
              break
            case 4:
              this.stepActive = 3
              this.stepData[1].text = '任务运行'
              this.stepData[1].status = 'error'
              this.stepData[2].title = '任务取消'
              this.stepData[2].status = 'error'
              clearInterval(this.timer)
              break
            default:
              break
          }
        } else {
          clearInterval(this.timer)
        }
        this.loading = false
      }).catch(err => {
        console.log(err)
        this.loading = false
        clearInterval(this.timer)
      })
    },
    closeDialog() {
      this.previewDialogVisible = false
    },
    async handlePreview() {
      this.previewDialogVisible = true
    }

  }
}
</script>

<style lang="scss" scoped>
.detail{
  margin-bottom: 40px;
  h3{
    border-left: 3px solid #1677FF;
    padding-left: 10px;
  }
}
.description-container{
  display: flex;
  flex-flow: wrap;
  background-color: #fafafa;
  padding: 25px 40px 25px 40px;
  line-height: 1.5;
  margin-bottom: 20px;
  border-radius: 12px;
  &.w100{
    display: block;
  }
}
.infos-title{
  font-size: 16px;
}

.desc-col{
  width: 50%;
  flex-shrink: 0;
  display: flex;
  color: #606266;
  font-size: 14px;
  margin: 6px 0;
  position: relative;
}
.desc-label{
  margin-right: 10px;
}
.desc-content{
  flex: 1;
  padding-right: 10px;
}

.task-error{
  width: 300px;
  padding: 10px;
  color: #fff;
  background-color: rgba($color: #000000, $alpha: .7);
  position: absolute;
  top: 40px;
  left: calc((100% / 2) - 100px);
  border-radius: 4px;
}
.el-steps--simple{
  margin:  5px 0;
  padding: 11px 8%;
}

</style>
