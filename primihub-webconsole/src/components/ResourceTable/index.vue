<template>
  <el-table
    ref="table"
    class="table"
    :row-key="rowKey"
    border
    :data="data"
    v-bind="$attrs"
    highlight-current-row
    min-height="300"
    @selection-change="handleSelectionChange"
  >
    <template slot="empty">
      <p>{{ emptyText }}</p>
    </template>
    <el-table-column
      v-if="multiple"
      :reserve-selection="true"
      :selectable="checkSelectable"
      type="selection"
      width="40"
    />
    <el-table-column
      label="资源ID"
      prop="resourceId"
    />
    <el-table-column
      label="资源名称"
      min-width="120"
      prop="resourceName"
    />
    <el-table-column
      label="资源信息"
      min-width="120"
    >
      <template slot-scope="{row}">
        特征量：{{ row.resourceColumnCount }}<br>
        样本量：{{ row.resourceRowsCount }} <br>
        正例样本数量：{{ row.resourceYRowsCount || 0 }}<br>
        正例样本比例：{{ row.resourceYRatio || 0 }}%<br>
        <div class="margin-top-5">
          <el-tag v-if="row.resourceContainsY" type="primary" size="mini">包含Y值</el-tag>
          <el-tag v-else type="danger" size="mini">不包含Y值</el-tag>
        </div>
      </template>
    </el-table-column>
    <el-table-column
      v-if="hasResourceDesc"
      prop="resourceDesc"
      label="资源描述"
    />
    <el-table-column
      v-if="showStatus"
      prop="auditStatus"
      label="审核状态"
      align="center"
    >
      <template slot-scope="{row}">
        <span :class="statusStyle(row.auditStatus)">{{ row.auditStatus | resourceAuditStatusFilter }}</span>
      </template>
    </el-table-column>

    <el-table-column
      v-if="showDeleteButton || showPreviewButton"
      label="操作"
      align="center"
      min-width="120"
    >
      <template slot-scope="{row}">
        <template v-if="thisInstitution && projectAuditStatus && row.auditStatus === 0">
          <el-button :disabled="status === 2" size="mini" type="primary" @click="handleAgree(row)">同意</el-button>
          <el-button :disabled="status === 2" size="mini" type="danger" @click="handleRefused(row)">拒绝</el-button>
        </template>
        <el-button v-if="showPreviewButton" :disabled="status === 2" size="mini" type="primary" plain @click="handlePreview(row)">查看</el-button>
        <el-button v-if="showDeleteButton" :disabled="status === 2" size="mini" type="danger" plain @click="handleRemove(row)">移除</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'ResourceTable',
  props: {
    data: {
      type: Array,
      default: () => []
    },
    rowKey: {
      type: String,
      default: 'resourceId',
      require: true
    },
    selectedData: {
      type: Array,
      default: () => []
    },
    multiple: {
      type: Boolean,
      default: false
    },
    projectAuditStatus: {
      type: Boolean,
      default: false
    },
    showStatus: {
      type: Boolean,
      default: true
    },
    showPreviewButton: {
      type: Boolean,
      default: true
    },
    showDeleteButton: {
      type: Boolean,
      default: true
    },
    thisInstitution: {
      type: Boolean,
      default: true
    },
    creator: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      emptyText: '暂无资源'
    }
  },
  computed: {
    hasResourceDesc() {
      return this.data[0] && Object.keys(this.data[0]).includes('resourceDesc')
    },
    ...mapState('project', ['status'])
  },
  watch: {
    selectedData(val) {
      if (val) {
        this.toggleSelection(this.selectedData)
      }
    }
  },
  created() {
    console.log(this.showDeleteButton)
    console.log(this.showPreviewButton)
  },
  mounted() {
    this.toggleSelection(this.selectedData)
  },
  methods: {
    statusStyle(status) {
      return status === 0 ? 'status-0 el-icon-refresh' : status === 1 ? 'status-1 el-icon-circle-check' : status === 2 ? 'status-2 el-icon-circle-close' : ''
    },
    toggleSelection(rows) {
      this.$refs.table.clearSelection()
      this.$nextTick(() => {
        if (rows) {
          rows.forEach(row => {
            this.$refs.table.toggleRowSelection(row)
          })
        } else {
          this.$refs.table.clearSelection()
        }
      })
    },
    handleSelectionChange(value) {
      if (!this.multiple) return
      this.multipleSelection = value
      this.$emit('change', this.multipleSelection)
    },
    handlePreview(row) {
      this.$emit('preview', row)
    },
    handleAgree(row) {
      this.$emit('handleAgree', row)
    },
    handleRefused(row) {
      this.$emit('handleRefused', row)
    },
    handleRemove(row) {
      this.$confirm('删除后将不能使用此数据集, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('remove', row)
      }).catch(() => {})
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: { id }
      })
    },
    checkSelectable(row) {
      const res = !(this.selectedData.length > 0 && this.selectedData.filter(item => item[this.rowKey] === row[this.rowKey]).length > 0)
      return res
    },
    searchResource() {
      this.pageNo = 1
      this.$emit('search', this.resourceName)
      this.fetchData()
    },
    handleSearchNameChange(searchName) {
      this.resourceName = searchName
    },
    showButton(row) {
      if (row.auditStatus === 0) {
        return row.auditStatus === 0
      }
      return true
    }
  }

}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.table{
  margin: 15px 0;
}
::v-deep .el-button{
  margin: 2px 5px;
}
.status-0{
  color: $mainColor;
}
.status-1{
  color: #67C23A;
}
.status-2{
  color: #F56C6C;
}

</style>

