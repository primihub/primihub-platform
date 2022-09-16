<template>
  <el-dialog
    title="选择模型"
    width="1000px"
    :visible="visible"
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <el-table
      v-loading="listLoading"
      border
      :data="modelList"
    >
      <el-table-column
        prop="modelId"
        label="模型ID"
        align="center"
      />
      <el-table-column
        prop="modelName"
        label="模型名称"
      />
      <el-table-column
        prop="projectName"
        label="所属项目"
      />
      <el-table-column
        prop="taskEndDate"
        label="建模完成时间"
        min-width="120"
      />
      <el-table-column
        label="机构名称"
      >
        <template slot-scope="{row}">
          <span>发起方: {{ row.createdOrgan }}</span><br>
          <div>协作方:
            <span v-for="(item,index) in row.providerOrgans" :key="item.organId">
              <span>{{ item.organName }}<span v-if="index === 0 && row.providerOrgans.length>1">，</span></span>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="resourceNum"
        label="所用资源数"
        align="center"
      />

      <el-table-column label="选择模型" align="center">
        <template slot-scope="{row}">
          <el-switch
            v-model="row.selected"
            active-color="#13ce66"
            inactive-color="#ccc"
            @change="handleSelect(row)"
          />
        </template>
      </el-table-column>
    </el-table>
    <span slot="footer" class="dialog-footer">
      <pagination v-show="pageCount>1" small :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next" @pagination="handlePagination" />
      <div>
        <el-button @click="closeDialog">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </span>
  </el-dialog>
</template>

<script>
import { getModelTaskSuccessList } from '@/api/model'
import Pagination from '@/components/Pagination'

export default {
  name: 'ResourceDialog',
  components: {
    Pagination
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      modelList: [],
      total: 0,
      currentPage: 1,
      pageNo: 1,
      pageSize: 5,
      pageCount: 0,
      listLoading: false
    }
  },
  watch: {
    visible: async function(val) {
      if (val) {
        this.pageNo = 1
        if (this.modelList.length > 0) {
          return
        }
        await this.getModelTaskSuccessList()
      }
    }
  },
  methods: {
    async getModelTaskSuccessList() {
      this.listLoading = true
      const res = await getModelTaskSuccessList({
        pageSize: this.pageSize,
        pageNo: this.pageNo
      })
      this.modelList = res.result.data.map(item => {
        item.selected = false
        return item
      })
      this.dialogVisible = true
      console.log('modelList', this.modelList)
      this.total = res.result?.total
      this.pageCount = res.result?.totalPage
      this.listLoading = false
    },
    handleSelect(row) {
      this.modelList.map(item => {
        if (item.modelId === row.modelId) {
          item.selected = true
        } else {
          item.selected = false
        }
      })
      this.selectRow = row
    },
    closeDialog() {
      this.$emit('close')
    },
    handleSubmit() {
      this.$emit('submit', this.selectRow)
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.getModelTaskSuccessList()
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px!important;
}
::v-deep .el-table td.el-table__cell div{
  line-height: 1.5;
}
::v-deep .table.el-table .el-table__cell{
  padding: 5px 0;
}
.pagination-container {
  padding: 10px 0 0 0;
  display: flex;
  justify-content: flex-start;
}
.dialog-footer{
  display: flex;
  justify-content: flex-end;
  padding-bottom: 30px;
  align-items: center;
}
::v-deep .el-dialog__body{
  padding: 10px 20px 0 20px;
}
.input-with-search{
  width: 300px;
  margin: 0 0 10px 0px;
}
.dialog-footer{
  margin-top: 20px;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  padding: 10px 0 0 0;
  display: flex;
  justify-content: center;
}
</style>
