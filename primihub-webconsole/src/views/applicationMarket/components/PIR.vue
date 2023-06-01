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
          <p><strong>{{ form.pirParam }}</strong>不在 {{ form.selectResources[0].resourceName }}资源中</p>
        </div>
        <div v-else class="result">
          <p><i class="el-icon-success icon-success" /> </p>
          <p><strong>{{ form.pirParam }}</strong>在 {{ form.selectResources[0].resourceName }} 资源中</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import { pirSubmitTask } from '@/api/PIR'
import { getDataResource } from '@/api/fusionResource'

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
      whiteList: ['邢运民', '李雪娜', '李俊', '成玉伟', '张亮', '蔡滔', '罗俊伟', '熊波', '侯嘉成', '许峰', '高严', '朱宇皓', '巫家麟', '陈状元', '刘冰齐', '代宏军', '朱龙', '马宁', '包云江', '董厅', '李文光', '高若城', '黄治顺', '胡国栋', '张凤然', '周向荣', '李俊英', '王鑫灿', '李春霞', '钟丽萍']
    }
  },
  computed: {
    ...mapState('application', ['origin'])
  },
  async created() {
    this.getLocationOrigin()
    this.setDefaultValue()
  },
  destroyed() {
    clearInterval(this.taskTimer)
  },
  methods: {
    async setDefaultValue() {
      const data = {
        'node1': {
          resourceId: '704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e'
        },
        'node2': {
          resourceId: '794e41ba0e63-fcec3208-cd95-4660-a651-0e2387cdb035'
        },
        'node3': {
          resourceId: '704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e'
        },
        'test1': {
          resourceId: '2b598a7e3298-d21b8fff-6c1d-4de1-9597-88c0c22d066a'
        }
      }
      if (this.origin !== 'other') {
        console.log('this.origin', this.origin)
        this.resourceId = data[this.origin].resourceId
        await this.getDataResource()
      } else {
        this.resource = [{
          'resourceId': '2b598a7e3298-d21b8fff-6c1d-4de1-9597-88c0c22d066a',
          'resourceName': 'pir测试数据',
          'resourceDesc': '测试数据',
          'resourceRowsCount': 30,
          'resourceColumnCount': 2,
          'resourceContainsY': null,
          'resourceYRowsCount': null,
          'resourceYRatio': null
        }]
      }
      this.form.selectResources = this.resource
    },
    next() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.pirParam.indexOf('，') !== -1 || this.form.pirParam.indexOf('；') !== -1 || this.form.pirParam.indexOf(';') !== -1) {
            this.$message.error('多条件查询请使用英文,分隔')
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
          if (this.origin !== 'other') {
            pirSubmitTask({
              resourceId: this.resource[0].resourceId,
              pirParam: this.form.pirParam
            }).then(res => {
              if (res.code === 0) {
                this.taskId = res.result.taskId
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
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    handleClose() {
      this.dialogVisible = false
    },
    async getDataResource() {
      const res = await getDataResource({
        resourceId: this.resourceId
      })
      if (res.code === 0) {
        this.resource = [res.result]
      }
    },
    ...mapActions('application', ['getLocationOrigin'])
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

