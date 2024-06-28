<template>
  <div v-if="form.selectResources.length>0" v-loading="loading" element-loading-text="查询中">
    <div class="search-area">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="查询条件" />
        <el-step title="查询结果" />
      </el-steps>
      <el-form ref="form" :model="form" :rules="rules" label-width="auto" class="form">
        <div class="select-resource">
          <div class="select-row">
            <el-form-item label="已选资源" prop="selectResources">
              <el-table
                :data="form.selectResources"
                border
                style="width: 100%"
              >
                <el-table-column
                  prop="resourceName"
                  align="center"
                  label="资源名称"
                />
                <el-table-column
                  prop="resourceId"
                  label="资源ID"
                  align="center"
                />
                <el-table-column
                  prop="resourceRowsCount"
                  align="center"
                  label="样本量"
                />
              </el-table>
            </el-form-item>
          </div>
          <el-form-item>
            <p :style="{color: '#999', lineHeight: 1}">节点和数据由山东大学网安研究团队提供</p>
          </el-form-item>
          <el-form-item label="查询内容" prop="pirParam">
            <el-input v-model="form.mobile" placeholder="请输入手机号" class="mb10" @blur="() => form.password && (form.pirParam = `${form.mobile}:${form.password}`)" />
            <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password @blur="() => form.mobile && (form.pirParam = `${form.mobile}:${form.password}`)" />
          </el-form-item>
          <el-form-item>
            <p :style="{color: '#999', lineHeight: 1}">利用隐匿查询技术在泄露密码库里查询确认您的账号密码是否泄露</p>
          </el-form-item>
          <el-form-item style="text-align: center">
            <el-button style="margin: 12px auto;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <el-dialog
      title="查询结果"
      :visible.sync="dialogVisible"
      width="30%"
      :before-close="handleClose"
    >
      <div class="dialog-body">
        <div v-if="fail" class="result">
          <p><i class="el-icon-success icon-success" /> </p>
          <p><strong>{{ form.mobile }}</strong>不在泄漏的账号密码数据资源中</p>
          <p class="search-tip">未发现{{ form.mobile }}账号关联的密码泄漏风险</p>
        </div>
        <div v-else class="result">
          <p class="el-icon-warning icon-error" />
          <p><strong>{{ form.mobile }}</strong>在泄漏的账号密码数据资源中</p>
          <p class="search-tip">建议更新{{ form.mobile }}相关的账号密码</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import { pirSubmitTask, getPirTaskDetail } from '@/api/PIR'
import { getDataResource } from '@/api/fusionResource'
import CryptoJS from 'crypto-js'

export default {
  data() {
    return {
      loading: false,
      dialogVisible: false,
      pirParam: '',
      fail: false,
      active: 1,
      form: {
        resourceName: '',
        mobile: '',
        password: '',
        pirParam: '',
        selectResources: []
      },
      rules: {
        selectResources: [
          { required: true, message: '请选择资源', trigger: 'blur' }
        ],
        mobile: [
          { required: true, message: '请输入手机号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        pirParam: [
          { required: true, message: '请输入完整的查询内容', trigger: 'blur' },
          { max: 50, message: '长度在50个字符以内', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapState('application', ['marketInfo'])
  },
  async created() {
    await this.getMarketInfo()
    this.setDefaultValue()
  },
  destroyed() {
    clearInterval(this.taskTimer)
  },
  methods: {
    // 设置资源id
    async setDefaultValue() {
      this.resourceId = this.marketInfo.credentialsPIR.resourceId
      await this.getDataResource()
      this.form.selectResources = this.resource
    },

    // 获取查询结果
    async getPirTaskResult() {
      const { code, result } = await getPirTaskDetail({ taskId: this.taskId })
      let timer = null
      if (code === 0) {
        const { taskState, list } = result
        if (taskState !== 2) {
          this.loading = false
          timer && window.clearTimeout(timer)
          taskState === 1 && (this.dialogVisible = true, this.fail = list.length === 0)
          taskState === 3 && this.$message.error('查询失败')
          taskState === 4 && this.$message.warning('查询已取消')
        } else {
          timer = window.setTimeout(() => {
            this.getPirTaskResult()
          }, 1000)
        }
      }
    },

    // 查询
    next() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // sha256加密
          const hashParams = CryptoJS.SHA256(this.form.pirParam).toString()

          this.loading = true
          pirSubmitTask({
            resourceId: this.resource[0].resourceId,
            pirParam: hashParams
          }).then(res => {
            if (res.code === 0) {
              this.taskId = res.result.taskId
              this.getPirTaskResult()
            } else {
              this.$message.error(res.msg)
              this.loading = false
            }
          }).catch(err => {
            console.log(err)
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    handleClose() {
      this.dialogVisible = false
    },

    // 获取资源信息
    async getDataResource() {
      const res = await getDataResource({
        resourceId: this.resourceId
      })
      if (res.code === 0) {
        this.resource = [res.result]
      }
    },
    ...mapActions('application', ['getMarketInfo'])
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-dialog__body{
  padding: 0px;
}
.mb10{
  margin-bottom: 10px;
}
.form{
  margin-top: 30px;
}
.search-area {
  margin: 20px auto;
  padding: 30px;
  // width: 800px;
}
.result{
  padding: 0 30px;
}
.dialog-body{
  text-align: center;
  padding-bottom: 30px;
  p{
    font-size: 18px;
    color: #333;
    margin: 10px auto;
    strong{
     font-weight: normal;
    }
  }
  .search-tip{
  font-size: 14px;
  color: #666;
}
  .icon-success{
    color: #67C23A;
    font-size: 80px;
  }
  .icon-error{
    color: #F56C6C;
    font-size: 100px;
  }
}
</style>

