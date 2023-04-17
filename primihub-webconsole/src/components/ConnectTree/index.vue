<template>
  <div>
    <el-tree
      ref="connectTree"
      lazy
      node-key="id"
      :data="treeData"
      :expand-on-click-node="false"
      :props="defaultProps"
      :load="loadNode"
    >
      <span slot-scope="{ node, data }" class="custom-tree-node">
        <i class="tree-icon"><img :src="data.icon" alt=""></i>
        <span> {{ data.label }} </span>
        <span v-if="data.in"><el-tag size="mini" type="success">已加入</el-tag></span>
        <span v-if="data.globalId" class="">{{ data.globalId }}</span>
        <span v-if="!data.leaf" class="buttons">
          <template v-if="data.type === 'group'">
            <el-button
              v-if="data.in"
              type="primary"
              plain
              size="medium"
              @click="exitGroup(node,data)"
            >
              退出
            </el-button>
            <el-button
              v-else
              type="primary"
              size="medium"
              plain
              @click="joinGroup(node,data)"
            >
              加入
            </el-button>
          </template>
          <template v-else>
            <el-button
              v-if="!data.registered"
              type="primary"
              size="medium"
              plain
              @click="reconnect(data)"
            >
              重新链接
            </el-button>
            <el-button
              type="danger"
              size="medium"
              plain
              @click="deleteConnect(node,data)"
            >
              删除节点
            </el-button>
            <el-button
              v-if="data.registered"
              type="success"
              size="medium"
              plain
              @click="createGroup(node,data)"
            >
              创建群组
            </el-button>
          </template>
        </span>
      </span>
    </el-tree>
    <!-- add group -->
    <el-dialog
      title="添加群组"
      :visible.sync="groupDialogVisible"
      width="600px"
      :before-close="closeGroupDialog"
    >
      <el-form ref="groupForm" :model="groupForm" :rules="rules">
        <el-form-item label="群组名称" prop="groupName">
          <el-input v-model="groupForm.groupName" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeGroupDialog">取 消</el-button>
        <el-button type="primary" @click="groupConfirmDialog">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { deleteConnection, findAllGroup, findOrganInGroup, createGroup, joinGroup, exitGroup } from '@/api/center'
const connectIcon = require('@/assets/connect-icon.svg')
const groupIcon = require('@/assets/group-icon.svg')
const organIcon = require('@/assets/organ-icon.svg')

export default {
  props: {
    fusionList: {
      type: Array,
      default: () => []
    },
    organId: {
      type: String,
      default: ''
    },
    organName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'label',
        isLeaf: 'leaf'
      },
      groupDialogVisible: false,
      groupForm: {
        groupName: ''
      },
      rules: {
        groupName: [
          { required: true, message: '请输入群组名称' }
        ]
      }
    }
  },
  watch: {
    fusionList(newValue, oldValue) {
      console.log(newValue)
      if (newValue) {
        this.getFirstLevel()
      }
    }
  },
  methods: {
    closeGroupDialog() {
      this.$refs['groupForm'].resetFields()
      this.groupForm.groupName = ''
      this.groupDialogVisible = false
    },
    groupConfirmDialog() {
      const params = {
        serverAddress: this.serverAddress,
        groupName: this.groupForm.groupName
      }
      this.$refs['groupForm'].validate(async valid => {
        if (valid) {
          const currentNode = this.$refs.connectTree.getCurrentNode()
          console.log('currentNode', currentNode)
          console.log('params', params)
          const { code, result } = await createGroup(params)
          if (code === 0) {
            if (!currentNode.children) {
              currentNode.children = []
            }
            currentNode.children.push({
              id: result.groupData.group.id,
              label: result.groupData.group.groupName || this.groupForm.groupName,
              in: true,
              type: 'group',
              icon: groupIcon
            })
            this.$message({
              type: 'success',
              message: '创建成功'
            })
            this.closeGroupDialog()
          }
        }
      })
    },
    deleteConnect(node, data) {
      this.serverAddress = data.serverAddress
      const { code } = deleteConnection({ serverAddress: this.serverAddress })
      if (code === 0) {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        const index = this.treeData.findIndex(item => item.id === node.id)
        this.treeData = this.treeData.splice(index, 1)
      }
    },
    async reconnect(data) {
      this.serverAddress = data.serverAddress
      await this.registerConnection()
    },
    async findAllGroup() {
      const { result } = await findAllGroup({ serverAddress: this.serverAddress })
      this.groupList = result.organList.groupList || []
    },
    async findOrganInGroup() {
      const params = {
        groupId: this.groupId,
        serverAddress: this.serverAddress
      }
      const { result } = await findOrganInGroup(params)
      this.organList = result.dataList.organList
    },
    async loadNode(node, resolve) {
      node.resolve = resolve
      if (node.level === 0) {
        node.resolve = resolve
        this.getFirstLevel()
        return resolve(this.treeData)
      } else if (node.level === 1) {
        this.serverAddress = node.data.serverAddress
        const data = await this.formatGroup(node)
        if (data.length > 0) {
          return resolve(data)
        } else {
          return resolve({ id: null, label: '-暂无群组', leaf: true })
        }
      } else if (node.level === 2) {
        this.groupId = node.data.id
        const data = await this.formatOrgan(node)
        if (data) {
          return resolve(data)
        } else {
          return resolve([])
        }
      }
      if (node.level > 2) {
        return resolve([])
      }
    },
    getFirstLevel() {
      try {
        this.treeData = []
        console.log('fusionList', this.fusionList)
        this.fusionList.length > 0 && this.fusionList.forEach((item, index) => {
          if (item.show) {
            this.treeData.push({
              id: index,
              label: `中心节点${index + 1}: ${item.serverAddress}`,
              serverAddress: item.serverAddress,
              registered: item.registered,
              show: item.show,
              icon: connectIcon
            })
          }
        })
      } catch (e) {
        console.log(e)
      }
    },
    async formatOrgan(node) {
      await this.findOrganInGroup()
      if (!node.data.children) {
        let children = []
        if (this.organList.length > 0) {
          children = this.organList && this.organList.map(item => {
            return {
              id: item.globalId,
              globalId: item.globalId,
              label: item.globalName,
              leaf: true,
              icon: organIcon
            }
          })
          this.$set(node.data, 'children', children)
        }
        return children
      }
    },
    async formatGroup(node) {
      if (!node.data.children && node.data.registered) {
        await this.findAllGroup()
        if (this.groupList.length > 0) {
          const children = this.groupList.map(item => {
            return {
              id: item.id,
              label: item.groupName,
              in: item.in,
              type: 'group',
              icon: groupIcon
            }
          })
          this.$set(node.data, 'children', children)
          return children
        } else {
          return [{ id: null, label: '-暂无群组', leaf: true }]
        }
      } else {
        return [{ id: null, label: '-暂无群组', leaf: true }]
      }
    },
    createGroup(node, data) {
      this.selectTreeId = data.id
      this.serverAddress = data.serverAddress
      setTimeout(() => {
        this.groupDialogVisible = true
      }, 1000)
    },
    async exitGroup(node, data) {
      console.log('node', this.$refs.connectTree.getCurrentKey())
      console.log('treeData', this.treeData)
      const { code } = await exitGroup({ serverAddress: this.serverAddress, groupId: data.id })
      if (code === 0) {
        this.$message({
          message: `已退出(${data.label})群组`,
          type: 'success'
        })
        this.$set(data, 'in', false)
        window.location.reload()
      }
    },
    async joinGroup(node, data) {
      console.log(data)
      const { code } = await joinGroup({ serverAddress: this.serverAddress, groupId: data.id })
      if (code === 0) {
        this.$set(data, 'in', true)
        console.log(' data.children', data.children)
        if (!data.children) {
          this.$set(data, 'children', [])
        }
        data.children.push({
          id: this.organId,
          globalId: this.organId,
          label: this.organName,
          icon: organIcon,
          leaf: true
        })
        this.$message({
          message: `已加入(${data.label})群组`,
          type: 'success'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-tree__empty-text{
  position: relative;
  left: 0;
}
.buttons{
  margin: 0 5px;
  .el-button{
    margin: 10px 3px;
    padding: 3px 5px;
  }
}
.tree{
  &-icon{
    color: #22b8f2;
    width: 18px;
    height: 18px;
    display: inline-block;
    vertical-align: middle;
    img{
      max-width: 100%;
    }
  }
  font-size: 16px;
}
</style>
