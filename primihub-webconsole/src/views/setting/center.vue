<template>
  <div class="container">
    <h2>中心管理</h2>
    <div class="info-container">
      <el-descriptions title="机构信息" :column="1" border>
        <template v-if="!sysLocalOrganInfo" slot="extra">
          <el-button type="primary" class="info-button" icon="el-icon-plus" @click="addOrgan">创建机构</el-button>
        </template>
        <template v-else>
          <template slot="extra">
            <el-button type="primary" size="small" @click="editOrgan">编辑机构信息</el-button>
          </template>
          <el-descriptions-item label="机构ID"><span>{{ sysLocalOrganInfo.organId }}</span></el-descriptions-item>
          <el-descriptions-item label="机构名称">{{ sysLocalOrganInfo.organName }}</el-descriptions-item>
          <el-descriptions-item label="机构网关地址">{{ sysLocalOrganInfo.gatewayAddress }}</el-descriptions-item>
        </template>
      </el-descriptions>
    </div>
    <div v-if="sysLocalOrganInfo" class="info-container">
      <el-descriptions title="中心节点列表" :column="1" :colon="false">
        <template slot="extra">
          <el-button type="primary" size="small" @click="addConnect">添加中心节点</el-button>
        </template>
        <el-descriptions-item>
          <div v-if="sysLocalOrganInfo.fusionList" class="tree">
            <ConnectTree :fusion-list="sysLocalOrganInfo.fusionList" :organ-id="organId" :organ-name="organName" />
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <!-- create organ dialog -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="organDialogVisible"
      :close-on-click-modal="false"
      width="600px"
      :before-close="closeOrganDialog"
    >
      <el-form ref="organForm" :model="organForm" :rules="organFormRules">
        <el-form-item label="机构名称" prop="organName">
          <el-input v-model="organForm.organName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="机构网关地址" prop="gatewayAddress">
          <el-input v-model="organForm.gatewayAddress" autocomplete="off" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeOrganDialog">取 消</el-button>
        <el-button type="primary" @click="organConfirmDialog">确 定</el-button>
      </span>
    </el-dialog>
    <!-- add center fusion connect -->
    <el-dialog
      title="添加中心节点链接"
      :visible.sync="connectDialogVisible"
      width="600px"
      :before-close="closeConnectDialog"
    >
      <el-form ref="connectForm" :model="connectForm" :rules="connectFormRules">
        <el-form-item label="中心节点链接" prop="serverAddress">
          <el-input v-model="connectForm.serverAddress" placeholder="http://你的节点链接" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeConnectDialog">取 消</el-button>
        <el-button type="primary" @click="connectConfirmDialog">确 定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import { getLocalOrganInfo, changeLocalOrganInfo, healthConnection, registerConnection } from '@/api/center'
import ConnectTree from '@/components/ConnectTree'

const connectIcon = require('@/assets/connect-icon.svg')
const USER_INFO = 'userInfo'

export default {
  name: 'Center',
  components: {
    ConnectTree
  },
  data() {
    return {
      sysLocalOrganInfo: null,
      organDialogVisible: false,
      connectDialogVisible: false,

      dialogType: 'add',
      dialogTitle: '创建机构',
      organForm: {
        organId: '',
        organName: '',
        gatewayAddress: ''
      },
      serverAddress: '',
      connectForm: {
        serverAddress: ''
      },
      organFormRules: {
        organName: [
          { required: true, message: '请输入机构名称', trigger: 'blur' },
          { max: 20, message: '机构名称需少于20字符' }
        ],
        gatewayAddress: [
          { required: true, message: '机构网关地址', trigger: 'blur' }
        ]
      },
      connectFormRules: {
        serverAddress: [
          { required: true, message: '请输入中心节点链接', trigger: 'blur' }
        ]
      },
      treeData: [],
      selectTreeId: -1,
      groupList: [],
      organList: [],
      organId: '',
      organName: '',
      organChange: this.$store.state.user.organChange
    }
  },
  async created() {
    await this.getLocalOrganInfo()
  },
  methods: {
    async getLocalOrganInfo() {
      const { result = {}} = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      this.organId = this.sysLocalOrganInfo?.organId
      this.organName = this.sysLocalOrganInfo?.organName
      console.log('sysLocalOrganInfo', this.sysLocalOrganInfo)
    },
    addOrgan() {
      this.dialogType = 'add'
      this.organDialogVisible = true
    },
    editOrgan() {
      this.dialogType = 'edit'
      this.dialogTitle = '编辑机构信息'
      this.organForm.organId = this.sysLocalOrganInfo.organId
      this.organForm.organName = this.sysLocalOrganInfo.organName
      this.organForm.gatewayAddress = this.sysLocalOrganInfo.gatewayAddress
      this.organDialogVisible = true
    },
    closeOrganDialog() {
      this.$refs.organForm.resetFields()
      this.organForm.organName = ''
      this.organForm.organId = ''
      this.organForm.gatewayAddress = ''
      this.organDialogVisible = false
    },
    addConnect() {
      this.connectDialogVisible = true
    },
    closeConnectDialog() {
      this.$refs.connectForm.resetFields()
      this.connectForm.organName = ''
      this.connectForm.organId = ''
      this.connectDialogVisible = false
    },
    organConfirmDialog() {
      const params = {
        organId: this.organForm.organId,
        organName: this.organForm.organName,
        gatewayAddress: this.organForm.gatewayAddress
      }
      this.$refs['organForm'].validate(async valid => {
        if (valid) {
          await changeLocalOrganInfo(params)
          const message = this.dialogType === 'edit' ? '更新成功' : '添加成功'
          this.$message({
            type: 'success',
            message
          })
          this.closeOrganDialog()
          await this.getLocalOrganInfo()
          const userInfo = JSON.parse(localStorage.getItem(USER_INFO))
          userInfo.organIdList = this.organId
          userInfo.organIdListDesc = this.organName
          localStorage.setItem(USER_INFO, JSON.stringify(userInfo))
          this.$store.commit('user/SET_ORGAN_CHANGE', true)
        }
      })
    },
    connectConfirmDialog() {
      this.$refs['connectForm'].validate(async valid => {
        if (valid) {
          this.serverAddress = encodeURI(this.connectForm.serverAddress.trim())
          await this.registerConnection()
          await this.getLocalOrganInfo()
          this.closeConnectDialog()
        }
      })
    },
    async registerConnection() {
      const { code } = await healthConnection({ serverAddress: this.serverAddress })
      if (code === 0) {
        const { result } = await registerConnection({ serverAddress: this.serverAddress })
        if (result.isRegistered) {
          const index = this.treeData.length
          this.treeData.push({
            id: index,
            label: `中心节点${index + 1}: ${this.connectForm.serverAddress}`,
            serverAddress: this.connectForm.serverAddress,
            registered: result.isRegistered,
            show: result.show || true,
            icon: connectIcon
          })
          this.$message({
            type: 'success',
            message: result.fusionMsg
          })
        }
        return true
      } else {
        this.$message({
          type: 'error',
          message: '连接失败'
        })
        return false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
::v-deep .el-tree-node__content{
  height: 35px;
}
.info-container{
  border-radius: $sectionBorderRadius;
  background: #fff;
  padding: 30px;
  margin-bottom: 30px;
}
</style>
