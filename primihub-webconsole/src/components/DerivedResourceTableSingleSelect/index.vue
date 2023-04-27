<template>
  <el-table
    ref="table"
    v-loading="loading"
    class="table"
    border
    :data="derivedDataResourceList"
    v-bind="$attrs"
    highlight-current-row
    empty-text="暂无数据"
  >
    <el-table-column label="选择" width="55">
      <template slot-scope="{row}">
        <el-radio v-model="radioSelect" :label="row.resourceId" @change="handleRadioChange(row)"><i /></el-radio>
      </template>
    </el-table-column>
    <el-table-column
      prop="resourceName"
      label="资源名称"
      min-width="100"
    >
      <template slot-scope="{row}">
        {{ row.resourceName }}
        <!-- <el-link size="mini" type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceName }}</el-link> -->
      </template>
    </el-table-column>
    <el-table-column
      prop="resourceId"
      label="资源ID"
    >
      <template slot-scope="{row}">
        {{ row.resourceId }}
      </template>
    </el-table-column>
    <el-table-column
      label="资源信息"
      min-width="150"
    >
      <template slot-scope="{row}">
        <div class="info">
          特征量：{{ row.fileColumns }}<br>
          样本量：{{ row.fileRows }} <br>
          正例样本数量：{{ row.fileYRows || 0 }}<br>
          正例样本比例：{{ row.fileYRatio || 0 }}% <br>
          <el-tag size="mini" :type="!row.fileContainsY? 'danger': 'primary'">{{ row.fileContainsY? '包含Y值' : '不包含Y值' }}</el-tag>
        </div>
      </template>
    </el-table-column>
    <el-table-column
      prop="tag"
      label="衍生数据来源"
      align="center"
    />
    <el-table-column
      prop="createDate"
      label="创建时间"
      min-width="110"
    >
      <template slot-scope="{row}">
        {{ row.createDate.split(' ')[0] }} <br>
        {{ row.createDate.split(' ')[1] }}
      </template>
    </el-table-column>
  </el-table>

</template>

<script>
import { getDerivationResourceList } from '@/api/project'

export default {
  name: 'ResourceTable',
  props: {
    data: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      currentRow: null,
      radioSelect: null,
      derivedDataResourceList: [],
      projectId: '',
      loading: false
    }
  },
  watch: {
    selectedData(newVal) {
      this.setCurrent(newVal)
    }
  },
  async mounted() {
    await this.getDerivationResourceList()
    if (this.selectedData) {
      this.setCurrent(this.selectedData)
    }
  },
  methods: {
    async getDerivationResourceList() {
      this.loading = true
      const params = {
        projectId: this.$route.params.id || ''
      }
      const { code, result } = await getDerivationResourceList(params)
      if (code === 0) {
        if (result.length > 0) {
          const { organId, participationIdentity } = JSON.parse(sessionStorage.getItem('organ'))
          result.map(item => {
            if (item.organId === organId) {
              item.participationIdentity = participationIdentity
              item.resourceId = item.resourceId.toString()
              item.calculationField = item.calculationField ? item.calculationField : item.fileHandleField ? item.fileHandleField[0] : ''
              this.derivedDataResourceList.push(item)
            }
          })
        }
        this.loading = false
      }
      this.loading = false
    },
    handleRadioChange(row) {
      this.currentRow = row
      this.currentRow.type = 'derivedResource'
      this.setCurrent(row.resourceId)
      this.$emit('change', this.currentRow)
    },
    setCurrent(resourceId) {
      this.radioSelect = resourceId || ''
    },
    toModelTaskDetail(row) {
      this.$router.push({
        path: `/project/detail/${this.projectId}/task/${row.taskId}`
      })
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'DerivedDataResourceDetail',
        params: { id }
      })
    }
  }

}
</script>
<style lang="scss" scoped>
.table{
  margin: 15px 0;
  &.el-table{
    font-size: 13px;
  }
}
::v-deep .el-button{
  margin: 2px 5px;
}

</style>

