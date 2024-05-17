<template>
  <div v-loading="listLoading" class="container">
    <div class="search-area">
      <el-form :model="query" :inline="true" @keyup.enter.native="search">
        <el-form-item>
          <el-input v-model="query.taskName" size="small" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="search">查询</el-button>
          <el-button icon="el-icon-refresh-right" size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="model-list">
      <ul class="flex task-record">
        <li
          v-for="item in tablist"
          :key="item.index"
          :class="{active: activeIndex === item.index}"
          @click="handelChangeTab(item)"
        >{{ item.title }}</li>
      </ul>
      <el-table
        empty-text="暂无数据"
        :data="taskList"
        :default-sort="{prop: 'createDate',order: 'descending'}"
      >
        <el-table-column
          label="任务名称"
        >
          <template slot-scope="{row}">
            {{activeIndex === 1 ? row.pirName : row.psiName}}
          </template>
        </el-table-column>
        <el-table-column
          prop="resultRowsNum"
          label="结果样本数"
          width="100"
          align="center"
        />
        <el-table-column
          prop="commitRowsNum"
          label="提交样本数"
          width="100"
          align="center"
        />
        <el-table-column
          prop="startTime"
          label="发起时间"
          width="240"
          align="center"
        />
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>
  </div>
</template>

<script>
import { pirRecordPageRequest, psiRecordPageRequest } from '@/api/taskRecord'
import Pagination from '@/components/Pagination'

export default {
  name: 'Log',
  components: {
    Pagination
  },
  data() {
    return {
      activeIndex: 1,
      taskRequest: pirRecordPageRequest,
      tablist:[{
        title: '隐匿查询任务',
        index: 1,
        request: pirRecordPageRequest,
      },{
        title: '隐私求交任务',
        index: 2,
        request: psiRecordPageRequest
      }],
      listLoading: false,
      query: {
        taskName: '',
      },
      taskList: null,
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      taskTypeOptions: [
        {
          value: 1,
          label: '联合建模'
        },
        {
          value: 2,
          label: '安全求交'
        },
        {
          value: 3,
          label: '隐匿查询'
        },
        {
          value: 4,
          label: '联合预测'
        }
      ],
      dialogVisible: false,
      taskId: 0,
      taskState: 0,
    }
  },
  computed: {
    hasModelViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelView')
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    handelChangeTab({request, index}){
      this.taskRequest = request
      this.activeIndex = index
      this.reset()
    },

    async fetchData() {
      this.listLoading = true;
      const params = {...this.query, pageNo: this.pageNo, pageSize: this.pageSize}
      try {
        const {code, result, msg} = await this.taskRequest(params)
        if (code === 0) {
          const { total, totalPage, data} = result
          this.total = total
          this.pageCount = totalPage
          this.taskList = data
        } else {
          this.$message.error(msg)
        }
      } finally{
        this.listLoading = false;
      }
    },

    search() {
      this.pageNo = 1
      this.fetchData()
    },

    reset() {
      this.query.taskName = ''
      this.search()
    },

    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
.task-record, .task-record li{
  list-style: none;
  margin: 0;
  padding: 0;
}
.task-record{
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 25px;
}
.task-record li {
  position: relative;
  bottom: -1px;
  padding: 10px 20px;
  border: 1px solid #e4e7ed;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  &:hover{
    color: #409eff;
  }
}

.task-record li + li{
  border-left: none;
}
.task-record li.active{
  border-bottom-color: #fff;
  color: #409eff;
}

.search-area {
  padding: 30px 0px 10px 20px;
  background-color: #fff;
  display: flex;
  flex-wrap: wrap;
}
.form-wrap{
  padding-top: 20px;
  background-color: #fff;
}
.model-list {
  margin-top: 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
  padding: 30px;
}
.pagination {
  padding: 50px;
  display: flex;
  justify-content: center;
}
.buttons{
  display: flex;
  justify-content: space-evenly;
}
</style>
