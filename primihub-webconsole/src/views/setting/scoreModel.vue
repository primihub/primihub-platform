<template>
  <div class="app-container">
    <el-button v-if="hasAddPermission" type="primary" icon="el-icon-plus" @click="handelAdd">新增模型分</el-button>
    <el-table
      v-loading="listLoading"
      :data="scoreModelList"
      class="table-list"
    >
      <el-table-column align="center" label="序号" width="100" type="index" />
      <el-table-column label="模型分名称" prop="scoreModelName" />
      <el-table-column label="模型分类型" prop="scoreModelType" />
      <el-table-column label="模型分代码" prop="scoreModelCode" />
      <el-table-column label="模型分参数名" prop="scoreKey" />
    </el-table>

    <el-dialog
      title="新增模型分"
      :visible.sync="dialogFormVisible"
      width="610px"
      :before-close="closeDialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="模型分名称" prop="scoreModelName">
          <el-input v-model="form.scoreModelName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="模型分类型" prop="scoreModelType">
          <el-input v-model="form.scoreModelType" autocomplete="off" />
        </el-form-item>
        <el-form-item label="模型分代码" prop="scoreModelCode">
          <el-input v-model="form.scoreModelCode" autocomplete="off" />
        </el-form-item>
        <el-form-item label="模型分参数名" prop="scoreKey">
          <el-input v-model="form.scoreKey" autocomplete="off" />
        </el-form-item>
        <el-form-item label="选择机构" prop="organId">
          <el-select v-model="form.organId" style="width: 100%" placeholder="请选择机构" clearable>
            <el-option
              v-for="item in targetOrganList"
              :key="item.globalId"
              :label="item.globalName"
              :value="item.globalId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">取 消</el-button>
        <el-button type="primary" @click="enterDialog">确 定</el-button>
      </span>
    </el-dialog>
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page.sync="pageNum" :total="itemTotalCount" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getScoreModelListRequest, submitScoreModelRequest } from '@/api/scoreModel'
import { getAvailableOrganList } from '@/api/center'
import Pagination from '@/components/Pagination'

// const count = 0
export default {
  components: {
    Pagination
  },
  data() {
    return {
      listLoading: false,
      dialogFormVisible: false,
      form: {
        scoreModelName: '',
        scoreModelType: '',
        scoreModelCode: '',
        scoreKey: '',
        organId: ''
      },
      rules: {
        scoreModelName: [
          { required: true, message: '请输入模型分名称', trigger: 'blur' },
        ],
        scoreModelType: [
          { required: true, message: '请输入模型分类型', trigger: 'blur' },
        ],
        scoreModelCode: [
          { required: true, message: '请输入模型分代码', trigger: 'blur' },
        ],
        scoreKey: [
          { required: true, message: '请输入模型分参数名', trigger: 'blur' },
        ],
        organId: [
          { required: true, message: '请选择机构', trigger: 'change' },
        ],
      },
      pageSize: 10,
      pageCount: 0, // 页总数
      pageNum: 1, // 页数
      itemTotalCount: 0, // 总条数
      scoreModelList: [],
      targetOrganList: []
    }
  },
  computed: {
    hasAddPermission() {
      return this.buttonPermissionList.includes('RoleAdd')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  created() {
    this.getAvailableOrganList()
    this.fetchData()
  },

  methods: {
    /** 获取机构列表 */
    async getAvailableOrganList() {
      const { result } = await getAvailableOrganList()
      this.targetOrganList = result
    },

    async fetchData() {
      const { result } = await getScoreModelListRequest()
      this.scoreModelList = result
    },

    handelAdd() {
      this.dialogFormVisible = true
    },

    closeDialog() {
      this.clearForm()
      this.dialogFormVisible = false
    },

    clearForm() {
      this.$refs.formRef.resetFields();
    },

    enterDialog() {
      this.$refs.formRef.validate(async valid => {
        if (valid) {
          const res = await submitScoreModelRequest({...this.form})
          if (res.code === 0) {
            this.$message.success('新增成功！')
            this.dialogFormVisible = false
            this.fetchData()
            this.clearForm()
          }
        }
      })
    },

    handlePagination(data) {
      this.pageNum = data.page
      this.getRoles()
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-table th{
  background: #fafafa;
}
.table-list{
  margin-top: 30px;
}
.dialog-footer{
  text-align: center;
  justify-content: center;
}
</style>
