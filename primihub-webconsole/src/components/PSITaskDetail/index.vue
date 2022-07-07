<template>
  <div class="task-detail">
    <el-descriptions title="" :column="1" label-class-name="detail-title">
      <el-descriptions-item label="求交结果名称">{{ data.resultName }}</el-descriptions-item>
      <el-descriptions-item label="任务Id">{{ data.taskIdName }}</el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="求交数据配置" :column="2" label-class-name="detail-title">
      <el-descriptions-item label="节点1">{{ data.ownOrganName }}</el-descriptions-item>
      <el-descriptions-item label="节点2">{{ data.otherOrganName }}</el-descriptions-item>
      <el-descriptions-item label="数据表">{{ data.ownResourceName }}</el-descriptions-item>
      <el-descriptions-item label="数据表">{{ data.otherResourceName }}</el-descriptions-item>
      <el-descriptions-item label="关联键">{{ data.ownKeyword }}</el-descriptions-item>
      <el-descriptions-item label="关联键">{{ data.otherKeyword }}</el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="求交配置方式" :column="2" label-class-name="detail-title">
      <el-descriptions-item label="输出内容">{{ data.outputContent=== 0? '交集': '差集' }}</el-descriptions-item>
      <el-descriptions-item label="输出格式">{{ data.outputFormat === '0'? '资源文件(csv)': '' }}</el-descriptions-item>
      <!-- <el-descriptions-item label="结果获取方">{{ data.resultOrganName }}</el-descriptions-item> -->
      <el-descriptions-item v-if="data.remarks !== null " label="备注">{{ data.remarks }}</el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="高级设置" :column="1" label-class-name="detail-title">
      <el-descriptions-item label="输出资源路径">{{ data.outputFilePathType === 0? '自动生成': '' }}</el-descriptions-item>
      <el-descriptions-item v-if="data.taskState === 1" :label="`${data.resultOrganName}`"><el-button type="text" @click="downloadPsiTask">{{ data.resultName }}.csv <i class="el-icon-download" /></el-button></el-descriptions-item>
      <el-descriptions-item label="关键键有重复值时">{{ data.outputNoRepeat === 1? '去重': '不去重' }}</el-descriptions-item>
      <!-- <el-descriptions-item label="是否对&quot;可统计&quot;的附加列做全表统计">{{ data.columnCompleteStatistics === 1? '否': '是' }}</el-descriptions-item> -->
    </el-descriptions>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'

export default {
  name: 'PSITaskDetail',
  components: {
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
    async downloadPsiTask() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/psi/downloadPsiTask?taskId=${this.data.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-table th{
  background: #fafafa;
}
::v-deep .el-descriptions__body{
  background-color: #fafafa;
  padding: 10px 20px 10px 20px;
}
::v-deep  .el-descriptions{
  margin-bottom:20px;
}
::v-deep .el-descriptions-item__container{
  align-items: center;
}
::v-deep .el-descriptions__header{
  margin-bottom: 10px;
}
::v-deep .el-dialog__body{
  padding: 20px 20px 10px 20px;
}
.result-name{
  cursor: pointer;
  &:hover{
    color: #1989fa;
  }
}
</style>
