<template>
  <div class="organ-select">
    <el-select v-model="organId" v-loading="loading" v-bind="$attrs" element-loading-spinner="el-icon-loading" placeholder="请选择机构" @change="handleChange">
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
  data() {
    return {
      organId: '',
      organName: '',
      organList: [],
      pageSize: 20,
      pageNo: 1,
      loading: false
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
      this.organId = val
      const selectOrgan = this.organList.find(item => item.globalId === val)
      this.organName = selectOrgan.organName
      this.$emit('change', {
        organId: this.organId,
        organName: this.organName
      })
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
