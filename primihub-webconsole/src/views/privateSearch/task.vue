<template>
  <div class="container">
    <div class="steps">
      <!--      <el-steps :active="active" finish-status="success" align-center>-->
      <!--        <el-step title="查询条件" />-->
      <!--        <el-step title="查询结果" />-->
      <!--      </el-steps>-->
      <div v-if="active === 0" class="search-area">
        <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
          <div class="select-resource">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" maxlength="32" show-word-limit placeholder="请输入任务名称,限32字" />
            </el-form-item>
            <div class="dialog-con">
              <el-form-item label="选择查询资源" prop="resourceName">
                <el-row type="flex" :gutter="10">
                  <el-col :span="12">
                    <el-form-item prop="organId">
                      <el-select v-model="form.organId" style="width: 100%" placeholder="请选择机构" clearable @change="handleOrganChange">
                        <el-option
                          v-for="item in organList"
                          :key="item.globalId"
                          :label="item.globalName"
                          :value="item.globalId"
                        />
                      </el-select>
                    </el-form-item>

                  </el-col>
                  <el-col :span="12">
                    <el-form-item>
                      <div class="custom-input" :style="{'color': resourceName === '请选择机构下资源' ? '#C0C4CC' : '#606266'}" :class="{'disabled': form.organId === '' }" @click="openDialog"><span class="resource-name">{{ resourceName }}</span><i class="el-icon-arrow-down" /></div>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item v-if="form.selectResources" class="resource-container" label="已选资源" prop="selectResources">
                <ResourceItemSimple :data="selectResources" :show-close="true" class="select-item" @delete="handleDelete" />
              </el-form-item>
            </div>
            <el-form-item label="关键词" prop="pirParam">
              <tags-input  :selected-data="tagValueList" ></tags-input>
            </el-form-item>
            <el-form-item>
              <p :style="{color: '#999', lineHeight: 1}">基于关键词的精准查询，输入关键词后Enter即可。</p>
            </el-form-item>
            <el-button v-if="hasSearchPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
          </div>
        </el-form>
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
                @change="handleSearchNameChange"
                @keyup.enter.native="searchResource"
              >
                <el-button slot="append" icon="el-icon-search" @click="searchResource" />
              </el-input>
            </div>
            <ResourceTableSingleSelect max-height="560" :data="resourceList" :show-status="false" :selected-data="selectResources && selectResources.resourceId" @change="handleResourceChange" />
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

<script>
import { mapGetters } from 'vuex'
import { pirSubmitTask } from '@/api/PIR'
import { getAvailableOrganList } from '@/api/center'
import { getResourceList } from '@/api/fusionResource'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ResourceTableSingleSelect from '@/components/ResourceTableSingleSelect'
import Pagination from '@/components/Pagination'
import tagsInput from '@/components/TagsInput/index.vue'

export default {
  components: {
    ResourceItemSimple,
    ResourceTableSingleSelect,
    Pagination,
    tagsInput
  },
  data() {
    return {
      resourceList: [],
      searchKeyword: '',
      active: 0,
      pirParam: 0,
      taskId: 0,
      taskDate: 0,
      organList: [],
      dialogVisible: false,
      resourceName: '请选择机构下资源',
      form: {
        organId: '',
        resourceName: '',
        pirParam: '',
        selectResources: null,
        taskName: ''
      },
      selectResources: null, // selected resource id list
      total: 0,
      currentPage: 1,
      pageNo: 1,
      pageSize: 5,
      pageCount: 0,
      rules: {
        taskName:[
          { required: true, message: '请输入任务名称', trigger: 'blur' }
        ],
        organId: [
          { required: true, message: '请选择机构', trigger: 'change' }
        ],
        resourceName: [
          { required: true, message: '请选择机构下资源', trigger: 'blur' }
        ],
        pirParam: [
          { required: true, message: '请输入关键词', trigger: 'blur' },
          { max: 50, message: '长度在50个字符以内' }
        ]
      },
      tagValueList: []
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
  watch: {
    tagValueList(val) {
      if (val.length > 0) {
        this.form.pirParam = val.join(',')
      } else {
        this.form.pirParam = ''
      }
    }
  },
  async created() {
    await this.getAvailableOrganList()
  },
  methods: {
    handleDelete() {
      this.form.resourceId = ''
      this.form.resourceName = ''
      this.resourceName = ''
      this.selectResources = null
      this.form.selectResources = null
    },
    handleDialogSubmit() {
      if (this.selectResources) {
        this.form.resourceName = this.selectResources.resourceName
        this.resourceName = this.selectResources.resourceName
        this.form.selectResources = this.selectResources
        this.dialogVisible = false
      } else {
        this.$message({
          message: '请选择资源',
          type: 'warning'
        })
      }
    },
    async handlePagination(data) {
      this.pageNo = data.page
      await this.getResourceList()
    },
    async getResourceList() {
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        organId: this.form.organId,
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
    handleSearchNameChange(searchName) {
      console.log(searchName)
      this.form.resourceName = searchName
    },
    handleDialogCancel() {
      this.dialogVisible = false
      this.searchKeyword = ''
      this.pageNo = 1
      if (!this.selectResources) {
        this.form.resourceName = ''
      }
    },
    searchResource() {
      this.pageNo = 1
      this.getResourceList()
    },
    handleResourceChange(data) {
      console.log('handleResourceChange', data)
      this.selectResources = data
    },
    async openDialog() {
      if (this.form.organId) {
        await this.getResourceList()
        this.dialogVisible = true
      } else {
        this.$refs.form.validateField('organId')
      }
    },
    async getAvailableOrganList() {
      this.organLoading = true
      const { result } = await getAvailableOrganList()
      this.organList = result
      this.organLoading = false
    },
    handleOrganChange(value) {
      this.organName = this.organList.find(item => item.globalId === value)?.globalName
      this.form.resourceName = ''
      this.form.selectResources = null
    },
    tagChange(data) {
      console.log('当前的数据==', data)
    },
    next() {
      console.log('next', this.selectResources)
      if (!this.selectResources) {
        this.$message({
          message: '请选择资源',
          type: 'error'
        })
        return
      }
      this.dialogVisible = false

      this.$refs.form.validate(valid => {
        if (valid) {
          this.listLoading = true
          if (this.form.pirParam.indexOf('，') !== -1 || this.form.pirParam.indexOf('；') !== -1 || this.form.pirParam.indexOf(';') !== -1) {
            this.$message.error('多条件查询请使用英文,分隔')
            return
          }
          pirSubmitTask({
            resourceId: this.selectResources.resourceId,
            pirParam: this.form.pirParam.replace(/(\s|,)+$/g, '')
          }).then(res => {
            if (res.code === 0) {
              this.listLoading = false
              this.taskId = res.result.taskId
              this.$emit('next', this.taskId)
              this.toTaskListPage()
            } else {
              this.$message({
                message: res.msg,
                type: 'error'
              })
              this.listLoading = false
            }
          }).catch(err => {
            console.log(err)
            this.listLoading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    toTaskListPage() {
      this.$router.push({
        name: 'PrivateSearchList'
      })
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.steps{
  background-color: #fff;
  padding:50px;
}
::v-deep .el-dialog__body{
  padding: 10px 20px;
}
::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px!important;
}
::v-deep .el-table td.el-table__cell div{
  line-height: 1.5;
}
::v-deep .table.el-table .el-table__cell{
  padding: 5px 0;
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
