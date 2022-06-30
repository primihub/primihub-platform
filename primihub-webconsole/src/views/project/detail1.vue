<template>
  <div class="app-container">
    <h2>项目详情</h2>
    <div v-loading="listLoading" class="detail-panel">
      <el-descriptions :column="1" label-class-name="detail-title">
        <el-descriptions-item label="项目名称">{{ list.projectName }}</el-descriptions-item>
        <el-descriptions-item label="项目描述">{{ list.projectDesc }}</el-descriptions-item>
        <!-- <el-descriptions-item label="创建机构">{{ list.organName }}</el-descriptions-item> -->
        <el-descriptions-item label="创建时间">{{ list.createDate }}</el-descriptions-item>
        <!-- <el-descriptions-item label="参与机构">
          <div class="context">
            <div class="organ-box">
              <div v-for="item in list.organNames" :key="item.organId" class="organ-txt">{{ item }}</div>
            </div>
          </div>
        </el-descriptions-item> -->
        <el-descriptions-item label="数据资源">
          <div class="context">
            <div class="card-box">
              <ResourceItemSimple v-for="item in list.resources" :key="item.resourceId" class="item" :data="item" />
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="list.models && hasAddPermission" label="模型">
          <div class="context">
            <div class="card-box">
              <ModelItemCreate class="item" @click.native="toModelCreate" />
              <ModelItemSimple v-for="item in list.models" :key="item.modelId" class="item" :data="item" @click="handleModelClick" />
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>

    </div>
  </div>
</template>

<script>
import { getProjectDetail } from '@/api/project'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ModelItemSimple from '@/components/ModelItemSimple'
import ModelItemCreate from '@/components/ModelItemCreate'
import { mapGetters } from 'vuex'

export default {
  components: {
    ModelItemCreate,
    ResourceItemSimple,
    ModelItemSimple
  },
  data() {
    return {
      list: [],
      listLoading: true,
      projectId: 0
    }
  },
  computed: {
    hasAddPermission() {
      return this.buttonPermissionList.includes('ModelCreate')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  created() {
    this.fetchData()
  },
  methods: {
    handleModelClick(id) {
      this.$router.push({
        name: 'ModelDetail',
        params: { id }
      })
    },
    toModelCreate() {
      const index = this.list.resources.findIndex(item => item.isAuthed !== 1)
      if (index > -1) {
        this.$message({
          message: '添加模型需数据资源全部授权',
          type: 'warning'
        })
        return
      }
      this.$router.push({
        path: '/model/create',
        query: { projectId: this.projectId }
      })
    },
    fetchData() {
      this.listLoading = true
      this.projectId = this.$route.params.id || this.list.projectId
      getProjectDetail({ projectId: this.projectId }).then(res => {
        if (res.code === 0) {
          this.listLoading = false
          this.list = res.result
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-panel {
  padding: 20px 0 20px 20px;
  border-top: 1px solid #f0f0f0;
  .card-box {
    display: flex;
    flex-wrap: wrap;
    .item {
      margin-right: 20px;
      margin-bottom: 20px;
    }
  }
}
::v-deep .detail-title{
  width: 80px;
  text-align: right;
  justify-content: flex-end;
}
::v-deep .el-descriptions-item__container{
  flex-wrap: wrap;
}
</style>
