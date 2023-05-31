<template>
  <div class="organ-select">
    <el-select v-model="organIds" v-loading="loading" multiple v-bind="$attrs" element-loading-spinner="el-icon-loading" placeholder="请选择机构" @change="handleChange">
      <el-option
        v-for="item in organList"
        :key="item.globalId"
        :label="item.globalName"
        :value="item.globalId"
      />
    </el-select>
  </div>
</template>

<script>
import { getAvailableOrganList } from '@/api/center'

export default {
  name: 'OrganSelect',
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      organIds: this.value,
      organName: '',
      organList: [],
      pageSize: 20,
      pageNo: 1,
      loading: false
    }
  },
  watch: {
    value(newVal) {
      if (newVal) {
        this.organIds = newVal
      }
    }
  },
  async created() {
    await this.getAvailableOrganList()
  },
  methods: {
    async getAvailableOrganList() {
      this.loading = true
      const res = await getAvailableOrganList()
      if (res.code === 0) {
        this.loading = false
        const { result } = res
        this.organList = result
      }
    },
    handleChange(val) {
      this.organIds = val
      const selectOrgans = []
      for (let index = 0; index < this.organIds.length; index++) {
        const organId = this.organIds[index]
        const selectOrgan = this.organList.find(item => item.globalId === organId)
        selectOrgans.push({
          organGlobalId: organId,
          organName: selectOrgan.globalName
        })
      }
      this.$emit('change', selectOrgans)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
