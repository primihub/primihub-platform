<template>
  <div class="app-container">
    <el-button type="primary" icon="el-icon-plus" @click="addOrgan">新增机构</el-button>
    <el-table
      v-loading="listLoading"
      :data="list"
      class="table-list"
    >
      <!-- <el-table-column align="center" label="序号" width="150" type="index" /> -->
      <el-table-column label="机构ID" prop="organId" width="150" />
      <el-table-column label="机构名称" prop="organName" />
      <el-table-column align="center" label="创建时间" prop="ctime" />
      <el-table-column v-if="hasEditPermission || hasDeletePermission" align="center" label="操作" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="hasEditPermission" type="text" icon="edit" @click="openEdit(scope)"><i class="el-icon-edit" type="primary" />编辑</el-button>
          <el-button v-if="hasDeletePermission" type="text" icon="magic-stick" @click="deleteOrganNode(scope)"><i class="el-icon-delete" />删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogFormVisible"
      width="600px"
      :before-close="closeDialog"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="机构名称" prop="organName">
          <el-input v-model="form.organName" autocomplete="off" />
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
import { getOrgans, createOrganNode, alterOrganNodeStatus, deleteOrganNode } from '@/api/organ'
import Pagination from '@/components/Pagination'

export default {
  components: {
    Pagination
  },
  data() {
    return {
      listLoading: false,
      list: null,
      dialogFormVisible: false,
      dialogType: 'add',
      dialogTitle: '新增机构',
      form: {
        organId: '',
        organName: ''
      },
      params: {
        organId: '',
        organName: ''
      },
      rules: {
        organName: [
          { required: true, message: '请输入机构名称', trigger: 'blur' },
          { max: 20, message: '机构名称需少于20字符' }
        ]
      },
      pageSize: 5,
      pageCount: 0, // 页总数
      pageNum: 1, // 页数
      itemTotalCount: 0 // 总条数
    }
  },
  computed: {
    hasAddPermission() {
      return this.buttonPermissionList.includes('RoleAdd')
    },
    hasEditPermission() {
      return this.buttonPermissionList.includes('RoleEdit')
    },
    hasDeletePermission() {
      return this.buttonPermissionList.includes('RoleDelete')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  created() {
    this.getOrgans()
  },
  methods: {
    async getOrgans() {
      this.listLoading = true
      const params = {
        organId: this.form.organId,
        organName: this.form.organName,
        pageSize: this.pageSize,
        pageNum: this.pageNum
      }
      const res = await getOrgans(params)
      if (res.code === 0) {
        const { sysOrganList, pageParam } = res.result
        this.list = sysOrganList
        this.itemTotalCount = pageParam.itemTotalCount
        this.pageCount = pageParam.pageCount
        setTimeout(() => {
          this.listLoading = false
        }, 200)
      }
    },
    clearForm() {
      this.$refs.form.resetFields()
      this.form.organName = ''
      this.form.organId = ''
      console.log('clearForm', this.form)
    },
    addOrgan() {
      this.dialogType = 'add'
      this.dialogFormVisible = true
    },
    async openEdit({ row }) {
      this.dialogTitle = '编辑机构'
      this.dialogType = 'edit'
      this.form.organId = row.organId
      this.form.organName = row.organName
      this.dialogFormVisible = true
    },
    deleteOrganNode({ row }) {
      this.$confirm('此操作将永久删除该机构, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        await deleteOrganNode({ organId: row.organId })
        this.$message({
          type: 'success',
          message: '删除成功!'
        })
        this.getOrgans()
      }).catch((error) => {
        console.log(error)
      })
    },
    closeDialog() {
      this.clearForm()
      this.dialogFormVisible = false
    },
    enterDialog() {
      const params = {
        organIndex: 1,
        organId: this.form.organId,
        organName: this.form.organName
      }
      this.$refs['form'].validate(async valid => {
        if (valid) {
          switch (this.dialogType) {
            case 'add':
              await createOrganNode(params)
              this.$message({
                type: 'success',
                message: '添加成功'
              })
              this.pageNum = 1
              this.closeDialog()
              this.getOrgans()
              break
            case 'edit':
              await alterOrganNodeStatus(params)
              this.$message({
                type: 'success',
                message: '更新成功'
              })
              this.closeDialog()
              this.getOrgans()
              break
            default:
              break
          }
        }
      })
    },
    handlePagination(data) {
      this.pageNum = data.page
      this.getOrgans()
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
