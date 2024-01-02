<template>
  <div class="container">
    <div class="resource">
      <div>
        <div class="resource-info flex margin-bottom-5">
          <div class="flex">
            <p class="margin-right-10 ">资源ID: {{ $route.params.id }}</p>
            <p>资源名称: {{ resourceName }}</p>
          </div>
        </div>
      </div>
      <el-button v-if="resourceAuthType !== 1 && activeName === '1'" class="add-button" icon="el-icon-circle-plus-outline" type="primary" @click="addAuthorization">添加授权</el-button>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="资源使用情况" name="0">
          <!-- <div class="resource-info flex margin-bottom-5">
            <div class="flex">
              <p class="margin-right-10 ">资源ID: {{ $route.params.id }}</p>
              <p>资源名称: {{ resourceName }}</p>
            </div>

          </div> -->
          <el-table
            :data="resourceUseList"
            empty-text="暂无数据"
            border
          >
            <el-table-column
              label="序号"
              type="index"
              width="50"
              align="center"
            />
            <el-table-column
              prop="taskName"
              label="使用任务"
            />
            <el-table-column
              prop="taskId"
              label="任务ID"
            />
            <el-table-column
              prop="dataProjectName"
              label="所属项目"
            >
              <template slot-scope="{row}">
                <el-link type="primary" @click="toProjectPage(row)">{{ row.dataProjectName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column
              prop="usageTime"
              label="使用时间"
            />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="资源授权情况" name="1">
          <el-tabs v-model="childActiveName" @tab-click="handleChildClick">
            <el-tab-pane label="授权给机构" name="1" />
            <el-tab-pane label="授权给用户" name="2" />
          </el-tabs>
          <el-table
            :data="resourceList"
            empty-text="暂无数据"
            border
          >
            <el-table-column
              label="序号"
              type="index"
              width="50"
              align="center"
            />
            <el-table-column
              v-if="childActiveName === '2'"
              prop="userName"
              label="被授权人"
            />
            <el-table-column
              prop="organName"
              :label="childActiveName === '2' ? '被授权人所在机构' : '被授权机构'"
            />
            <el-table-column
              prop="applyTime"
              label="申请授权时间"
            />
            <!-- 0申请中，1审核通过，2审核拒绝 -->
            <el-table-column
              prop="auditStatus"
              label="授权状态"
            >
              <template slot-scope="{row}">
                {{ row.auditStatus === 1 ? '已授权' : row.auditStatus === 2 ? '已拒绝' : '申请待确认' }}
              </template>
            </el-table-column>
            <el-table-column
              prop="assignTime"
              label="授权时间"
            />
            <el-table-column
              label="操作"
              fixed="right"
              width="160"
              align="center"
            >
              <template slot-scope="{row}">
                <el-button v-if="row.auditStatus === 1 || row.auditStatus === 0" type="text" @click="changeResourceStatus(2, row)">取消授权</el-button>
                <el-button v-if="row.auditStatus === 0" type="text" @click="changeResourceStatus(1, row)">确认授权</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    <el-dialog
      title="选择授权对象 / 选择机构"
      :visible.sync="dialogVisible"
      width="700px"
    >
      <template v-if="stepActive === 1">
        <div class="flex">
          <el-transfer
            ref="organTransfer"
            v-model="organValue"
            v-loading="organLoading"
            class="transfer-container"
            filterable
            :left-default-checked="organLeftDefaultChecked"
            :right-default-checked="organRightDefaultChecked"
            :titles="['已建立连接机构', '已选']"
            :format="{
              noChecked: '${total}',
              hasChecked: '${checked}/${total}'
            }"
            :data="organList"
            @change="handleChange($event, 'organ')"
          >
            <span slot-scope="{ option }">{{ option.label }}</span>
          </el-transfer>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="closeAuthDialog">取消</el-button>
          <el-button type="primary" @click="handleStep">下一步</el-button>
        </span>
      </template>
      <template v-else>
        <AuthorizationUserTransfer :value="userValue" @change="handleChange($event, 'user')" />
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="handleStep">上一步</el-button>
          <el-button @click="handleAuthConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getAvailableOrganList } from '@/api/center'
import { getDataResourceUsage, getDataResourceAssignmentDetail, changeDataResourceAuthStatus, saveDataResourceAssignment } from '@/api/resource'
import Pagination from '@/components/Pagination'
import AuthorizationUserTransfer from '@/components/authorizationUserTransfer'

export default {
  components: { Pagination, AuthorizationUserTransfer },
  data() {
    return {
      organList: [],
      stepActive: 1,
      organLeftDefaultChecked: [],
      organRightDefaultChecked: [],
      organLoading: false,
      organValue: [],
      userValue: [],
      resourceId: 0,
      resourceFusionId: '',
      dialogVisible: false,
      loading: false,
      activeName: '0',
      childActiveName: '1',
      resourceList: [],
      currentPage: 1,
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 5,
      resourceName: '',
      resourceAuthType: '',
      resourceUseList: []
    }
  },
  computed: {
    ...mapGetters([
      'buttonPermissionList',
      'isOrganAdmin'
    ])
  },
  async created() {
    console.log(this.$route.query)
    this.resourceId = this.$route.params.id || ''
    this.resourceFusionId = this.$route.query.id || this.resourceId || ''
    this.resourceName = decodeURIComponent(this.$route.query.resourceName)
    this.resourceAuthType = this.$route.query.resourceAuthType
    console.log('resourceId', this.resourceId)
    await this.getDataResourceUsage()
  },
  methods: {
    toProjectPage({ dataProjectId }) {
      this.$router.push({
        name: 'ProjectDetail',
        params: { id: dataProjectId || 90 }
      })
    },
    handleChange(value, name) {
      if (name === 'organ') {
        this.organRightDefaultChecked = value
      } else {
        this.userRightDefaultChecked = value
      }
    },
    async getAvailableOrganList() {
      this.organLoading = true
      const { result } = await getAvailableOrganList()
      this.organList = result.map(item => {
        return {
          key: item.globalId,
          label: item.globalName
        }
      })
      this.organLoading = false
      console.log('organList', this.organList)
    },
    handleStep() {
      this.stepActive = this.stepActive === 1 ? 2 : 1
      console.log('this.userValue', this.userValue)
      this.$nextTick(() => {
        this.$refs.organTransfer && this.$refs.organTransfer.clearQuery('left')
        this.$refs.organTransfer && this.$refs.organTransfer.clearQuery('right')
        this.$refs.userTransfer && this.$refs.userTransfer.clearQuery('left')
        this.$refs.userTransfer && this.$refs.userTransfer.clearQuery('right')
      })
    },
    handleClick() {
      console.log(this.activeName)
      if (this.activeName === '1') {
        this.getAuthList()
      } else {
        this.getDataResourceUsage()
      }
      this.pageNo = 1
      this.total = 0
      this.pageCount = 1
    },
    handleChildClick() {
      console.log(this.childActiveName)
      this.getAuthList()
      this.pageNo = 1
      this.total = 0
      this.pageCount = 1
    },
    closeAuthDialog() {
      this.dialogVisible = false
    },
    handleAuthConfirm() {
      this.stepActive = 1
      this.dialogVisible = false
      let params = {
        resourceId: this.resourceId,
        resourceFusionId: this.resourceFusionId
      }
      if (this.userRightDefaultChecked.length > 0) {
        this.userValue = this.userRightDefaultChecked
        const userAssignList = this.userValue.map(item => {
          return {
            userId: item
          }
        })
        params = Object.assign(params, { userAssignList })
      }
      if (this.organValue.length > 0) {
        const fusionOrganList = []
        this.organValue.forEach(item => {
          const current = this.organList.find(organ => organ.key === item)
          console.log(current, item)
          fusionOrganList.push({
            organGlobalId: current.key,
            organName: current.label
          })
        })
        params = Object.assign(params, { fusionOrganList })
      }
      saveDataResourceAssignment(params).then(res => {
        if (res.code === 0) {
          this.userValue = []
          this.$message.success('授权成功')
          this.getAuthList()
        } else {
          this.$message.error('授权失败')
        }
      })
      console.log('organValue', this.organValue)
      console.log('userValue', this.userValue)
    },
    addAuthorization() {
      this.dialogVisible = true
      this.getAvailableOrganList()
    },
    changeResourceStatus(auditStatus, { organGlobalId, userId }) {
      let params = {
        auditStatus,
        resourceId: this.resourceId,
        queryType: this.isOrganAdmin ? '1' : '2'
      }
      if (organGlobalId) {
        params = Object.assign(params, {
          organId: organGlobalId
        })
      }

      if (userId) {
        params = Object.assign(params, {
          userId: userId
        })
      }

      changeDataResourceAuthStatus(params)
    },
    async getAuthList() {
      this.loading = true
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceFusionId: this.resourceFusionId,
        queryType: this.childActiveName
      }
      try {
        const res = await getDataResourceAssignmentDetail(params)
        if (res.code === 0) {
          const { data, total, totalPage } = res.result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceList = data
          }
          this.isReset = false
        }
        this.loading = false
      } catch (e) {
        this.loading = false
      }
    },
    async getDataResourceUsage() {
      this.loading = true
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceId: this.resourceId
      }
      try {
        const res = await getDataResourceUsage(params)
        if (res.code === 0) {
          const { data, total, totalPage } = res.result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceUseList = data
          }
          this.isReset = false
        }
        this.loading = false
      } catch (e) {
        this.loading = false
      }
    },
    handlePagination(data) {
      this.pageNo = data.page
      if (this.activeName === '1') {
        this.getAuthList()
      } else {
        this.getDataResourceUsage()
      }
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/resource.scss";
.resource{
  position: relative;
  padding-top: 30px;
  .resource-info{
    padding: 10px 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .add-button{
    right: 40px;
    top: 30px;
    z-index: 10;
  }
  .resource-info{
    margin-bottom: 10px;
  }
}
</style>
