<template>
  <div v-loading="loading" class="container">
    <div class="detail">
      <h3>基础信息</h3>
      <div class="description-container section">
        <div class="desc-col">
          <div class="desc-label">任务名称:</div>
          <div class="desc-content">{{ taskData.taskName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">任务ID:</div>
          <div class="desc-content">{{ taskData.taskIdName }}</div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>数据配置</h3>
      <div class="description-container section">
        <div class="desc-col">
          <div class="desc-label">被查询方:</div>
          <div class="desc-content">{{ taskData.organName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">资源表:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toUnionResourceDetailPage(taskData.resourceId)">{{ taskData.resourceName }}</el-link>
          </div>
        </div>
        <div class="desc-col">
          <div class="desc-label">关键词:</div>
          <div class="desc-content">{{ taskData.retrievalId }}</div>
        </div>
      </div>

    </div>
    <div class="detail">
      <h3>具体实现</h3>
      <div class="description-container section">
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
            <FlowStep :active="stepActive" :data="stepData" :task-state="taskState" :error-text="taskError" />
          </div>
        </div>
        <div v-if="taskData.taskState === 1" class="desc-col align-items-center" style="width: 100%;">
          <div class="desc-label">计算结果:</div>
          <div class="desc-content flex">
            <el-link :underline="false" class="margin-right-10" type="primary" @click="downloadPsiTask">{{ taskData.taskName }}任务结果.csv <svg-icon icon-class="download" /></el-link>
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
import { getPirTaskDetail } from '@/api/PIR'
import { getToken } from '@/utils/auth'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'
import FlowStep from '@/components/FlowStep'

const intersection = require('@/assets/intersection.svg')
const diffsection = require('@/assets/diffsection.svg')

export default {
  components: {
    ResourcePreviewDialog,
    FlowStep
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
      taskState: 0,
      stepActive: 1,
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
      }],
      taskError: [],
      taskTypeText: '计算'
    }
  },
  computed: {
    centerImg() {
      return this.taskData.outputContent === 0 ? intersection : this.taskData.outputContent === 1 ? diffsection : intersection
    }

  },
  async created() {
    await this.fetchData()
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    toUnionResourceDetailPage(id) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: { id }
      })
    },
    async downloadPsiTask() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/psi/downloadPsiTask?taskId=${this.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    async fetchData() {
      this.loading = true
      getPirTaskDetail({ taskId: this.taskId }).then(res => {
        if (res.code === 0) {
          this.taskData = res.result
          this.taskState = this.taskData.taskState
          if (this.taskState === 2) {
            this.timer = window.setInterval(() => {
              setTimeout(this.fetchData(), 0)
            }, 1500)
          } else {
            clearInterval(this.timer)
          }
          this.previewList = this.taskData.dataList
          this.taskError = this.taskData.taskError ? this.taskData.taskError.split('\n') : []
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
@import "~@/styles/taskDetail.scss";
.description-container{
  flex: 1;
  &.dataset-container{
    // width: 300px;
    // margin-left: 160px;
    // margin-right: 50px;
  }
}
.section .dataset-container{
  display: inline-block;
  // &:first-child{
  //   margin-left: 100px;
  // }
  .desc-col{
    width: 100%;
  }
}
.relation-img{
  display: inline-block;
  position: relative;
  width: 300px;
  margin: 15px 70px;
  vertical-align: top;
  .line{
    border-top: 1px dotted #cccccc;
    margin: 0px auto 0;
    &-icon{
      padding: 0 3px;
      border: 1px solid #666;
      position: absolute;
      left: 50%;
      transform: translate3d(-50%,0,0);
      top: -12px;
      background-color: #fff;
      font-size: 12px;
      line-height: 20px;
      text-align: center;
      color: #333;
    }
  }
  .center{
    // margin: 0 25px 0 60px;
    text-align: center;
    font-size: 12px;
    color: #666;
    margin-top: 25px;
    img{
      width: 28px;
    }
  }
}

</style>
