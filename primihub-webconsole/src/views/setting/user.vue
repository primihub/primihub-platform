<template>
  <div class="container">
    <el-button v-if="hasAddPermission" type="primary" icon="el-icon-plus" @click="addUser">新增用户</el-button>
    <div class="main">
      <el-table
        :data="list"
        class="table-list"
      >
        <el-table-column align="center" label="序号" width="100" type="index" />
        <el-table-column align="left" label="ID" prop="userId" width="100" />
        <el-table-column align="left" label="账户名" prop="userAccount" />
        <el-table-column align="left" label="昵称" prop="userName" />
        <el-table-column align="left" label="角色名称" prop="roleIdListDesc" />
        <el-table-column align="center" label="注册时间" prop="cTime" />
        <el-table-column v-if="hasEditPermission || hasDeletePermission || hasResetPermission" align="center" label="操作" fixed="right" width="250">
          <template slot-scope="scope">
            <el-button v-if="hasEditPermission" type="text" icon="edit" @click="openEdit(scope.row)"><i class="el-icon-edit" type="primary" />编辑</el-button>
            <el-button v-if="hasDeletePermission" type="text" icon="magic-stick" @click="handleDeleteUser(scope.row)"><i class="el-icon-delete" />删除</el-button>
            <el-button v-if="hasResetPermission" type="text" icon="magic-stick" @click="handleInitPassword(scope.row)"><i class="el-icon-magic-stick" />重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNum" :total="itemTotalCount" @pagination="handlePagination" />
    </div>
    <!-- 新增/编辑用户弹窗 -->
    <el-dialog :visible.sync="dialogVisible" custom-class="user-dialog" :title="dialogTitle" closable :before-close="closeDialog">
      <el-form ref="userForm" :model="userInfo" label-width="100px" :rules="rules" label-position="right">
        <el-form-item label="账户名" prop="userAccount">
          <el-input v-model="userInfo.userAccount" :disabled="dialogFlag === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称" prop="userName">
          <el-input
            v-model="userInfo.userName"
            maxlength="20"
            minlength="3"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="用户角色" prop="roleIdList">
          <el-select
            v-model="userInfo.roleIdList"
            filterable
            placeholder="请选择用户角色"
            value-key="value"
          >
            <el-option
              v-for="(item,index) in authOptions"
              :key="index"
              :label="item.roleName"
              :value="item.roleId"
              multiple
            />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="头像" label-width="80px">
          <div style="display:inline-block" @click="openHeaderChange">
            <img v-if="userInfo.avatar" class="header-img-box" :src="(userInfo.avatar && userInfo.avatar.slice(0, 4) !== 'http')?path+userInfo.avatar:userInfo.avatar">
            <div v-else class="header-img-box">
              <el-upload
                class="avatar-uploader"
                action="https://jsonplaceholder.typicode.com/posts/"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                accept="image/gif,image/jpeg,image/png,image/jpg"
              >
                <i class="el-icon-plus avatar-uploader-icon" />
              </el-upload>
            </div>
          </div>
        </el-form-item> -->
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取 消</el-button>
          <el-button type="primary" @click="enterUserDialog">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, deleteUser, saveOrUpdateUser, initPassword } from '@/api/userAdmin'
import { getRoles } from '@/api/role'
import Pagination from '@/components/Pagination'
import { mapGetters } from 'vuex'

export default {
  components: {
    Pagination
  },
  data() {
    return {
      list: [],
      dialogFlag: '',
      dialogTitle: '',
      userInfo: {
        userId: '',
        userAccount: '',
        userName: '',
        roleIdList: [],
        avatar: ''
      },
      sysOrganList: [],
      dialogVisible: false,
      authOptions: [], // 角色列表
      organOptions: [], // 机构列表
      rules: {
        userAccount: [
          { required: true, message: '请输入账户名称', trigger: 'blur' },
          { min: 3, max: 50, message: '账户名称在3到50个字符之间', trigger: 'blur' }
        ],
        userName: [
          { required: true, message: '请输入用户昵称', trigger: 'blur' }
        ],
        roleIdList: [
          { required: true, message: '请选择用户角色', trigger: 'blur' }
        ]
      },
      itemTotalCount: 0, // 总数
      pageSize: 10, // 每页条数 默认10条
      pageCount: 1, // 页总
      pageNum: 1 // 页数
    }
  },
  computed: {
    hasAddPermission() {
      return this.buttonPermissionList.includes('UserAdd')
    },
    hasEditPermission() {
      return this.buttonPermissionList.includes('UserEdit')
    },
    hasDeletePermission() {
      return this.buttonPermissionList.includes('UserDelete')
    },
    hasResetPermission() {
      return this.buttonPermissionList.includes('UserPasswordReset')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      const params = {
        pageSize: this.pageSize,
        pageNum: this.pageNum
      }
      getUserList(params).then((res) => {
        if (res.code === 0) {
          const { sysUserList, pageParam } = res.result
          this.list = sysUserList
          this.pageCount = Number(pageParam.pageCount)
          this.pageNum = Number(pageParam.pageNum)
          this.itemTotalCount = Number(pageParam.itemTotalCount)
        }
      })
    },
    async getRoles(query) {
      this.authOptions = []
      const params = {
        roleName: query,
        pageSize: 100
      }
      const res = await getRoles(params)
      if (res.code === 0) {
        const { sysRoleList } = res.result
        sysRoleList.map(item => {
          this.authOptions.push({
            roleName: item.roleName,
            roleId: item.roleId.toString()
          })
        })
      }
    },
    // handleRoleFocus() {
    //   this.getRoles()
    // },
    async searchRole(query) {
      if (query !== '') {
        await this.getRoles(query)
      }
    },
    handlePagination(data) {
      this.pageNum = data.page
      this.fetchData()
    },
    async openEdit(row) {
      this.dialogFlag = 'edit'
      this.dialogTitle = '编辑用户'
      await this.getRoles()
      this.userInfo.userId = row.userId
      this.userInfo.userName = row.userName
      this.userInfo.userAccount = row.userAccount
      this.userInfo.roleIdList = row.roleIdList
      this.userInfo.roleName = row.roleIdListDesc
      this.dialogVisible = true
    },
    async addUser() {
      this.userInfo.userId = ''
      this.dialogTitle = '新增用户'
      this.dialogVisible = true
      this.dialogFlag = 'add'
      await this.getRoles()
    },
    clearForm() {
      this.$refs['userForm'].resetFields()
      this.userInfo.userName = ''
      this.userInfo.userAccount = ''
      this.userInfo.roleIdList = []
    },
    handleInitPassword(row) {
      this.$confirm('是否将此用户密码重置为123456?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const res = await initPassword({ userId: row.userId })
        if (res.code === 0) {
          this.$message({
            message: '重置成功',
            type: 'success'
          })
        }
      })
    },
    async handleDeleteUser(row) {
      console.log('删除用户')
      this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        // TODO 删除用户接口
        const res = await deleteUser({ userId: row.userId })
        if (res.code === 0) {
          this.$message({
            type: 'success',
            message: '删除成功'
          })
          this.list.splice()
          this.fetchData()
        }
      })
    },
    openHeaderChange() {
      console.log('修改头像')
      // TODO 上传头像接口
    },
    closeDialog() {
      this.dialogVisible = false
      this.clearForm()
      console.log('关闭弹窗')
    },
    enterUserDialog() {
      this.$refs['userForm'].validate(async valid => {
        if (valid) {
          const params = {
            userId: this.userInfo.userId,
            userAccount: this.userInfo.userAccount,
            userName: this.userInfo.userName,
            roleIdList: this.userInfo.roleIdList
          }

          const res = await saveOrUpdateUser(params)
          if (res.code === 0) {
            const message = this.dialogFlag === 'add' ? '添加成功' : '更新成功'
            this.$message({
              type: 'success',
              message: message
            })
            this.closeDialog()
            this.fetchData()
          }
        }
      })
    },
    handleAvatarSuccess() {

    },
    beforeAvatarUpload() {

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
.main{
  background-color: #fff;
}
</style>
