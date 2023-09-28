<template>
  <div class="step-container">
    <el-steps :active="active" simple finish-status="success" process-status="wait">
      <el-step v-for="(step) in data" :key="step.step" icon="el-icon-loading" :title="step.title" :status="step.status" @mouseover.native="handleStepOver(step)" @mouseleave.native="showError = false" />
    </el-steps>
    <div v-if="showError" class="task-error">
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

  mounted() {

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
.task-error{
  width: 300px;
  padding: 10px;
  color: #fff;
  background-color: rgba($color: #000000, $alpha: .7);
  position: absolute;
  top: 40px;
  left: calc((100% / 2) - 100px);
  border-radius: 4px;
  font-size: 12px;
}
.el-steps--simple{
  margin:  5px 0;
  padding: 11px 8%;
}
</style>
