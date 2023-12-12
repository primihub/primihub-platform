<template>
  <div v-loading="loading" class="container">
    <div class="info-container">
      <el-descriptions title="节点信息" :column="1" border :label-style="{width: '120px'}">
        <template v-if="!sysLocalOrganInfo" slot="extra">
          <el-button type="primary" class="info-button" icon="el-icon-plus" @click="addOrgan">创建机构</el-button>
        </template>
        <template v-else>
          <template slot="extra">
            <el-button type="primary" @click="editOrgan">编辑节点信息</el-button>
          </template>
          <el-descriptions-item label="节点ID"><span>{{ sysLocalOrganInfo.organId }}</span></el-descriptions-item>
          <el-descriptions-item label="机构名称">{{ sysLocalOrganInfo.organName }}</el-descriptions-item>
          <el-descriptions-item label="节点网关地址">{{ sysLocalOrganInfo.gatewayAddress }}</el-descriptions-item>
          <el-descriptions-item label="节点公钥">
            {{ sysLocalOrganInfo.publicKey }}
            <el-link type="primary" :underline="false" @click="handleCopy(sysLocalOrganInfo.publicKey,$event)">
              <i class="el-icon-copy-document" />复制
            </el-link>
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </div>
    <div v-if="sysLocalOrganInfo" class="info-container">
      <div class="flex justify-content-between align-items-start">
        <h3>合作节点列表</h3>
        <el-button type="primary" @click="addPartners">添加合作节点</el-button>
      </div>
      <el-table
        border
        :data="organList"
      >
        <el-table-column
          prop="organId"
          label="节点ID"
          min-width="100"
        />
        <el-table-column
          prop="organName"
          label="机构名称"
        />
        <el-table-column
          prop="examineState"
          label="状态"
        >
          <template slot-scope="{row}">
            <div>
              <span :class="statusStyle(row.examineState, row.enable)" />
              {{ filterState(row, 1) }}
            </div>
            <p style="font-size: 12px;color: #909399;">{{ filterState(row, 2) }}</p>
          </template>
        </el-table-column>
        <el-table-column
          prop="utime"
          label="创建时间"
        />
        <el-table-column
          label="操作"
          align="center"
          min-width="120"
        >
          <template slot-scope="{row}">
            <div class="buttons">
              <!-- <el-link v-if="row.identity === 1 && row.examineState === 0" size="mini" type="primary" @click="handleAgree(row)">同意</el-link>
              <el-link v-if="row.identity === 1 && row.examineState === 0" size="mini" type="primary" @click="handleRefuse(row)">拒绝</el-link>
              <el-link v-if="row.identity === 0 && row.examineState === 2" size="mini" type="primary" @click="handleApply(row)">申请</el-link> -->
              <el-link v-if="row.examineState === 1 && row.enable === 0" size="mini" type="primary" @click="handleConnect(row)">断开连接</el-link>
              <el-link v-if="row.examineState === 1 && row.enable === 1" size="mini" type="primary" @click="reconnectApply (row)">重新连接</el-link>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
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
        <el-form-item label="节点名称" prop="organName">
          <el-input v-model="organForm.organName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="节点网关地址" prop="gatewayAddress">
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
      :close-on-click-modal="false"
      :title="organDialogTitle"
      :visible.sync="connectDialogVisible"
      width="600px"
      :before-close="closeConnectDialog"
    >
      <el-form ref="partnersForm" :model="partnersForm" :rules="partnersFormRules">
        <el-form-item label="节点网关地址" prop="gateway">
          <el-input v-model="partnersForm.gateway" />
        </el-form-item>
        <el-form-item label="节点publicKey" prop="publicKey">
          <el-input v-model="partnersForm.publicKey" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeConnectDialog">取 消</el-button>
        <el-button v-if="organInfoChanged" type="primary" @click="changeOrganInfo">修改</el-button>
        <el-button v-else type="primary" @click="connectConfirmDialog">确 定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import Pagination from '@/components/Pagination'
import clip from '@/utils/clipboard'
import { getLocalOrganInfo, changeLocalOrganInfo, changeOtherOrganInfo, joiningPartners, getOrganList, examineJoining, enableStatus } from '@/api/center'

const USER_INFO = 'userInfo'

export default {
  name: 'Center',
  components: {
    Pagination
  },
  data() {
    return {
      organInfoChanged: false,
      reconnect: false,
      pageNo: 1,
      pageSize: 10,
      pageCount: 1,
      total: 0,
      loading: false,
      applyId: '',
      connectApplyId: '',
      sysLocalOrganInfo: null,
      organDialogVisible: false,
      connectDialogVisible: false,
      dialogType: 'add',
      dialogTitle: '创建节点',
      organDialogTitle: '添加节点',
      organForm: {
        organId: '',
        organName: '',
        gatewayAddress: ''
      },
      partnersForm: {
        organId: '',
        organName: '',
        publicKey: '',
        gateway: '',
        applyId: ''
      },
      organFormRules: {
        organName: [
          { required: true, message: '请输入节点名称', trigger: 'blur' },
          { max: 20, message: '节点名称需少于20字符' }
        ],
        gatewayAddress: [
          { required: true, message: '节点网关地址', trigger: 'blur' }
        ]
      },
      partnersFormRules: {
        publicKey: [
          { required: true, message: '请输入publicKey' }
        ],
        gateway: [
          { required: true, message: '节点网关地址', trigger: 'blur' }
        ]
      },
      organList: [],
      organId: '',
      organName: '',
      organChange: this.$store.state.user.organChange
    }
  },
  async created() {
    await this.getLocalOrganInfo()
    await this.getOrganList()
  },
  methods: {
    async handlePagination(data) {
      this.pageNo = data.page
      await this.getOrganList()
    },
    filterState(row, type) {
      const { examineState, identity, enable } = row
      if (examineState === 1 && type === 1) {
        return enable === 0 ? '已连接' : '连接断开'
      }
      if (examineState === 0) {
        if (type === 1) {
          return identity === 1 ? '待审核' : '审核中'
        } else {
          return identity === 1 ? '正在申请成为关系节点，请尽快确认' : '对方正在审核中'
        }
      } else if (examineState === 2) {
        if (type === 1) {
          return identity === 0 ? '已被拒绝' : '已拒绝'
        } else {
          return identity === 0 ? '申请已被对方拒绝，您可继续发起申请添加' : '已拒绝对方申请'
        }
      }
    },
    handleCopy(text, event) {
      clip(text, event)
    },
    statusStyle(state, enable) {
      return state === 1 && enable === 1 ? 'state-error el-icon-error' : state === 0 ? 'state-default el-icon-loading' : state === 2 ? 'state-error el-icon-error' : state === 1 ? 'state-success el-icon-success' : 'state-default'
    },
    reconnectApply({ id, publicKey, organGateway, organId, organName, applyId, enable }) {
      console.log('reconnectApply ====>>', organId)
      this.partnersForm.publicKey = publicKey
      this.partnersForm.gateway = organGateway
      this.partnersForm.organId = organId
      this.partnersForm.organName = organName
      this.partnersForm.enable = enable
      this.reconnect = true
      this.applyId = id
      this.connectApplyId = applyId
      this.organDialogTitle = '重新连接节点'
      this.connectDialogVisible = true
    },
    async handleConnect({ id, enable, organId, organGateway, publicKey, applyId }) {
      const status = enable === 1 ? 0 : 1
      const res = await enableStatus({ id, status })
      if (res.code === 0) {
        const msg = status === 1 ? '已断开连接' : '连接成功'
        this.setConnectionStatus(id, status)
        this.$message.success(msg)
      } else {
        this.$message.error(res.msg)
        this.partnersForm.organId = organId
        this.partnersForm.gateway = organGateway
        this.partnersForm.publicKey = publicKey
        this.applyId = id
        this.connectApplyId = applyId
        this.organInfoChanged = true
        this.organDialogTitle = '修改节点信息'
        setTimeout(() => {
          this.connectDialogVisible = true
        }, 200)
      }
    },
    async changeOtherOrganInfo() {
      console.log('this.applyId=====>', this.connectApplyId)
      const params = {
        applyId: this.connectApplyId,
        organId: this.partnersForm.organId,
        organName: this.partnersForm.organName,
        gatewayAddress: this.partnersForm.gateway,
        publicKey: this.partnersForm.publicKey
      }
      const res = await changeOtherOrganInfo(params)
      if (res.code === 0) {
        this.$message.success('修改成功')
        this.getOrganList()
        this.closeConnectDialog()
      } else {
        this.$message.error(res.msg)
      }
    },
    setConnectionStatus(id, status) {
      const current = this.organList.find(item => item.id === id)
      current.enable = status
    },
    async enableStatus(id, status) {
      await enableStatus({ id, status })
    },
    async handleApply({ id, applyId }) {
      this.reconnect = false
      this.applyId = id
      this.connectApplyId = applyId
      const res = await examineJoining({
        id,
        applyId,
        examineState: 0
      })
      if (res.code === 0) {
        this.$message.success('已重新发起申请')
        this.getOrganList()
      }
    },
    async handleAgree({ id }) {
      this.applyId = id
      this.examineState = 1
      this.applyConfirm('确认后对方将可查询您已开放的资源，项目中需要使用的资源需要再次确认。', '您确认同意对方的申请')
    },
    handleRefuse({ id }) {
      this.applyId = id
      this.examineState = 2
      this.applyConfirm('确认后对方后续也可继续发起申请。', '您确认拒绝对方的申请')
    },
    applyConfirm(content, title) {
      this.$confirm(content, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        this.loading = true
        const res = await examineJoining({
          id: this.applyId,
          examineState: this.examineState
        })
        if (res.code === 0) {
          const msg = this.examineState === 1 ? '已同意对方申请' : '已拒绝对方申请'
          this.$message.success(msg)
          const index = this.organList.findIndex(item => item.id === this.applyId)
          this.organList[index].examineState = this.examineState
        }
        this.loading = false
      }).catch(err => {
        console.log(err)
        this.loading = false
      })
    },
    async getOrganList() {
      this.loading = true
      const res = await getOrganList({
        pageSize: this.pageSize,
        pageNo: this.pageNo
      })
      if (res.code === 0) {
        this.loading = false
        const { result } = res
        this.organList = result.data
        this.total = res.result.total || 0
        this.pageCount = res.result.totalPage
      }
    },
    async getLocalOrganInfo() {
      this.loading = true
      const { result = {}} = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      this.organId = this.sysLocalOrganInfo?.organId
      this.organName = this.sysLocalOrganInfo?.organName
      this.loading = false
    },
    addOrgan() {
      this.dialogType = 'add'
      this.organDialogVisible = true
    },
    editOrgan() {
      this.dialogType = 'edit'
      this.dialogTitle = '编辑节点信息'
      this.organForm.organId = this.sysLocalOrganInfo.organId
      this.organForm.organName = this.sysLocalOrganInfo.organName
      this.organForm.gatewayAddress = this.sysLocalOrganInfo.gatewayAddress
      this.organDialogVisible = true
    },
    closeOrganDialog() {
      this.$refs.organForm.resetFields()
      this.organForm.organName = ''
      this.organForm.organId = ''
      this.organForm.gateway = ''
      this.organDialogVisible = false
    },
    addPartners() {
      this.partnersForm.publicKey = ''
      this.partnersForm.gateway = ''
      this.reconnect = false
      this.organInfoChanged = false
      this.connectDialogVisible = true
    },
    closeConnectDialog() {
      this.$refs.partnersForm.resetFields()
      this.partnersForm.organName = ''
      this.partnersForm.organId = ''
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
          const res = await changeLocalOrganInfo(params)
          if (res.code === 0) {
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
          } else {
            this.$message.error(res.msg)
          }
        }
      })
    },
    changeOrganInfo() {
      this.$refs['partnersForm'].validate(async valid => {
        if (valid) {
          await this.changeOtherOrganInfo()
          console.log('partnersForm', this.partnersForm)
        }
      })
    },
    connectConfirmDialog() {
      this.$refs['partnersForm'].validate(async valid => {
        if (valid) {
          if (this.organInfoChanged) {
            this.changeOrganInfo()
          } else if (this.reconnect) {
            this.loading = true
            this.examineState = 0
            this.reExamineJoining()

            this.loading = false
          } else {
            this.joiningPartners(this.partnersForm.publicKey, this.partnersForm.gateway, '', this.connectApplyId)
            this.closeConnectDialog()
          }
        }
      })
    },
    async reExamineJoining() {
      const result = await examineJoining({
        id: this.applyId,
        applyId: this.connectApplyId,
        examineState: this.examineState
      })
      if (result.code === 0) {
        this.getOrganList()
        this.$message.success('已重新连接')
        this.closeConnectDialog()
      } else {
        this.organInfoChanged = true

        // this.dialogTitle = '修改节点信息'
        this.$message.error(result.msg)
      }
    },
    joiningPartners(publicKey, gateway, id, applyId) {
      this.loading = true
      const params = {
        publicKey,
        gateway,
        id,
        applyId
      }
      joiningPartners(params).then(res => {
        if (res.code === 0) {
          this.$message.success('添加成功')
          this.getOrganList()
        } else {
          this.$message.error(res.msg)
        }
        this.loading = false
      })
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
.buttons{
  display: flex;
  justify-content: center;
  a{
    margin: 0 5px;
  }
}
.state-default,.state-processing,.status-error,.status-end{
  margin-right: 3px;
  font-size: 12px;
}
.state-default{
  color: #909399;
}
.state-success{
  color: #67C23A;
}
.state-primary{
  color: $mainColor;
}
.state-error{
  color: #F56C6C;
}
</style>
