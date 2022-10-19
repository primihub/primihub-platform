<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="form">
    <el-form-item
      prop="dbType"
      label="数据库类型"
    >
      <el-select v-model="form.dbType" placeholder="请选择" @change="handleDbTypeChange">
        <el-option v-for="item in dbTypeMap" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </el-form-item>
    <el-form-item
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
      <p style="color:#999;">示例：jdbc:mysql://xxxxxxxxxx</p>
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
      <el-input v-model="form.dbPassword" placeholder="请输入密码" />
      <div class="test-connect">
        <span class="margin-right-10" :class="statusStyle(connectState)"><i class="state-icon" />{{ connectState | connectStateFilter }}</span>
        <el-button type="primary" size="small" @click="handleDBConnect">检测连接</el-button>
      </div>
    </el-form-item>
    <el-form-item
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
      <el-select v-model="form.dbTableName" no-data-text="暂无数据" filterable clearable placeholder="请选择" @focus="handleDbTableNameFocus" @change="handleDbTableNameChange">
        <el-option v-for="(item,index) in tableNames" :key="index" :label="item.label" :value="item.value" />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<script>
import { healthConnection, tableDetails } from '@/api/resource'

const fieldList = [{
  'fieldId': null,
  'fieldName': 'id',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'dict_name',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'dict_code',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'description',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'del_flag',
  'fieldAs': null,
  'fieldType': 'integer',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'create_by',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'create_time',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'update_by',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'update_time',
  'fieldAs': null,
  'fieldType': 'string',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}, {
  'fieldId': null,
  'fieldName': 'type',
  'fieldAs': null,
  'fieldType': 'integer',
  'fieldDesc': null,
  'relevance': false,
  'grouping': false,
  'protectionStatus': false,
  'createDate': null
}]

const dataList = [{
  'id': '0b5d19e1fce4b2e6647e6b4a17760c14',
  'dict_name': '通告类型',
  'dict_code': 'msg_category',
  'description': '消息类型1:通知公告2:系统消息',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-22T18:01:35',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1174509082208395266',
  'dict_name': '职务职级',
  'dict_code': 'position_rank',
  'description': '职务表职级字典',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-09-19T10:22:41',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1174511106530525185',
  'dict_name': '机构类型',
  'dict_code': 'org_category',
  'description': '机构类型 1公司，2部门 3岗位',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-09-19T10:30:43',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1178295274528845826',
  'dict_name': '表单权限策略',
  'dict_code': 'form_perms_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-09-29T21:07:39',
  'update_by': 'admin',
  'update_time': '2019-09-29T21:08:26',
  'type': null
}, {
  'id': '1199517671259906049',
  'dict_name': '紧急程度',
  'dict_code': 'urgent_level',
  'description': '日程计划紧急程度',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-11-27T10:37:53',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1199518099888414722',
  'dict_name': '日程计划类型',
  'dict_code': 'eoa_plan_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-11-27T10:39:36',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1199520177767587841',
  'dict_name': '分类栏目类型',
  'dict_code': 'eoa_cms_menu_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-11-27T10:47:51',
  'update_by': 'admin',
  'update_time': '2019-11-27T10:49:35',
  'type': 0
}, {
  'id': '1199525215290306561',
  'dict_name': '日程计划状态',
  'dict_code': 'eoa_plan_status',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-11-27T11:07:52',
  'update_by': 'admin',
  'update_time': '2019-11-27T11:10:11',
  'type': 0
}, {
  'id': '1209733563293962241',
  'dict_name': '数据库类型',
  'dict_code': 'database_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-12-25T15:12:12',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1232913193820581889',
  'dict_name': 'Online表单业务分类',
  'dict_code': 'ol_form_biz_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2020-02-27T14:19:46',
  'update_by': 'admin',
  'update_time': '2020-02-27T14:20:23',
  'type': 0
}, {
  'id': '1250687930947620866',
  'dict_name': '定时任务状态',
  'dict_code': 'quartz_status',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2020-04-16T15:30:14',
  'update_by': '',
  'update_time': null,
  'type': null
}, {
  'id': '1280401766745718786',
  'dict_name': '租户状态',
  'dict_code': 'tenant_status',
  'description': '租户状态',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2020-07-07T15:22:25',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '1356445645198135298',
  'dict_name': '开关',
  'dict_code': 'is_open',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2021-02-02T11:33:38',
  'update_by': 'admin',
  'update_time': '2021-02-02T15:28:12',
  'type': 0
}, {
  'id': '236e8a4baff0db8c62c00dd95632834f',
  'dict_name': '同步工作流引擎',
  'dict_code': 'activiti_sync',
  'description': '同步工作流引擎',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-05-15T15:27:33',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '2e02df51611a4b9632828ab7e5338f00',
  'dict_name': '权限策略',
  'dict_code': 'perms_type',
  'description': '权限策略',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-26T18:26:55',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '2f0320997ade5dd147c90130f7218c3e',
  'dict_name': '推送类别',
  'dict_code': 'msg_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-17T21:21:32',
  'update_by': 'admin',
  'update_time': '2019-03-26T19:57:45',
  'type': 0
}, {
  'id': '3486f32803bb953e7155dab3513dc68b',
  'dict_name': '删除状态',
  'dict_code': 'del_flag',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-01-18T21:46:26',
  'update_by': 'admin',
  'update_time': '2019-03-30T11:17:11',
  'type': 0
}, {
  'id': '3d9a351be3436fbefb1307d4cfb49bf2',
  'dict_name': '性别',
  'dict_code': 'sex',
  'description': null,
  'del_flag': 0,
  'create_by': null,
  'create_time': '2019-01-04T14:56:32',
  'update_by': 'admin',
  'update_time': '2019-03-30T11:28:27',
  'type': 1
}, {
  'id': '4274efc2292239b6f000b153f50823ff',
  'dict_name': '全局权限策略',
  'dict_code': 'global_perms_type',
  'description': '全局权限策略',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-05-10T17:54:05',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '4c03fca6bf1f0299c381213961566349',
  'dict_name': 'Online图表展示模板',
  'dict_code': 'online_graph_display_template',
  'description': 'Online图表展示模板',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-12T17:28:50',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '4c753b5293304e7a445fd2741b46529d',
  'dict_name': '字典状态',
  'dict_code': 'dict_item_status',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2020-06-18T23:18:42',
  'update_by': 'admin',
  'update_time': '2019-03-30T19:33:52',
  'type': 1
}, {
  'id': '4d7fec1a7799a436d26d02325eff295e',
  'dict_name': '优先级',
  'dict_code': 'priority',
  'description': '优先级',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-16T17:03:34',
  'update_by': 'admin',
  'update_time': '2019-04-16T17:39:23',
  'type': 0
}, {
  'id': '4e4602b3e3686f0911384e188dc7efb4',
  'dict_name': '条件规则',
  'dict_code': 'rule_conditions',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-01T10:15:03',
  'update_by': 'admin',
  'update_time': '2019-04-01T10:30:47',
  'type': 0
}, {
  'id': '4f69be5f507accea8d5df5f11346181a',
  'dict_name': '发送消息类型',
  'dict_code': 'msgType',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-11T14:27:09',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '68168534ff5065a152bfab275c2136f8',
  'dict_name': '有效无效状态',
  'dict_code': 'valid_status',
  'description': '有效无效状态',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2020-09-26T19:21:14',
  'update_by': 'admin',
  'update_time': '2019-04-26T19:21:23',
  'type': 0
}, {
  'id': '6b78e3f59faec1a4750acff08030a79b',
  'dict_name': '用户类型',
  'dict_code': 'user_type',
  'description': null,
  'del_flag': 0,
  'create_by': null,
  'create_time': '2019-01-04T14:59:01',
  'update_by': 'admin',
  'update_time': '2019-03-18T23:28:18',
  'type': 0
}, {
  'id': '72cce0989df68887546746d8f09811aa',
  'dict_name': 'Online表单类型',
  'dict_code': 'cgform_table_type',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-01-27T10:13:02',
  'update_by': 'admin',
  'update_time': '2019-03-30T11:37:36',
  'type': 0
}, {
  'id': '78bda155fe380b1b3f175f1e88c284c6',
  'dict_name': '流程状态',
  'dict_code': 'bpm_status',
  'description': '流程状态',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-05-09T16:31:52',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '83bfb33147013cc81640d5fd9eda030c',
  'dict_name': '日志类型',
  'dict_code': 'log_type',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-18T23:22:19',
  'update_by': null,
  'update_time': null,
  'type': 1
}, {
  'id': '845da5006c97754728bf48b6a10f79cc',
  'dict_name': '状态',
  'dict_code': 'status',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-18T21:45:25',
  'update_by': 'admin',
  'update_time': '2019-03-18T21:58:25',
  'type': 0
}, {
  'id': '880a895c98afeca9d9ac39f29e67c13e',
  'dict_name': '操作类型',
  'dict_code': 'operate_type',
  'description': '操作类型',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-07-22T10:54:29',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': '8dfe32e2d29ea9430a988b3b558bf233',
  'dict_name': '发布状态',
  'dict_code': 'send_status',
  'description': '发布状态',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-16T17:40:42',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': 'a7adbcd86c37f7dbc9b66945c82ef9e6',
  'dict_name': '1是0否',
  'dict_code': 'yn',
  'description': '',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-05-22T19:29:29',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': 'a9d9942bd0eccb6e89de92d130ec4c4a',
  'dict_name': '消息发送状态',
  'dict_code': 'msgSendStatus',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-12T18:18:17',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': 'ac2f7c0c5c5775fcea7e2387bcb22f01',
  'dict_name': '菜单类型',
  'dict_code': 'menu_type',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2020-12-18T23:24:32',
  'update_by': 'admin',
  'update_time': '2019-04-01T15:27:06',
  'type': 1
}, {
  'id': 'ad7c65ba97c20a6805d5dcdf13cdaf36',
  'dict_name': 'onlineT类型',
  'dict_code': 'ceshi_online',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-22T16:31:49',
  'update_by': 'admin',
  'update_time': '2019-03-22T16:34:16',
  'type': 0
}, {
  'id': 'bd1b8bc28e65d6feefefb6f3c79f42fd',
  'dict_name': 'Online图表数据类型',
  'dict_code': 'online_graph_data_type',
  'description': 'Online图表数据类型',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-12T17:24:24',
  'update_by': 'admin',
  'update_time': '2019-04-12T17:24:57',
  'type': 0
}, {
  'id': 'c36169beb12de8a71c8683ee7c28a503',
  'dict_name': '部门状态',
  'dict_code': 'depart_status',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-18T21:59:51',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': 'c5a14c75172783d72cbee6ee7f5df5d1',
  'dict_name': 'Online图表类型',
  'dict_code': 'online_graph_type',
  'description': 'Online图表类型',
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-04-12T17:04:06',
  'update_by': null,
  'update_time': null,
  'type': 0
}, {
  'id': 'd6e1152968b02d69ff358c75b48a6ee1',
  'dict_name': '流程类型',
  'dict_code': 'bpm_process_type',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2021-02-22T19:26:54',
  'update_by': 'admin',
  'update_time': '2019-03-30T18:14:44',
  'type': 0
}, {
  'id': 'fc6cd58fde2e8481db10d3a1e68ce70c',
  'dict_name': '用户状态',
  'dict_code': 'user_status',
  'description': null,
  'del_flag': 0,
  'create_by': 'admin',
  'create_time': '2019-03-18T21:57:25',
  'update_by': 'admin',
  'update_time': '2019-03-18T23:11:58',
  'type': 1
}]

export default {
  name: 'DatabaseImport',
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
        dbType: '',
        dbDriver: '',
        dbUrl: '',
        dbUsername: '',
        dbPassword: '',
        dbName: '',
        dbTableName: '',
        dataList: [],
        fieldList: []
      },
      dbTypeMap: [
        {
          label: 'MYSQL5.5',
          value: '1'
        },
        {
          label: 'MYSQL5.7+',
          value: '2'
        }
      ],
      dbDriverMap: {
        // MySQL 数据库
        '1': { dbDriver: 'com.mysql.jdbc.Driver' },
        // MySQL5.7+ 数据库
        '2': { dbDriver: 'com.mysql.cj.jdbc.Driver' }
      },
      dbUrlMap: {
        // MySQL 数据库
        '1': { dbUrl: 'jdbc:mysql://127.0.0.1:3306/primihub?characterEncoding=UTF-8&useUnicode=true&useSSL=false' },
        // MySQL5.7+ 数据库
        '2': { dbUrl: 'jdbc:mysql://127.0.0.1:3306/primihub?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' }
      },
      tableNames: [],
      dataList: [],
      fieldList: [],
      rules: {
        dbType: [{ required: true, message: '请选择数据库类型' }],
        dbDriver: [{ required: true, message: '请输入驱动类' }],
        dbUrl: [{ required: true, message: '请输入数据源地址' }],
        dbUsername: [{ required: true, message: '请输入用户名' }],
        dbPassword: [{ required: true, message: '请输入密码' }],
        dbName: [{ required: true, message: '请输入数据库名称' }],
        dbTableName: [{ required: true, message: '请输入数据表名称' }]
      }
    }
  },
  methods: {
    statusStyle(state) {
      return state === 0 ? 'state-default' : state === 1 ? 'state-failed' : state === 2 ? 'state-success' : 'state-default'
    },
    handleDbTypeChange(val) {
      const dbDriver = this.dbDriverMap[val]
      const dbUrl = this.dbUrlMap[val]
      this.form.dbDriver = dbDriver ? dbDriver.dbDriver : ''
      this.form.dbUrl = dbUrl ? dbUrl.dbUrl : ''
      console.log(this.form)
    },
    handleDbTableNameFocus() {
      console.log(this.tableNames)
      if (this.tableNames.length < 1) {
        this.$message({
          message: '请先连接数据库',
          type: 'error'
        })
        return
      }
    },
    async handleDbTableNameChange(val) {
      console.log(val)
      try {
        const res = await tableDetails(this.params)
        if (res.code === 0) {
          this.form.dataList = res.result.dataList
          this.form.fieldList = res.result.fieldList
        }
        this.$emit('data', this.form)
      } catch (error) {
        console.log(error)
        this.fieldList = fieldList
        this.dataList = dataList
        this.$emit('data', this.form)
      }
    },
    handleDBConnect() {
      const tableNames = ['tmp_report_data_income', 'tmp_report_data_1', 'test_shoptype_tree', 'test_person', 'test_order_product', 'test_order_main', 'test_online_link', 'test_note', 'test_enhance_select']
      this.tableNames = tableNames.map((tableName, index) => {
        return {
          label: tableName,
          value: index
        }
      })
      this.$refs.form.validate((valid) => {
        if (valid) {
          console.log('检测连接')
          healthConnection(this.form).then(res => {
            if (res.code === 0) {
              this.connectState = 2
              this.tableNames = tableNames.map((tableName, index) => {
                return {
                  label: tableName,
                  value: index
                }
              })
              console.log(this.tableNames)
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
    color: #409EFF;
    .state-icon{
      background-color: #409EFF;
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
