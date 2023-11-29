<template>
  <div class="app-container">
    <el-button v-if="hasAddPermission" type="primary" icon="el-icon-plus" @click="addRole">新增角色</el-button>
    <el-table
      v-loading="listLoading"
      :data="list"
      class="table-list"
    >
      <el-table-column align="center" label="序号" width="150" type="index" />
      <el-table-column label="角色ID" prop="roleId" width="150" />
      <el-table-column label="角色名称" prop="roleName" width="150" />
      <el-table-column align="center" label="创建时间" prop="ctime" />
      <el-table-column v-if="hasEditPermission || hasEditPermission" align="center" label="操作" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="hasEditPermission" type="text" icon="edit" @click="openEdit(scope)"><i class="el-icon-edit" type="primary" />编辑</el-button>
          <el-button v-if="hasDeletePermission" type="text" icon="magic-stick" @click="deleteRole(scope)"><i class="el-icon-delete" />删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogFormVisible"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form ref="roleForm" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="角色权限" prop="rolePermission">
          <el-tree
            ref="tree"
            v-loading="treeLoading"
            element-loading-text="数据加载中"
            element-loading-spinner="el-icon-loading"
            :data="roleData"
            show-checkbox
            check-on-click-node
            node-key="authId"
            :check-strictly="true"
            :default-expanded-keys="defaultCheckedKeys"
            :default-checked-keys="defaultCheckedKeys"
            :props="defaultProps"
            @check="handleCheck"
            @check-change="handleCheckChange"
          />
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
import { getRoles, saveOrUpdateRole, getRoleTree, deleteRole, getAuthTree } from '@/api/role'
import Pagination from '@/components/Pagination'

// const count = 0
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
      dialogTitle: '新增角色',
      form: {
        roleId: '',
        roleName: '',
        grantAuthArray: [],
        cancelAuthArray: []

      },
      rules: {
        roleName: [
          { required: true, message: '请输入角色名称', trigger: 'blur' },
          { min: 3, max: 20, message: '角色名称在3到20个字符之间', trigger: 'blur' }
        ],
        rolePermission: []
      },
      roleData: [],
      defaultProps: {
        children: 'children',
        label: 'authName'
      },
      defaultCheckedKeys: [], // 默认选中key
      pageSize: 10,
      pageCount: 0, // 页总数
      pageNum: 1, // 页数
      itemTotalCount: 0, // 总条数
      treeLoading: false
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
    this.getRoles()
  },

  methods: {
    async getRoles() {
      this.listLoading = true
      const params = {
        pageSize: this.pageSize,
        roleName: this.form.roleName,
        pageNum: this.pageNum
      }
      const res = await getRoles(params)
      if (res.code === 0) {
        const { sysRoleList, pageParam } = res.result
        this.list = sysRoleList
        this.itemTotalCount = pageParam.itemTotalCount
        this.pageCount = pageParam.pageCount
        setTimeout(() => {
          this.listLoading = false
        }, 200)
      }
    },
    async getAuthTree() {
      this.treeLoading = true
      const res = await getAuthTree()
      this.treeLoading = false
      this.roleData = this.removeHideRole(res.result.sysAuthRootList)
    },
    async getRoleTree() {
      const res = await getRoleTree({ roleId: this.form.roleId })
      if (res.code === 0) {
        const { roleAuthRootList, sysRole } = res.result
        this.roleData = this.removeHideRole(roleAuthRootList)
        this.form.roleName = sysRole.roleName
        this.getDefaultChecked(this.roleData)
      }
    },
    removeHideRole(data) {
      return data.map(item => {
        const index = item.children.findIndex(c => c.isShow === 0)
        index !== -1 && item.children.splice(index, 1)
        return item
      })
    },
    // 获取默认展示权限节点
    getDefaultChecked(data) {
      for (const key in data) {
        const item = data[key]
        console.log(item)
        if (item.isGrant) {
          this.defaultCheckedKeys.push(item.authId)
        }
        if (item.children) {
          this.getDefaultChecked(item.children)
        }
      }
    },
    clearForm() {
      this.$refs['roleForm'].resetFields()
      this.defaultCheckedKeys = []
      this.form.roleName = ''
      this.form.roleId = ''
      this.form.grantAuthArray = []
      this.form.cancelAuthArray = []
      this.roleData = []
    },
    addRole() {
      this.dialogType = 'add'
      this.dialogFormVisible = true
      this.getAuthTree()
    },
    async openEdit({ row }) {
      this.dialogTitle = '编辑角色'
      this.dialogType = 'edit'
      this.form.roleId = row.roleId
      await this.getRoleTree()
      this.dialogFormVisible = true
    },
    deleteRole({ row }) {
      this.$confirm('此操作将永久删除该角色, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const res = await deleteRole({ roleId: row.roleId })
        if (res.code === 0) {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getRoles()
        }
      }).catch((error) => {
        console.log(error)
      })
    },
    closeDialog() {
      this.clearForm()
      this.dialogFormVisible = false
    },
    enterDialog() {
      this.$refs['roleForm'].validate(async valid => {
        if (valid) {
          const res = await saveOrUpdateRole(this.form)
          if (res.code === 0) {
            const message = this.dialogType === 'add' ? '添加成功' : '更新成功'
            this.$message({
              type: 'success',
              message: message
            })
            this.form.roleName = ''
            this.dialogFormVisible = false
            this.getRoles()
            this.clearForm()
          }
        }
      })
    },
    handlePagination(data) {
      this.pageNum = data.page
      this.getRoles()
    },
    handleCheckChange(currentNode, status) {
      if (this.dialogType === 'add') return
      this.getCancelAuthArray(currentNode, status)
    },
    handleCheck(currentNode, treeStatus) {
      /**
       * @des 根据父元素的勾选或取消勾选，将所有子级处理为选择或非选中状态
       * @param { node: Object }  当前节点
       * @param { status: Boolean } （true: 处理为勾选状态 ； false: 处理非选中）
       */
      const tree = this.$refs.tree
      const setChildStatus = (node, status) => {
        tree.setChecked(node.authId, status)
        if (node.children) {
          /* 循环递归处理子节点 */
          for (let i = 0; i < node.children.length; i++) {
            setChildStatus(node.children[i], status)
          }
        }
      }
      /* 设置父节点为选中状态 */
      const setParentStatus = (nodeObj) => {
        /* 拿到tree组件中的node,使用该方法的原因是第一次传入的 node 没有 parent */
        const node = tree.getNode(nodeObj)
        if (node.parent.key) {
          tree.setChecked(node.parent, true)
          setParentStatus(node.parent)
        }
      }

      /* 判断当前点击是选中还是取消选中操作 */
      if (treeStatus.checkedKeys.includes(currentNode.authId)) {
        setParentStatus(currentNode)
        setChildStatus(currentNode, true)
      } else {
        /* 取消选中 */
        if (currentNode.children) {
          setChildStatus(currentNode, false)
        }
      }

      this.form.grantAuthArray = [...tree.getCheckedKeys()]
    },
    /**
     * @des 获取取消权限数组
     * @param { status: Boolean } （true: 处理为勾选状态 ； false: 处理非选中）
     */
    getCancelAuthArray(currentNode, status) {
      const nodes = this.getChildrenNode(currentNode)
      for (let i = 0; i < nodes.length; i++) {
        const isDefaultInclude = this.defaultCheckedKeys.includes(currentNode.authId) // 曾存储过
        const index = this.form.cancelAuthArray.indexOf(currentNode.authId) // 数组中不存在
        if (!status && isDefaultInclude && index === -1) { // 取消选择
          this.form.cancelAuthArray.push(nodes[i].authId)
        } else if (status && isDefaultInclude && index !== -1) { // 选中
          this.form.cancelAuthArray.splice(index, 1)
        }
      }
    },
    getChildrenNode(node) { // 广度遍历
      const nodes = []
      if (node != null) {
        const queue = []
        queue.unshift(node)
        while (queue.length !== 0) {
          const item = queue.shift()
          nodes.push(item)
          const children = item.children
          if (children) {
            for (let i = 0; i < children.length; i++) {
              queue.push(children[i])
            }
          }
        }
      }
      return nodes
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
