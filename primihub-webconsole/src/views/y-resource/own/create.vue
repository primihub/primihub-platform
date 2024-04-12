<script>
import StepOne from './components/step-one.vue'
import StepTwo from './components/step-two.vue'
import StepThree from './components/step-three.vue'
import ViewForm from '../../../components/ViewBox/form.vue'

export default {
  name: 'YResourceCreate',
  components: {
    StepOne,
    StepTwo,
    StepThree,
    ViewForm
  },
  data() {
    return {
      active: 1,
      stepComps: [
        { component: 'StepOne', title: '基本信息' },
        { component: 'StepTwo', title: '导入数据' },
        { component: 'StepThree', title: '样本信息完善' }
      ],
      stepDate1: {},
      stepDate2: {},
      stepDate3: {}
    }
  },
  computed: {
    currentProp() {
      switch (this.stepComps[this.active - 1].component) {
        case 'StepOne':
          return this.stepDate1
        case 'StepTwo':
          return this.stepDate2
        case 'StepThree':
          return this.stepDate3
        default:
          return this.stepDate1
      }
    }
  },
  mounted() {
  },
  methods: {
    /** save resource or next step */
    handelSave() {
      if (!this.$refs.stepRef.validateForm()) return
      const data = this.$refs.stepRef.getParams()
      switch (this.active) {
        case 1:
          this.stepDate1 = data
          this.active += 1
          break
        case 2:
          this.stepDate2 = data
          this.stepDate3.fieldList = data?.fieldList
          this.active += 1
          break
        case 3:
          this.stepDate3 = data
          this.saveFn()
          break
      }
    },

    saveFn() {
      console.log({ ...this.stepDate1, ...this.stepDate2, ...this.stepDate3 })
    },

    /** prev step */
    handelPrev() {
      if (this.active > 1) this.active -= 1
    },

    /** go to resource list */
    toResourceList() {
      this.$router.push({
        name: 'YResourceOwnList'
      })
    }
  }
}
</script>

<template>
  <div class="resource-form-container">
    <div class="resource-step-wrapper">
      <el-steps :active="active" class="resource-step">
        <el-step v-for="(item, index) in stepComps" :key="index" :title="item.title" />
      </el-steps>
    </div>
    <view-form width="100%">
      <template v-slot:container>
        <keep-alive>
          <component :is="stepComps[active-1].component" ref="stepRef" :current-prop="currentProp" />
        </keep-alive>
      </template>
      <template v-slot:footer>
        <el-button @click="toResourceList">取消</el-button>
        <el-button v-if="active > 1" @click="handelPrev">上一步</el-button>
        <el-button type="primary" @click="handelSave">{{ active === 3 ? '保存' : '下一步' }}</el-button>
      </template>
    </view-form>
  </div>
</template>

<style lang="scss" scoped>
.resource-form-container{
  background-color: #fff;
  padding: 20px 0 0 0;
}
.resource-step-wrapper{
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
</style>
