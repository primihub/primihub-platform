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
                  width="180"
                />
                <el-table-column
                  prop="resourceContainsY"
                  align="center"
                  label="是否包含Y值"
                >
                  <template slot-scope="{row}">{{ row.resourceContainsY === 1? '是' : '否' }}</template>
                </el-table-column>
                <el-table-column
                  prop="resourceColumnCount"
                  align="center"
                  label="特征量"
                />
                <el-table-column
                  prop="resourceRowsCount"
                  align="center"
                  label="样本量"
                />
                <el-table-column
                  prop="resourceYRowsCount"
                  align="center"
                  label="正例样本数量"
                >
                  <template slot-scope="{row}">{{ row.resourceYRowsCount || 0 }}</template>
                </el-table-column>
                <el-table-column
                  prop="resourceYRatio"
                  align="center"
                  label="正例样本比例"
                >
                  <template slot-scope="{row}">{{ row.resourceYRatio || 0 }}%</template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </div>
          <el-form-item label="关键词" prop="pirParam">
            <el-input v-model="form.pirParam" placeholder="请输入关键词" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item>
            <p :style="{color: '#999', lineHeight: 1}">基于关键词的精准查询，多条件查询请使用英文,分隔。例: a,b,c</p>
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
          <p class="el-icon-error icon-error" />
          <p><strong>{{ form.pirParam }}</strong>不在{{ form.selectResources[0].resourceName }} 资源中</p>
        </div>
        <div v-else class="result">
          <p><i class="el-icon-success icon-success" /> </p>
          <p><strong>{{ existKeyWords.join(',') }}</strong>在{{ form.selectResources[0].resourceName }} 资源中</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import {getPirTaskDetail, pirSubmitTaskMarket} from '@/api/PIR'
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
        pirParam: '',
        selectResources: []
      },
      rules: {
        selectResources: [
          { required: true, message: '请选择资源', trigger: 'blur' }
        ],
        pirParam: [
          { required: true, message: '请输入关键词', trigger: 'blur' },
          { max: 50, message: '长度在50个字符以内', trigger: 'blur' }
        ]
      },
      keyWordsMap: {}, // Plain ciphertext of keywords
      existKeyWords: []
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
      this.resourceId = this.marketInfo.pir.resourceId
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
          taskState === 3 && this.$message.error('查询失败')
          taskState === 4 && this.$message.warning('查询已取消')
          if (taskState === 1 ){
            this.dialogVisible = true
            list.forEach(item => {
              const ele = this.keyWordsMap[item.value]
              !this.existKeyWords.includes(ele) && this.existKeyWords.push(ele)
            })
            this.fail = list.length === 0
          }
        } else {
          timer = window.setTimeout(() => {
            this.getPirTaskResult()
          }, 1000)
        }
      }
    },

    // 查询
    next() {
      this.keyWordsMap = {} // reset keyWordsMap
      this.existKeyWords = [] // reset existKeyWords
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.pirParam.indexOf('，') !== -1 || this.form.pirParam.indexOf('；') !== -1 || this.form.pirParam.indexOf(';') !== -1) {
            this.$message.error('多条件查询请使用英文,分隔')
            return
          }

          // sha256加密
          const paramsList = this.form.pirParam.split(',')
          const hashParams = paramsList.map(item => {
            this.keyWordsMap[CryptoJS.SHA256(item).toString()] = item
            return CryptoJS.SHA256(item)
          }).join(',')

          this.loading = true
          pirSubmitTaskMarket({
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
    font-size: 20px;
    margin: 10px auto;
    strong{
     font-weight: normal;
    }
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

