<template>
  <el-table
    :data="resourceList"
    v-bind="$attrs"
    :row-class-name="tableRowDisabled"
    empty-text="暂无数据"
  >
    <el-table-column
      prop="id"
      label="衍生数据ID"
    />
    <el-table-column
      prop="resourceName"
      label="衍生资源名称"
      min-width="120"
    >
      <template slot-scope="{row}">
        <el-link v-if="row.resourceState === 0" :underline="false" size="small" type="primary" @click="toResourceDetailPage(row.id)">{{ row.resourceName }}</el-link>
        <template v-else>{{ row.resourceName }}</template>
      </template>
    </el-table-column>

    <el-table-column
      label="衍生资源信息"
      min-width="120"
    >
      <template slot-scope="{row}">
        <div class="info">
          特征量：{{ row.fileColumns }}<br>
          样本量：{{ row.fileRows }} <br>
          正例样本数量：{{ row.fileYRows || 0 }}<br>
          正例样本比例：{{ row.fileYRatio || 0 }}%<br>
        </div>
        <div class="margin-top-5">
          <el-tag v-if="row.resourceContainsY" type="primary" size="mini">包含Y值</el-tag>
          <el-tag v-else type="danger" size="mini">不包含Y值</el-tag>
        </div>
      </template>
    </el-table-column>
    <el-table-column
      prop="taskIdName"
      label="任务ID"
      min-width="100"
    >
      <template slot-scope="{row}">
        <el-link :underline="false" size="small" type="primary" @click="toModelTaskDetail(row)">{{ row.taskIdName }}</el-link>
      </template>
    </el-table-column>
    <el-table-column
      prop="tag"
      label="衍生数据来源"
    />
    <el-table-column
      prop="createDate"
      label="创建时间"
    >
      <template slot-scope="{row}">
        {{ row.createDate.split(' ')[0] }} <br>
        {{ row.createDate.split(' ')[1] }}
      </template>
    </el-table-column>
    <el-table-column
      label="操作"
      fixed="right"
      width="160"
      align="center"
    >
      <template slot-scope="{row}">
        <el-button v-if="row.resourceState === 0" type="text" @click="toResourceDetailPage(row.id)">查看</el-button>
        <el-button type="text" @click="changeResourceStatus(row)">{{ row.resourceState === 0 ? '下线': '上线' }}</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import { resourceStatusChange } from '@/api/resource'

export default {
  name: '',
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      // resourceList: this.data
    }
  },
  computed: {
    resourceList() {
      return this.data
    }
  },
  methods: {
    toModelTaskDetail(row) {
      this.$router.push({
        path: `/project/detail/${row.projectId}/task/${row.taskId}`
      })
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'DerivedDataResourceDetail',
        params: { id }
      })
    },
    changeResourceStatus({ id, resourceState }) {
      resourceState = resourceState === 0 ? 1 : 0
      resourceStatusChange({ resourceId: id, resourceState }).then(res => {
        if (res.code === 0) {
          this.$message({
            message: resourceState === 0 ? '上线成功' : '下线成功',
            type: 'success'
          })
          const posIndex = this.resourceList.findIndex(item => item.id === id)
          this.resourceList[posIndex].resourceState = resourceState
        }
      })
    },
    tableRowDisabled({ row }) {
      if (row.resourceState === 1) {
        return 'resource-disabled'
      } else {
        return ''
      }
    }
  }
}
</script>
