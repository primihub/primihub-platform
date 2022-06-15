<template>
  <div class="app-container">
    <div v-loading="listLoading">
      <p>[ {{ resourceName }} ] 详情</p>

      <div class="detail">
        <el-descriptions title="" :column="1" label-class-name="detail-title">
          <el-descriptions-item label="表结构模板">{{ tableStructureTemplate }}</el-descriptions-item>
          <el-descriptions-item label="所属资源表类型">{{ resourceTableType }}</el-descriptions-item>
          <el-descriptions-item label="所属机构">{{ resourceOrganName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ resourceState | stateFilter }} <i class="state" :class="{'el-icon-circle-check': resourceState === 0}" /></el-descriptions-item>
          <el-descriptions-item label="是否允许求交结果出现在对方节点上">{{ resultsAllowOpen | resultsAllowOpenFilter }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ psiResourceDesc }} </el-descriptions-item>
        </el-descriptions>

        <template v-if="resourceField">
          <p>资源表结构</p>
          <el-table
            :data="resourceField"
            class="table-list"
            border
          >
            <el-table-column align="center" label="字段名称" prop="name" />
            <el-table-column label="关联键" prop="key" align="center">
              <template slot-scope="{row}">
                <el-checkbox v-model="row.check" @change="handleChange(row.name)" />
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import { getPsiResourceDetails, saveOrUpdatePsiResource } from '@/api/PSI'

export default {
  filters: {
    resultsAllowOpenFilter(type) {
      const typeMap = {
        0: '允许',
        1: '不允许'
      }
      return typeMap[type]
    },
    stateFilter(state) {
      const stateMap = {
        0: '可用',
        1: '不可用'
      }
      return stateMap[state]
    }
  },
  data() {
    return {
      resource: {},
      listLoading: true,
      resourceId: 0,
      checked: false,
      resourceName: '',
      tableStructureTemplate: 'xjhd_nf_xh_v_1s',
      resourceTableType: '机构资源',
      resourceOrganName: '',
      status: 1,
      resultsAllowOpen: 0,
      psiResourceDesc: '223',
      keywordList: [],
      resourceState: 0,
      resourceField: []
    }
  },
  async created() {
    this.resourceId = this.$route.params.id || ''
    await this.fetchData()
  },
  methods: {
    async fetchData() {
      this.listLoading = true
      const res = await getPsiResourceDetails({ resourceId: this.resourceId })
      if (res.code === 0) {
        const { resourceOrganName, resourceState, resourceField, psiResourceDesc, resourceName } = res.result
        this.resourceName = resourceName
        this.resourceOrganName = resourceOrganName
        this.resourceState = resourceState
        this.psiResourceDesc = psiResourceDesc
        this.resourceField = resourceField
        this.listLoading = false
      }
    },
    handleChange(val) {
      this.keywordList.push(val)
      console.log(this.keywordList)
      const data = {
        resourceId: this.resourceId,
        keywordList: this.keywordList.join(','),
        tableStructureTemplate: this.tableStructureTemplate,
        resourceTableType: this.resourceTableType,
        resourceOrganName: this.resourceOrganName,
        resourceState: 0,
        resultsAllowOpen: 0,
        psiResourceDesc: this.psiResourceDesc
      }
      saveOrUpdatePsiResource(data).then(res => {
        console.log('11', res)
        this.$message({
          message: '设置关联键成功',
          type: 'success'
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-table th{
  background: #fafafa;
}
::v-deep .el-table{
  text-align: center;
}
.table-list{
  width: 400px;

}
.detail {
  padding: 20px 0 20px 20px;
  border-top: 1px solid #f0f0f0;
}
.state{
  margin-left: 5px;
  color: #67C23A;
  font-weight: bold;
}
::v-deep .detail-title{
  // width: 100px;
  text-align: right;
  justify-content: flex-end;
}

</style>
