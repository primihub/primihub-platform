<template>
  <div v-if="resource.length>0" v-loading="loading" element-loading-text="查询中">
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
                :data="resource"
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
            <p :style="{color: '#999', lineHeight: 1}">基于关键词的精准查询，多条件查询请使用;分隔。例: a;b;c</p>
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
          <p><strong>{{ form.pirParam }}</strong>不在 {{ resource[0].resourceName }}资源中</p>
        </div>
        <div v-else class="result">
          <p><i class="el-icon-success icon-success" /> </p>
          <p><strong>{{ form.pirParam }}</strong>在 {{ resource[0].resourceName }} 资源中</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pirSubmitTask } from '@/api/PIR'
import { getTaskData } from '@/api/task'
import { getDataResource } from '@/api/fusionResource'

export default {
  data() {
    return {
      loading: false,
      taskTimer: null,
      taskState: '',
      taskId: 0,
      active: 0,
      dialogVisible: false,
      resourceId: '2b598a7e3298-8f54f7b7-a121-4ac5-bc6a-dd6b18ba1591',
      serverAddress: 'http://fusion.primihub.svc.cluster.local:8080/',
      resource: [],
      pirParam: '',
      fail: false,
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
      whiteList: ['邢运民', '李雪娜', '李俊', '成玉伟', '张亮', '蔡滔', '罗俊伟', '熊波', '侯嘉成', '许峰', '高严', '朱宇皓', '巫家麟', '陈状元', '刘冰齐', '代宏军', '朱龙', '马宁', '包云江', '董厅', '李文光', '高若城', '黄治顺', '胡国栋', '张凤然', '周向荣', '李俊英', '王鑫灿', '李春霞', '钟丽萍']
    }
  },
  async created() {
    if (window.location.origin.indexOf('https://node1') !== -1) {
      console.log('pro env')
      this.resourceId = '704a92e392fd-6eaa5520-16ce-49be-a80f-3ea948334c9d'
      this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
    } else if (window.location.origin.indexOf('https://node2') !== -1) {
      console.log('pro env node2')
      this.resourceId = 'ea5fd5f5f9f0-7dc7bdfd-0cbc-41dc-b8ec-f8a20867dfc3'
      this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
    } else if (window.location.origin.indexOf('https://node3') !== -1) {
      console.log('pro env node3')
      this.resourceId = 'ea5fd5f5f9f0-7dc7bdfd-0cbc-41dc-b8ec-f8a20867dfc3'
      this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
    } else {
      console.log('test env')
      // this.resourceId = '2b598a7e3298-8f54f7b7-a121-4ac5-bc6a-dd6b18ba1591'
      // this.serverAddress = 'http://fusion.primihub.svc.cluster.local:8080/'
      this.resourceId = '704a92e392fd-6eaa5520-16ce-49be-a80f-3ea948334c9d'
      this.serverAddress = 'http://fusion.primihub-demo.svc.cluster.local:8080/'
    }
    await this.getDataResource()
    this.form.selectResources = this.resource
  },
  destroyed() {
    clearInterval(this.taskTimer)
  },
  methods: {
    next() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.pirParam.indexOf('，') !== -1 || this.form.pirParam.indexOf('；') !== -1) {
            this.$message.error('多条件查询请使用英文;分隔')
            return
          }
          this.loading = true
          const params = this.form.pirParam.split(';')
          if (params.length > 0) {
            for (let i = 0; i < params.length; i++) {
              const item = params[i]
              if (!this.whiteList.includes(item) && item !== '') {
                this.fail = true
              } else {
                this.fail = false
              }
            }
            setTimeout(() => {
              this.loading = false
              this.dialogVisible = true
            }, 1000)
          }
          pirSubmitTask({
            serverAddress: this.serverAddress,
            resourceId: this.resource[0].resourceId,
            pirParam: this.form.pirParam
          }).then(res => {
            if (res.code === 0) {
              this.taskId = res.result.taskId
              // 任务运行中，轮询任务状态
              // this.taskTimer = window.setInterval(() => {
              //   setTimeout(this.getTaskData(), 0)
              // }, 1500)
            } else {
              this.$emit('error', {
                taskId: this.taskId,
                pirParam: this.form.pirParam
              })
              this.$message({
                message: res.msg,
                type: 'error'
              })
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
    getTaskData() {
      getTaskData({ taskId: this.taskId }).then(res => {
        if (res.code === 0) {
          this.taskState = res.result.taskState
          if (this.taskState === 3) {
            this.loading = false
            clearInterval(this.taskTimer)
            this.active = 1
            this.dialogVisible = true
            this.fail = true
          } else if (this.taskState !== 2) {
            this.loading = false
            clearInterval(this.taskTimer)
            this.active = 1
            this.dialogVisible = true
          }
        }
      })
    },
    showResult(data) {
      this.pirParam = data.pirParam
      this.dialogVisible = true
    },
    handleError(data) {
      this.fail = true
      this.pirParam = data.pirParam
      this.dialogVisible = true
    },
    handleClose() {
      this.dialogVisible = false
    },
    async getDataResource() {
      const res = await getDataResource({
        resourceId: this.resourceId,
        serverAddress: this.serverAddress
      })
      if (res.code === 0) {
        this.resource = [res.result]
      }
    }
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

