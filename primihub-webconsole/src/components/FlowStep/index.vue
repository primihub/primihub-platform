<template>
  <div class="step-container" @mouseover="handleStepOver" @mouseleave="showError = false">
    <el-steps :active="active" simple finish-status="success" process-status="wait">
      <el-step v-for="(step) in data" :key="step.step" icon="el-icon-loading" :title="step.title" :status="step.status" />
    </el-steps>
    <div v-if="showError" class="task-error" @mouseenter="showError = true">
      <p>错误信息：</p>
      <p v-for="(item,index) in errorText" :key="index">
        {{ item }}
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FlowStep',
  props: {
    active: {
      type: Number,
      default: 1
    },
    data: {
      type: Array,
      default: () => []
    },
    taskState: {
      type: Number,
      default: 0
    },
    errorText: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      showError: false,
      taskError: ''
    }
  },
  methods: {
    handleStepOver() {
      if (this.taskState !== 3) {
        this.showError = false
      } else {
        this.showError = true
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.step-container{
  cursor: pointer;
}
.task-error{
  width: 320px;
  padding: 10px;
  color: #fff;
  background-color: rgba($color: #000000, $alpha: .7);
  position: absolute;
  top: 40px;
  max-height: 280px;
  left: calc((100% / 2) - 125px);
  border-radius: 4px;
  font-size: 12px;
  word-break: break-all;
  overflow-y: scroll;
  margin-bottom: 20px;
}
.el-steps--simple{
  margin:  5px 0;
  padding: 11px 8%;
}
</style>
