<template>
  <div>
    <div class="form-wrap">
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item label="模型名称">
          <el-input v-model="query.modelName" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-input v-model="query.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="模型状态">
          <el-select v-model="query.taskStatus" placeholder="请选择">
            <el-option
              v-for="item in statusList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="">
          <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="model-list">
      <el-table
        :data="modelList"
      >
        <!-- 解决加载数据时先显示no data -->
        <template slot="empty">
          <p>{{ emptyText }}</p>
        </template>
        <el-table-column
          prop="modelName"
          label="名称"
        />
        <el-table-column
          prop="projectName"
          label="所属项目"
        />
        <el-table-column
          prop="resourceNum"
          label="资源数量"
        />
        <el-table-column
          prop="latestTaskStatus"
          label="状态"
        >
          <template slot-scope="{row}">
            <i :class="statusStyle(row.latestTaskStatus)" />
            {{ row.latestTaskStatus | modelStatusFilter }}
          </template>
        </el-table-column>
        <el-table-column
          v-if="hasModelViewPermission"
          label="操作"
        >
          <template slot-scope="{row}">
            <div>
              <el-button type="text" icon="el-icon-view" @click="toModelDetail(row.modelId)">查看</el-button>
              <!-- <el-button type="text" icon="el-icon-edit" @click="toModelDetail(row.modelId)">编辑</el-button> -->
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="params.pageSize" :page.sync="params.pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
  </div>
</template>

<script>
import { getModelList } from '@/api/model'
import Pagination from '@/components/Pagination'

export default {
  components: {
    Pagination
  },
  filters: {
    modelStatusFilter(status) {
      const statusMap = {
        0: '未开始',
        1: '运行中',
        2: '已完成',
        3: '异常'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      listLoading: false,
      query: {
        modelName: '',
        projectName: '',
        taskStatus: ''
      },
      projectNamesList: [],
      statusList: [{ label: '未开始', value: 0 }, { label: '运行中', value: 1 }, { label: '已完成', value: 2 }, { label: '异常', value: -1 }],
      modelList: null,
      params: {
        modelName: '',
        projectName: '',
        taskStatus: '',
        pageNo: 1,
        pageSize: 5,
        projectId: this.$route.params.id
      },
      total: 0,
      pageCount: 0,
      hidePagination: true,
      emptyText: ''
    }
  },
  computed: {
    hasModelViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelView')
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    search() {
      this.params.pageNo = 1
      this.params.modelName = this.query.modelName
      this.params.projectName = this.query.projectName
      this.params.taskStatus = this.query.taskStatus
      this.fetchData()
    },
    toModelDetail(id) {
      this.$router.push({
        path: `/model/detail/${id}`
      })
    },
    searchModel() {
      console.log('searchModel', this.searchName)
    },
    fetchData() {
      this.listLoading = true
      this.modelList = []
      console.log('fetchData', this.params)
      getModelList(this.params).then((response) => {
        console.log('response.data', response.result)
        const { result } = response
        this.modelList = result.data
        if (this.modelList.length === 0) {
          this.emptyText = '暂无数据'
        }
        this.total = result.total
        this.pageCount = result.totalPage
        // TODO 模型状态待确定
        setTimeout(() => {
          this.listLoading = false
        }, 200)
      })
    },
    statusStyle(status) {
      return status === 0 ? 'status-default' : status === 1 ? 'status-processing' : status === 2 ? 'status-end' : 'status-error'
    },
    handlePagination(data) {
      this.params.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-form--inline .el-form-item {
  margin: 5px 10px!important;
}
.form-wrap{
  padding: 20px 0;
}
.status-default,.status-processing,.status-error,.status-end{
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  vertical-align: middle;
  margin-right: 3px;
}
.status-default{
  background-color: #409EFF;
}
.status-end{
  background-color: #909399;
}
.status-processing{
  background-color: #67C23A;
}
.status-error{
  background-color: #F56C6C;
}
</style>
