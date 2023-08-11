<template>
  <div class="task-detail">
    <h2>基本信息</h2>
    <div class="description-container">
      <div class="desc-col">
        <div class="desc-label">任务结果名称:</div>
        <div class="desc-content">
          <EditInput type="textarea" show-word-limit maxlength="50" size="mini" :value="data.resultName" @change="handleEditChange" />
        </div>
      </div>
      <div class="desc-col">
        <div class="desc-label">任务ID:</div>
        <div class="desc-content">{{ data.taskIdName }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">输出内容:</div>
        <div class="desc-content">{{ data.outputContent=== 0? '交集': '差集' }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">输出格式:</div>
        <div class="desc-content">{{ data.outputFormat === '0'? '资源文件(csv)': '' }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">实现协议:</div>
        <div class="desc-content">{{ data.tag === 0? 'ECDH': 'KKRT' }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">关键键有重复值时:</div>
        <div class="desc-content">{{ data.outputNoRepeat === 1? '去重': '不去重' }}</div>
      </div>
      <div v-if="data.taskState === 1 && showDownload" class="desc-col">
        <div class="desc-label">任务结果:</div>
        <div class="desc-content">
          <el-link :underline="false" type="primary" @click="downloadPsiTask">{{ data.resultName }}.csv <svg-icon icon-class="download" /></el-link>
        </div>
      </div>
      <div class="desc-col" style="width: 100%;">
        <div class="desc-label">备注:</div>
        <div class="desc-content">{{ data.remarks }}</div>
      </div>
    </div>
    <h2>数据配置</h2>
    <div class="description-container">
      <div class="desc-col">
        <div class="desc-label">发起方:</div>
        <div class="desc-content">{{ data.ownOrganName }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">协作方:</div>
        <div class="desc-content">{{ data.otherOrganName }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">资源表:</div>
        <div class="desc-content">
          <el-link type="primary" @click="toResourceDetailPage(data.ownResourceId)">{{ data.ownResourceName }}</el-link>
        </div>
      </div>
      <div class="desc-col">
        <div class="desc-label">资源表:</div>
        <div class="desc-content">
          <el-link type="primary" @click="toUnionResourceDetailPage(data.otherResourceId)">{{ data.otherResourceName }}</el-link>
        </div>
      </div>
      <div class="desc-col">
        <div class="desc-label">关联键:</div>
        <div class="desc-content">{{ data.ownKeyword }}</div>
      </div>
      <div class="desc-col">
        <div class="desc-label">关联键:</div>
        <div class="desc-content">{{ data.otherKeyword }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { updateDataPsiResultName } from '@/api/PSI'
import EditInput from '@/components/editInput'
export default {
  name: 'PSITaskDetail',
  components: {
    EditInput
  },
  filters: {
    // 运行状态 0未运行 1完成 2运行中 3失败 默认0
    taskStateFilter(state) {
      const stateMap = {
        0: '未运行',
        1: '完成',
        2: '运行中',
        3: '失败'
      }
      return stateMap[state]
    }
  },
  props: {
    showDownload: {
      type: Boolean,
      default: true
    },
    data: {
      type: Object,
      required: true,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
    }
  },
  methods: {
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'ResourceDetail',
        params: { id }
      })
    },
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
      window.open(`${process.env.VUE_APP_BASE_API}/data/psi/downloadPsiTask?taskId=${this.data.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    handleEditChange({ change, value }) {
      if (change) {
        this.data.resultName = value
        updateDataPsiResultName({ id: this.data.taskId, resultName: this.data.resultName }).then(res => {
          if (res.code === 0) {
            this.$emit('change', {
              taskId: this.data.taskId,
              resultName: this.data.resultName
            })
            this.$message.success('修改成功')
          } else {
            this.$message.error('修改失败')
          }
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.task-detail{
  padding: 0 10px;
}
h2{
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 20px;
  margin-block-start: 0;
}
.description-container{
  display: flex;
  flex-flow: wrap;
  background-color: #fafafa;
  padding: 20px 20px 10px 20px;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 20px;
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
  margin-bottom: 12px;
}
.desc-label{
  margin-right: 10px;
}
.desc-content{
  flex: 1;
  padding-right: 10px;
}

::v-deep .el-descriptions-item__container .el-descriptions-item__content{
  align-items: center;
}
::v-deep .el-descriptions__body{
  background-color: #fafafa;
  padding: 20px 10px 10px 10px;
  font-size: 14px;
  line-height: 35px;
  margin-bottom: 20px;
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
  margin-bottom: 12px;
  align-items: center;
  height: 22px;
}
.desc-label{
  margin-right: 10px;
}
.desc-content{
  flex: 1;
}

::v-deep .el-descriptions-item__container .el-descriptions-item__content{
  align-items: center;
}
::v-deep .el-descriptions__body{
  background-color: #fafafa;
  padding: 20px 10px 10px 10px;
  font-size: 14px;
  line-height: 35px;
}
::v-deep  .el-descriptions{
  margin-bottom:30px;
  padding: 0 10px;
}
::v-deep .el-descriptions-item{
  padding: 5px 10px;
}
.download-button{
  padding: 0;
  line-height: 1;
}
.svg-icon{
  width: 25px;
  height: 18px;
  vertical-align: middle;
}
</style>
