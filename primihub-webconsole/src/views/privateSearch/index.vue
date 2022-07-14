<template>
  <div class="container">
    <div class="steps">
      <el-steps :active="active" finish-status="success" align-center="">
        <el-step title="查询条件" />
        <el-step title="查询结果" />
      </el-steps>
      <step-first v-if="active === 0" :has-permission="hasSearchPermission" @next="next" />
      <step-second v-if="active === 1" :has-permission="hasSearchPermission" :task-id="taskId" :task-date="taskDate" :pir-param="pirParam" @reset="reset" />
    </div>

  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import StepFirst from '@/components/QueryStep/StepFirst'
import StepSecond from '@/components/QueryStep/StepSecond'

export default {
  components: {
    StepFirst,
    StepSecond
  },
  data() {
    return {
      active: 0,
      pirParam: 0,
      taskId: 0,
      taskDate: 0
    }
  },
  computed: {
    hasSearchPermission() {
      return this.buttonPermissionList.includes('PrivateSearchButton')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  methods: {
    next(data) {
      if (!data.taskId) {
        return
      }
      this.pirParam = data.pirParam
      this.taskId = data.taskId
      this.taskDate = data.taskDate
      if (this.active++ > 2) this.active = 0
    },
    reset() {
      this.active = 0
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.steps{
  background-color: #fff;
  border-radius: $sectionBorderRadius;
  padding:50px;
}
</style>
