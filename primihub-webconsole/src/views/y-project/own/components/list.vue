<script>
import { mapGetters } from "vuex"
import { closeProject, openProject } from "@/api/project"

export default {
  name: "YOwnResource",
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {}
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes("ProjectDetail")
    },
    hasDeletePermission() {
      return this.$store.getters.buttonPermissionList.includes("ProjectDelete")
    },
    hasOpenPermission() {
      return this.$store.getters.buttonPermissionList.includes("openProject")
    },
    ...mapGetters(["buttonPermissionList", "userOrganId"])
  },
  mounted() {},
  methods: {
    /** go to project detail */
    toDetailPage(id) {
      this.$router.push({ name: "YProjectDetail", params: { id } })
    },

    /** change row disable/able */
    handelProjectAction(id, action) {
      let text, actionProjectRequest, successMsg
      if (action === "close") {
        text =
          "禁用后，数据、任务、模型将均不可用，进行中的任务立即停止，确认禁用么？"
        actionProjectRequest = closeProject
        successMsg = "禁用成功"
      } else {
        text = "开启后，项目可正常发起任务，确认开启么？"
        actionProjectRequest = openProject
        successMsg = "启动成功"
      }

      this.$confirm(text, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(async () => {
        const { code } = await actionProjectRequest({ id })
        if (code === 0) {
          this.$message.success(successMsg)
          this.$emit("refresh")
        }
      })
    },

    /** when the resource state changes, modify the resource style. */
    tableRowDisabled({ row }) {
      return row.status === 2 ? "table-row-disabled" : ""
    },

    statusStyle(status) {
      // 项目状态 0审核中 1可用 2关闭 11 全部可用 12 部分可用
      let projectStatusStyles = {
        0: 'status-0 el-icon-refresh',
        1: 'status-1 el-icon-circle-check',
        2: 'status-0 el-icon-refresh',
        11: 'status-1 el-icon-circle-check',
        12: 'status-1 el-icon-circle-check'
      }
      return projectStatusStyles[status]
    }
  }
}
</script>

<template>
  <el-table
    :data="data"
    :row-class-name="tableRowDisabled"
    empty-text="暂无数据"
    border
  >
    <el-table-column prop="projectId" label="项目ID" min-width="240">
      <template slot-scope="{ row }">
        <template v-if="hasViewPermission && row.status !== 2">
          <el-link type="primary" @click="toDetailPage(row.id)">{{
            row.projectId
          }}</el-link>
          <br />
        </template>
        <template v-else> {{ row.projectId }}<br /> </template>
      </template>
    </el-table-column>
    <el-table-column prop="projectName" label="项目名称" min-width="200" />
    <el-table-column prop="providerOrganNames" label="合作方" min-width="140" />
    <el-table-column
      prop="providerOrganNames"
      label="项目类型"
      min-width="140"
    />
    <el-table-column prop="createDate" label="创建时间" min-width="160" />
    <el-table-column label="任务状态" min-width="220">
      <template slot-scope="{ row }">
        运行中：{{ row.taskRunNum }}  成功：{{ row.taskSuccessNum || 0 }}  失败：{{ row.taskFailNum }}
      </template>
    </el-table-column>
    <el-table-column prop="resourceNum" label="资源数量" align="center" />
    <el-table-column prop="status" label="项目状态" width="80">
      <template slot-scope="{ row }">
        <span :class="statusStyle(row.status)">{{ row.status | projectAuditStatusFilter }}</span>
      </template>
    </el-table-column>
    <el-table-column label="操作" fixed="right" width="120" align="center">
      <template slot-scope="{ row }">
        <el-button
          v-if="hasViewPermission"
          type="text"
          :disabled="row.status === 2"
          @click="toDetailPage(row.id)"
          >查看</el-button
        >
        <el-button
          v-if="
            hasDeletePermission &&
            row.status === 1 &&
            row.organId === userOrganId
          "
          type="text"
          @click="handelProjectAction(row.id, 'close')"
          >禁用</el-button
        >
        <el-button
          v-if="
            hasOpenPermission && row.status === 2 && row.organId === userOrganId
          "
          type="text"
          @click="handelProjectAction(row.id, 'open')"
          >启用</el-button
        >
      </template>
    </el-table-column>
  </el-table>
</template>

<style lang="scss" scoped>
@import "@/styles/variables.scss";
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
