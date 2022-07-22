<template>
  <el-table
    ref="table"
    class="table"
    :row-key="rowKey"
    border
    :data="data"
    v-bind="$attrs"
    highlight-current-row
    @selection-change="handleSelectionChange"
  >
    <el-table-column
      v-if="multiple"
      :reserve-selection="true"
      :selectable="checkSelectable"
      type="selection"
      width="40"
    />
    <el-table-column
      label="资源 / Id"
      min-width="120"
    >
      <template slot-scope="{row}">
        <!-- <el-link type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceName }}</el-link><br> -->
        {{ row.resourceName }}<br>
        {{ row.resourceId }}
      </template>
    </el-table-column>
    <!-- <el-table-column
        prop="resourceTag"
        label="关键词"
      >
        <template slot-scope="{row}">
          <el-tag v-for="(tag,index) in row.resourceTag" :key="index" type="success" size="mini" class="tag">{{ tag }}</el-tag>
        </template>
      </el-table-column> -->
    <!-- <el-table-column
      prop="resourceAuthType"
      label="可见性"
      align="center"
    >
      <template slot-scope="{row}">
        {{ row.resourceAuthType | authTypeFilter }}
      </template>
    </el-table-column> -->
    <!-- <el-table-column
      prop="resourceType"
      label="资源类型"
      align="center"
    >
      <template slot-scope="{row}">
        {{ row.resourceType | sourceFilter }}
      </template>
    </el-table-column> -->
    <el-table-column
      label="资源信息"
      min-width="120"
    >
      <template slot-scope="{row}">
        特征量：{{ row.resourceRowsCount }}<br>
        样本量：{{ row.resourceColumnCount }} <br>
        正例样本数量：{{ row.resourceYRowsCount || 0 }}<br>
        正例样本比例：{{ row.resourceYRatio || 0 }}%<br>
      </template>
    </el-table-column>
    <el-table-column
      v-if="showStatus"
      prop="auditStatus"
      label="审核状态"
      align="center"
    >
      <template slot-scope="{row}">
        {{ row.auditStatus | resourceAuditStatusFilter }}
      </template>
    </el-table-column>
    <el-table-column
      label="是否包含Y值"
      min-width="80"
      align="center"
    >
      <template slot-scope="{row}">
        {{ row.resourceContainsY? '是' : '否' }}
      </template>
    </el-table-column>
    <el-table-column
      v-if="showButtons"
      label="操作"
      align="center"
      min-width="120"
    >
      <template slot-scope="{row}">
        <template v-if="thisInstitution && row.participationIdentity === 2 && projectAuditStatus && row.auditStatus === 0">
          <el-button size="mini" type="primary" @click="handleAgree(row)">同意</el-button>
          <el-button size="mini" type="danger" @click="handleRefused(row)">拒绝</el-button>
        </template>
        <el-button v-if="thisInstitution" size="mini" type="primary" plain @click="handlePreview(row)">预览</el-button>
        <el-button v-if="creator" size="mini" type="danger" plain @click="handleRemove(row)">移除</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import { mapActions } from 'vuex'

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
    showButtons: {
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
    },
    serverAddress: {
      type: String,
      default: '',
      require: true
    }
  },
  data() {
    return {
    }
  },
  watch: {
    selectedData(val) {
      if (val) {
        this.toggleSelection(this.selectedData)
      }
    }
  },
  mounted() {
    this.toggleSelection(this.selectedData)
  },
  methods: {
    toggleSelection(rows) {
      console.log(this.selectedData)
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
        params: { id },
        query: {
          serverAddress: this.serverAddress
        }
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
    },
    ...mapActions('user', ['getInfo'])
  }

}
</script>
<style lang="scss" scoped>
.table{
  margin: 15px 0;
}
::v-deep .el-button{
  margin: 2px 5px;
}

</style>

