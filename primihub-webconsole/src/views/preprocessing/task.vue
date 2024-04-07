<script>
import { mapGetters } from 'vuex'
import { saveExamTaskRequest } from '@/api/preprocessing'
import { getAvailableOrganList, getLocalOrganInfo } from '@/api/center'
import { getResourceList } from '@/api/fusionResource'
import ResourceTableSingleSelect from '@/components/ResourceTableSingleSelect'
import Pagination from '@/components/Pagination'
import ResourceItemSimple from '@/components/ResourceItemSimple'

export default {
  components: {
    ResourceTableSingleSelect,
    ResourceItemSimple,
    Pagination
  },
  data() {
    return {
      resourceList: [],
      searchKeyword: '',
      active: 0,
      taskId: 0,
      taskDate: 0,
      targetOrganList: [],
      organId: '', // 自有机构id
      dialogVisible: false,
      resourceName: '请选择资源',
      selectedResource: '', // 已经选择的资源
      currentSelectResource: '', // 当前单选的资源
      form: {
        targetOrganId: '',
        resourceId: '',
        taskName: ''
      },
      total: 0,
      currentPage: 1,
      pageNo: 1,
      pageSize: 5,
      pageCount: 0,
      rules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' }
        ],
        targetOrganId: [
          { required: true, message: '请选择机构', trigger: 'change' }
        ],
        resourceId: [
          { required: true, message: '请选择资源', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    hasSearchPermission() {
      return this.buttonPermissionList.includes('PrivateSearchButton')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  async created() {
    this.organLoading = true
    await this.getLocalOrganInfoFunc()
    await this.getAvailableOrganList()
    this.organLoading = false
  },
  methods: {
    /** 删除已选资源 */
    handleDelete() {
      this.form.resourceId = ''
      this.resourceName = '请选择资源'
      this.currentSelectResource = null
      this.selectedResource = null
    },

    /** dialog: 确认选择 资源 */
    handleDialogSubmit() {
      if (this.currentSelectResource) {
        this.form.resourceId = this.currentSelectResource.resourceId // 资源id存储
        this.resourceName = this.currentSelectResource.resourceName
        this.selectedResource = this.currentSelectResource
        this.dialogVisible = false
      } else {
        this.$message({
          message: '请选择资源',
          type: 'warning'
        })
      }
    },

    /** dialog: 分页 */
    async handlePagination(data) {
      this.pageNo = data.page
      await this.getResourceList()
    },

    /** dialog: 获取资源列表 */
    async getResourceList() {
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        organId: this.organId,
        resourceName: this.searchKeyword
      }
      const { code, result } = await getResourceList(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        this.resourceList = data
      }
    },

    /** dialog：关闭弹窗 */
    handleDialogCancel() {
      this.dialogVisible = false
      this.searchKeyword = ''
      this.pageNo = 1
      if (!this.currentSelectResource) {
        this.resourceName = '请选择资源'
      }
    },

    /** dialog: 资源列表筛选 */
    searchResource() {
      this.pageNo = 1
      this.getResourceList()
    },

    /** dialog: 切换选中资源 */
    handleResourceChange(data) {
      this.currentSelectResource = data
    },

    /** dialog: 打开弹窗 */
    async openDialog() {
      await this.getResourceList()
      this.dialogVisible = true
      // this.$refs.form.validateField('organId')
    },

    /** 获取机构列表 */
    async getAvailableOrganList() {
      const { result } = await getAvailableOrganList()
      this.targetOrganList = result
      // this.targetOrganList = result.filter(item => item.globalId !== this.organId)
    },

    /** 获取自有机构 */
    async getLocalOrganInfoFunc() {
      const { result } = await getLocalOrganInfo()
      const { organId } = result.sysLocalOrganInfo
      this.organId = organId
    },

    /** 提交任务  */
    handelTaskSubmit() {
      if (!this.currentSelectResource) return this.$message({ message: '请选择资源', type: 'error' })

      this.$refs.form.validate(valid => {
        if (valid) {
          this.listLoading = true
          saveExamTaskRequest(this.form).then(res => {
            if (res.code === 0) {
              this.listLoading = false
              this.$router.push({ name: 'Preprocessing' })
            } else {
              this.$message({ message: res.msg, type: 'error' })
            }
          }).finally(() => {
            this.listLoading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
</script>

<template>
  <div class="container">
    <div class="steps">
      <div class="search-area">
        <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
          <div class="select-resource">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" maxlength="32" show-word-limit placeholder="请输入任务名称,限32字" />
            </el-form-item>
            <el-form-item label="选择查询资源" prop="resourceId">
              <div class="custom-input" :style="{'color': resourceName === '请选择资源' ? '#C0C4CC' : '#606266'}" @click="openDialog"><span class="resource-name">{{ resourceName }}</span><i class="el-icon-arrow-down" /></div>
            </el-form-item>
            <el-form-item v-if="selectedResource" class="resource-container" label="已选资源">
              <ResourceItemSimple :data="selectedResource" :show-close="true" class="select-item" @delete="handleDelete" />
            </el-form-item>
            <el-form-item label="选择目标机构" prop="targetOrganId">
              <el-select v-model="form.targetOrganId" style="width: 100%" placeholder="请选择目标机构" clearable>
                <el-option
                  v-for="item in targetOrganList"
                  :key="item.globalId"
                  :label="item.globalName"
                  :value="item.globalId"
                />
              </el-select>
            </el-form-item>
            <el-button v-if="hasSearchPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="handelTaskSubmit">确认提交</el-button>
          </div>
        </el-form>

        <!--  dialog -->
        <el-dialog
          title="选择资源"
          :visible.sync="dialogVisible"
          top="10px"
          class="dialog"
          width="800px"
          :before-close="handleDialogCancel"
        >
          <div class="dialog-body">
            <div class="search-input">
              <el-input
                v-model="searchKeyword"
                placeholder="请输入内容"
                class="search"
                @keyup.enter.native="searchResource"
              >
                <el-button slot="append" icon="el-icon-search" @click="searchResource" />
              </el-input>
            </div>
            <ResourceTableSingleSelect max-height="560" :data="resourceList" :show-status="false" :selected-data="currentSelectResource && currentSelectResource.resourceId" @change="handleResourceChange" />
          </div>
          <div class="dialog-footer flex align-items-center" :class="{'justify-content-between': pageCount>1,'justify-content-center': pageCount<=1}">
            <pagination v-show="pageCount>1" :limit.sync="pageSize" :page.sync="pageNo" :page-count="pageCount" :total="total" layout="total, prev, pager, next" @pagination="handlePagination" />
            <div>
              <el-button @click="handleDialogCancel">取 消</el-button>
              <el-button type="primary" @click="handleDialogSubmit">确 定</el-button>
            </div>
          </div>
        </el-dialog>
      </div>
    </div>

  </div>
</template>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.steps{
  background-color: #fff;
  padding:50px;
}
::v-deep .el-dialog__body{
  padding: 10px 20px;
}
.custom-input{
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  color: #C0C4CC;
  line-height: 40px;
  height: 40px;
  padding: 0 15px;
  cursor: pointer;
  &.disabled{
    background-color: #F5F7FA;
  }
}
.dialog-body{
  min-height: 200px;
}
.search-input{
  width: 300px;
}
.search-area {
  margin: 20px auto;
  width: 595px;
  text-align: center;
}
.query-button {
  width: 200px;
  margin: 0 auto;
}
.select-row{
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}
.dialog-con{
  text-align: left;
  .popover-container{
    position: absolute;
    top: 12px;
    right: -22px;
    font-size: 16px;
    line-height: 1;
  }
}
.resource-box{
  display: flex;
  flex-flow: wrap;
  // margin-left: auto;
  width: calc(100% - 110px);

}
.no-data{
  color: #999;
  margin: 0 auto;
  text-align: center;
}
.dialog{
  text-align: left;
}
.dialog-footer{
  width: 100%;
  height: 50px;
  margin-bottom: 30px;
}
::v-deep .el-form-item__content{
  text-align: left;
}
.pagination-container {
  display: flex;
  justify-content: center;
}
</style>
