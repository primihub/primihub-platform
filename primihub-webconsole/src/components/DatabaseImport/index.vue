<template>
  <div>
    <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="form">
      <el-form-item
        prop="dbType"
        label="数据库类型"
      >
        <el-select v-model="form.dbType" placeholder="请选择" @change="handleDbTypeChange">
          <el-option v-for="item in dbTypeMap" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <template v-if="form.dbType === 2">
        <SQLiteImport @success="handleImportSuccess" @change="handleImportChange" />

      </template>
      <template v-else-if="form.dbType !== ''">
        <el-form-item
          v-if="form.dbType === 0 || form.dbType === 1 "
          prop="dbDriver"
          label="驱动类"
        >
          <el-input v-model="form.dbDriver" placeholder="请输入驱动类" />
        </el-form-item>
        <el-form-item
          prop="dbUrl"
          label="数据源地址"
        >
          <el-input v-model="form.dbUrl" placeholder="请输入数据源地址" />
          <p style="color:#999;">示例：{{ exampleDBUrl }}</p>
        </el-form-item>
        <el-form-item
          prop="dbUsername"
          label="用户名"
        >
          <el-input v-model="form.dbUsername" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item
          prop="dbPassword"
          label="密码"
        >
          <el-input v-model="form.dbPassword" type="password" placeholder="请输入密码" />
          <div class="test-connect">
            <span class="margin-right-10" :class="statusStyle(connectState)"><i class="state-icon" />{{ connectState | connectStateFilter }}</span>
            <el-button type="primary" size="small" @click="handleDBConnect">检测连接</el-button>
          </div>
        </el-form-item>
        <el-form-item
          v-if="connectState === 2"
          prop="dbName"
          label="数据库名称"
        >
          <el-input v-model="form.dbName" placeholder="请输入数据库名称" />
        </el-form-item>
        <el-form-item
          v-if="connectState === 2"
          prop="dbTableName"
          label="数据表名称"
        >
          <el-select v-model="form.dbTableName" no-data-text="暂无数据" filterable placeholder="请选择" @change="handleDbTableNameChange">
            <el-option v-for="(item,index) in tableNames" :key="index" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </template>

    </el-form>

  </div>

</template>

<script>
import { healthConnection, tableDetails } from '@/api/resource'
import SQLiteImport from '@/components/SQLiteImport'

export default {
  name: 'DatabaseImport',
  components: {
    SQLiteImport
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
      exampleDBUrl: '',
      form: {
        dbType: '',
        dbDriver: '',
        dbUrl: '',
        dbUsername: '',
        dbPassword: '',
        dbName: '',
        dbTableName: ''
      },
      params: {},
      dbTypeMap: [
        {
          label: 'MYSQL5.5',
          value: 0
        },
        {
          label: 'MYSQL5.7+',
          value: 1
        }
        // {
        //   label: 'SQLite',
        //   value: 2
        // }
      ],
      dbDriverMap: {
        // MySQL 数据库
        0: { dbDriver: 'com.mysql.jdbc.Driver' },
        // MySQL5.7+ 数据库
        1: { dbDriver: 'com.mysql.cj.jdbc.Driver' }
      },
      dbUrlMap: {
        // MySQL 数据库
        0: { dbUrl: 'jdbc:mysql://ip:port/dbName?characterEncoding=UTF-8&useUnicode=true&useSSL=false' },
        // MySQL5.7+ 数据库
        1: { dbUrl: 'jdbc:mysql://ip:port/dbName?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' },
        3: { dbUrl: 'jdbc:hive2://ip:port/default' }
      },
      tableNames: [],
      dataList: [],
      fieldList: [],
      rules: {},
      rules1: {
        dbType: [{ required: true, message: '请选择数据库类型' }],
        dbDriver: [{ required: true, message: '请输入驱动类' }],
        dbUrl: [{ required: true, message: '请输入数据源地址', trigger: 'blur' }],
        dbName: [{ required: true, message: '请输入数据库名称' }],
        dbTableName: [{ required: true, message: '请输入数据表名称' }],
        dbUsername: [{ required: true, message: '请输入用户名' }],
        dbPassword: [{ required: true, message: '请输入密码' }]
      },
      rules2: {
        dbType: [{ required: true, message: '请选择数据库类型' }],
        dbDriver: [{ required: true, message: '请输入驱动类' }],
        dbUrl: [{ required: true, message: '请输入数据源地址', trigger: 'blur' }],
        dbName: [{ required: true, message: '请输入数据库名称' }],
        dbTableName: [{ required: true, message: '请输入数据表名称' }]
      }
    }
  },
  methods: {
    handleImportSuccess(data) {
      this.fieldList = data.fieldList
      this.dataList = data.dataList
      this.$emit('success', {
        dataSource: data.dataSource,
        dataList: data.dataList,
        fieldList: data.fieldList
      })
    },
    handleImportChange() {
      this.fieldList = []
      this.dataList = []
    },
    statusStyle(state) {
      return state === 0 ? 'state-default' : state === 1 ? 'state-failed' : state === 2 ? 'state-success' : 'state-default'
    },
    reset() {
      this.fieldList = []
      this.dataList = []
      this.form.dbName = ''
      this.form.dbTableName = ''
      this.form.dbUsername = ''
      this.form.dbPassword = ''
      this.connectState = 0
    },
    handleDbTypeChange(val) {
      const dbDriver = this.dbDriverMap[val]
      const dbUrl = this.dbUrlMap[val]?.dbUrl
      this.exampleDBUrl = dbUrl
      this.form.dbUrl = dbUrl || ''
      this.form.dbDriver = dbDriver ? dbDriver.dbDriver : ''

      this.reset()

      if (this.form.dbType === 0 || this.form.dbType === 1) {
        this.rules = this.rules1
      } else {
        this.rules = this.rules2
      }

      this.$emit('change', {
        dataSource: this.form,
        dataList: this.dataList,
        fieldList: this.fieldList
      })
    },
    async handleDbTableNameChange(val) {
      this.params = Object.assign({}, this.params, {
        dbTableName: this.tableNames.find(item => item.value === val)?.label,
        dbName: this.form.dbName
      })
      try {
        const res = await tableDetails(this.params)
        if (res.code === 0) {
          this.dataList = res.result.dataList
          this.fieldList = res.result.fieldList
        }
        this.$emit('success', {
          dataSource: this.params,
          dataList: this.dataList,
          fieldList: this.fieldList
        })
      } catch (error) {
        console.log(error)
        this.connectState = 0
      }
    },
    handleDBConnect() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          console.log('检测连接')
          this.params = JSON.parse(JSON.stringify(this.form))
          this.params['dbType'] = this.form.dbType === 0 ? 1 : this.form.dbType
          healthConnection(this.params).then(res => {
            if (res.code === 0) {
              this.tableNames = res.result.tableNames
              this.connectState = 2
              this.tableNames = res.result.tableNames.map((tableName, index) => {
                return {
                  label: tableName,
                  value: index
                }
              })
              this.form.dbName = res.result.dbSource.dbName
            } else {
              this.connectState = 1
            }
          }).catch(error => {
            this.connectState = 2
            console.log(error)
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form{
  min-width: 1000px;
  .el-select{
    width: 600px;
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
