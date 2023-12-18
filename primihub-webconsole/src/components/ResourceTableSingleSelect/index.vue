<template>
  <div>
    <el-table
      ref="table"
      class="table"
      border
      :data="data"
      v-bind="$attrs"
      highlight-current-row
      :row-class-name="tableRowClassName"
    >
      <el-table-column label="选择" width="55" align="center">
        <template slot-scope="{row}">
          <el-radio v-model="radioSelect" :disabled="row.auditStatus !== undefined && row.auditStatus !== 1" :label="row.resourceId" @change="handleRadioChange(row)"><i /></el-radio>
        </template>
      </el-table-column>
      <el-table-column
        label="资源名称 / Id"
        min-width="120"
      >
        <template slot-scope="{row}">
          {{ row.resourceName }}<br>
          {{ row.resourceId }}
        </template>
      </el-table-column>
      <el-table-column
        label="资源信息"
      >
        <template slot-scope="{row}">
          特征量：{{ row.resourceColumnCount }}<br>
          样本量：{{ row.resourceRowsCount }} <br>
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
          {{ row.auditStatus | resourceAuditStatusFilter }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="showPreviewButton"
        label="操作"
        fixed="right"
        align="center"
      >
        <template slot-scope="{row}">
          <el-button size="mini" type="primary" plain @click="handlePreview(row.resourceId)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- preview dialog -->
    <ResourcePreviewDialog
      :data="previewList"
      :visible.sync="previewDialogVisible"
      append-to-body
      width="1000px"
      @close="closeDialog"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { resourceFilePreview } from '@/api/resource'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'
export default {
  name: 'ResourceTable',
  components: {
    ResourcePreviewDialog
  },
  props: {
    data: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: String,
      default: ''
    },
    showStatus: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      currentRow: null,
      radioSelect: null,
      previewList: [],
      previewDialogVisible: false
    }
  },
  computed: {
    showPreviewButton() {
      return this.data.length > 0 && this.data[0].participationIdentity === 1 && this.userOrganId === this.data[0].organId
    },
    hasResourceDesc() {
      return this.data[0] && Object.keys(this.data[0]).includes('resourceDesc')
    },
    ...mapGetters(['userOrganId'])
  },
  watch: {
    selectedData(newVal) {
      this.setCurrent(newVal)
    }
  },
  mounted() {
    console.log(this.data)
    if (this.selectedData) {
      this.setCurrent(this.selectedData)
    }
  },
  methods: {
    async handlePreview(resourceId) {
      this.resourceId = resourceId
      await this.resourceFilePreview()
      this.previewDialogVisible = true
    },
    async resourceFilePreview() {
      const res = await resourceFilePreview({ resourceId: this.resourceId })
      this.previewList = res.result?.dataList
    },
    closeDialog() {
      this.previewDialogVisible = false
    },
    tableRowClassName({ row }) {
      if (row.auditStatus !== undefined && row.auditStatus !== 1) {
        return 'row-disabled'
      } else {
        return ''
      }
    },
    handleRadioChange(row) {
      this.currentRow = row
      this.setCurrent(row.resourceId)
      this.$emit('change', this.currentRow)
    },
    setCurrent(resourceId) {
      this.radioSelect = resourceId || ''
    }
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
::v-deep .el-table tr.row-disabled{
  color: #999;
}
</style>

