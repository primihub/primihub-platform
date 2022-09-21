<template>
  <div class="container">
    <div class="steps">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="查询条件" />
        <el-step title="查询结果" />
      </el-steps>
      <step-first v-if="active === 0" :has-permission="hasSearchPermission" />
    </div>

  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import StepFirst from '@/components/QueryStep/StepFirst'

export default {
  components: {
    StepFirst
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
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.steps{
  background-color: #fff;
  padding:50px;
}
</style>
