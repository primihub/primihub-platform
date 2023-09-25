<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="120px" class="form">
    <el-form-item
      prop="dbUrl"
      label="指定数据库资源"
    >
      <upload :show-tips="false" :single="true" @success="handleUploadSuccess" />
    </el-form-item>
    <el-form-item
      v-if="form.dbUrl !== ''"
      prop="dbTableName"
      label="数据表名称"
    >
      <el-select v-model="form.dbTableName" no-data-text="暂无数据" filterable clearable placeholder="请选择" @change="handleDbTableNameChange">
        <el-option v-for="(item,index) in tableNames" :key="index" :label="item.label" :value="item.value" />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<script>
import { healthConnection, tableDetails } from '@/api/resource'
import Upload from '@/components/Upload'

export default {
  name: 'DatabaseImport',
  components: {
    Upload
  },
  filters: {
    connectStateFilter(state) {
      const statusMap = {
        0: '未检测',
        1: '未连接',
        2: '已连接'
      }
      return statusMap[state]
    }
  },
  data() {
    return {
      validateType: '1', // 1: check connect, 2: create resource
      connectState: 0,
      form: {
        dbType: 2,
        dbUrl: '',
        dbTableName: ''
      },
      tableNames: [],
      dataList: [],
      fieldList: [],
      rules: {
        dbUrl: [{ required: true, message: '指定数据库资源' }],
        dbTableName: [{ required: true, message: '请输入数据表名称' }]
      },
      fileId: ''
    }
  },
  methods: {
    handleUploadSuccess(data) {
      this.form.dbUrl = data.fileUrl
      this.form.dbTableName = ''
      this.tableNames = ''
      this.handleDBConnect()
    },
    async handleDbTableNameChange(val) {
      this.form.dbTableName = this.tableNames.find(item => item.value === val)?.label
      try {
        const res = await tableDetails(this.form)
        if (res.code === 0) {
          this.dataList = res.result.dataList
          this.fieldList = res.result.fieldList
        }
        this.$emit('success', {
          dataSource: this.form,
          dataList: this.dataList,
          fieldList: this.fieldList
        })
      } catch (error) {
        console.log(error)
        this.connectState = 0
      }
    },
    handleDBConnect() {
      healthConnection(this.form).then(res => {
        if (res.code === 0) {
          this.tableNames = res.result.tableNames
          this.tableNames = res.result.tableNames.map((tableName, index) => {
            return {
              label: tableName,
              value: index
            }
          })
          this.form.dbName = res.result.dbSource.dbName
          this.$emit('change')
        }
      }).catch(error => {
        console.log(error)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form{
  min-width: 1000px;
  .el-select{
    width: 660px;
  }
  .el-form-item{
    margin-bottom: 22px;
    position: relative;
  }
  .el-input{
    width: 600px;
  }
  .test-connect{
    text-align: left;
    position: absolute;
    right: 0;
    top: 0;
    width: calc(100% - 640px);
  }
  .state-icon{
    width: 6px;
    height: 6px;
    border-radius: 50%;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
  }
  .state-default{
    color:#909399;
    .state-icon{
      background-color: #909399;
    }
  }
  .state-success{
    color: #67C23A;
    .state-icon{
      background-color: #67C23A;
    }
  }
  .state-running{
    color: #1677FF;
    .state-icon{
      background-color: #1677FF;
    }
  }
  .state-failed{
    color: #F56C6C;
    .state-icon{
      background-color: #F56C6C;
    }
  }
}
</style>
