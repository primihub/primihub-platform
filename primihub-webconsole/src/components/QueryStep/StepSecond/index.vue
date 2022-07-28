<template>
  <div class="container">
    <el-row>
      <el-col :span="5">检索ID：</el-col>
      <el-col :span="4">{{ pirParam }}</el-col>
    </el-row>
    <div class="buttons">
      <el-button v-if="hasSearchPermission" type="primary" size="medium" @click="reset">重新查询<i class="el-icon-search el-icon--right" /></el-button>
      <el-button plain size="medium" @click="download">导出数据<i class="el-icon-download el-icon--right" /></el-button>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
export default {
  name: 'StepSecond',
  props: {
    pirParam: {
      type: String,
      default: ''
    },
    taskId: {
      type: Number,
      require: true,
      default: -1
    }
  },
  data() {
    return {
    }
  },
  computed: {
    hasSearchPermission() {
      return this.$store.getters.buttonPermissionList.includes('PrivateSearchButton')
    }
  },
  methods: {
    reset() {
      this.$emit('reset')
    },
    async download() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${this.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    }
  }
}
</script>

<style lang="scss" scoped>
.container{
  width: 600px;
  padding: 30px 0;
  text-align: center;
  margin: 0 auto;
}
.el-row{
  display: flex;
  justify-content: center;
  line-height: 30px;
  color: #606266;
}
.buttons{
  margin: 30px 0;
}
</style>
