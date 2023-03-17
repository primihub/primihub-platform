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
export default {
  data() {
    return {
      loading: false,
      dialogVisible: false,
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
    this.form.selectResources = [{
      'resourceId': '2b598a7e3298-8f54f7b7-a121-4ac5-bc6a-dd6b18ba1591',
      'resourceName': 'pir测试数据',
      'resourceDesc': '测试数据',
      'resourceRowsCount': 30,
      'resourceColumnCount': 2,
      'resourceContainsY': null,
      'resourceYRowsCount': null,
      'resourceYRatio': null
    }]
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
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    handleClose() {
      this.dialogVisible = false
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

