<template>
  <div class="item" :class="{'active': selected}" @click="handleResourceClick">
    <div class="header">
      <el-checkbox v-if="hasCheckBox" v-model="selected" class="checkbox" @change="handleResourceClick" />
      <div class="header-title"><span>{{ resource.resourceName }}</span></div>
      <div class="header-right">
        <div class="type"><el-tag size="mini" class="header-title-tag">{{ resource.resourceAuthType | authTypeFilter }}</el-tag></div>
        <div v-if="!hasCheckBox && (hasEditPermission || hasDeletePermission)" class="more">
          <el-popover
            v-model="operationPopVisible"
            placement="bottom"
            trigger="click"
          >
            <div class="operation">
              <div v-if="hasEditPermission" class="operation-item" @click="toResourceCreatePage">编辑资源</div>
              <div v-if="hasDeletePermission" class="operation-item" @click="handleResourceDelete">删除资源</div>
            </div>
            <div slot="reference"><i class="el-icon-more" /></div>
          </el-popover>
        </div>
      </div>
    </div>
    <div class="main">
      <div><span>是否包含Y值：</span><span>{{ resource.resourceContainsY ? '是' : '否' }}</span></div>
      <div><span>特征量：</span><span>{{ resource.resourceRowsCount || 0 }}</span></div>
      <div><span>样本量：</span><span>{{ resource.resourceColumnCount || 0 }}</span></div>
      <div><span>正例样本数量：</span><span>{{ resource.resourceYRowsCount || 0 }}</span></div>
      <div><span>正例样本比例：</span><span>{{ resource.resourceYRatio || 0 }}%</span></div>
      <div><span>创建时间：</span><span>{{ resource.createDate }}</span></div>
    </div>
    <div class="footer">
      <div class="labels">
        <el-tag
          v-for="(item,index) in resource.resourceTag"
          :key="index"
          size="mini"
        >
          {{ item }}
        </el-tag>
      </div>
      <div class="username">{{ resource.organName }}</div>
    </div>

  </div>
</template>

<script>
import { deleteResource } from '@/api/resource'
import { mapGetters } from 'vuex'

export default {
  name: 'Hamburger',
  props: {
    resource: {
      type: Object,
      default: () => {
        return {}
      }
    },
    hasCheckBox: {
      type: Boolean,
      default: false
    },
    selected: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      list: [],
      operationPopVisible: false,
      selectedIds: [],
      selectId: 0
    }
  },
  computed: {
    active() {
      return this.selectId === this.resource.resourceId
    },
    hasEditPermission() {
      return this.buttonPermissionList.includes('ResourceEdit')
    },
    hasDeletePermission() {
      return this.buttonPermissionList.includes('ResourceDelete')
    },
    ...mapGetters([
      'buttonPermissionList',
      'permissionList',
      'routes'
    ])
  },
  methods: {
    handleResourceDelete() {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.confirmDelete()
      }).catch(() => {
        this.cancelDelete()
      })
    },
    cancelDelete() {
      this.operationPopVisible = false
    },
    confirmDelete() {
      this.operationPopVisible = false
      deleteResource(this.resource.resourceId).then(res => {
        if (res.code === 0) {
          this.$message({
            message: res.msg,
            type: 'success'
          })
          this.$emit('delete', this.resource.resourceId)
        } else {
          this.$message({
            message: res.msg,
            type: 'error'
          })
        }
      })
    },
    toResourceDetailPage() {
      if (this.hasCheckBox) return
      this.$router.push({
        name: 'ResourceDetail',
        params: { id: this.resource.resourceId }
      })
    },
    toResourceCreatePage() {
      this.$router.push({
        name: 'ResourceEdit',
        params: { id: this.resource.resourceId }
      })
    },
    deleteResource(id) {
      console.log(id)
    },
    handleResourceClick(event) {
      if (event.target && event.target.className !== 'el-icon-more') {
        this.toResourceDetailPage()
      }
      if (this.hasCheckBox) {
        this.$emit('click', this.resource)
      }
    },
    toggleClick() {}
  }
}
</script>

<style lang="scss" scoped>
.item {
  width: 270px;
  box-sizing: border-box;
  padding: 10px 20px;
  border-radius: 5px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  margin: 10px 0;
  border: 1px solid #dcdfe6;
  &.active{
    border-color: #1677FF;
    color: #1677FF;
  }
  &:hover {
    box-shadow: 2px 4px 8px rgba(0,0,0,.05);
  }
}
.header {
  display: flex;
  line-height: 40px;
  overflow: hidden;
  align-items: center;
  &-title{
    max-width: 125px;
    height: 40px;
    font-size: 16px;
    font-weight: bold;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    // vertical-align: middle;

    &-tag{
      margin-left: 5px;
      max-width: 150px;
    }
  }
  &-right{
    margin-left: auto;
    div{
      display: inline-block;
      vertical-align: middle;
    }
  }
}
.checkbox{
  margin-right: 10px;
}
.main {
  line-height: 1.5;
  font-size: 12px;
}
.operation{
  &-item{
    cursor: pointer;
    padding: 2px 10px;
    &:hover{
      background-color: #F2F6FC;
    }
  }
}
.el-popover{
  padding: 0!important;
}
.more{
  z-index: 99;
}
.footer {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #e9e9e9;
  flex-shrink: 0;
  height: 40px;
  padding-top: 10px;
  padding-bottom: 5px;
  line-height: 24px;
  color: #666666;
  margin-top: 10px;
}
.labels{
  width: 70%;
  height: 100%;
  overflow: hidden;
  .el-tag{
    margin: 0 2px;
    max-width: 60px;
    overflow: hidden;
    padding: 0 4px;
    height: 17px;
    line-height: 17px;
  }
}
.username{
  text-align: right;
  font-size: 12px;
  line-height: 1.2;
}
</style>
