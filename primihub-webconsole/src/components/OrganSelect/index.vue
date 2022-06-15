<template>
  <div class="organ-select">
    <el-select v-model="organId" v-loading="loading" element-loading-spinner="el-icon-loading" placeholder="请选择求交机构" @change="handleChange">
      <el-option
        v-for="item in organList"
        :key="item.organId"
        :label="item.organName"
        :value="item.organId"
      />
    </el-select>
  </div>
</template>

<script>
import { getOrgans } from '@/api/organ'

const ORGAN_KEY = 'priOrgan'

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
    await this.getOrgans()
  },
  methods: {
    async getOrgans() {
      this.loading = true
      const res = await getOrgans()
      if (res.code === 0) {
        this.loading = false
        const { sysOrganList } = res.result
        this.organList = sysOrganList
      }
    },
    handleChange(val) {
      this.organId = val
      const selectOrgan = this.organList.find(item => item.organId === val)
      this.organName = selectOrgan.organName
      localStorage.setItem(ORGAN_KEY, JSON.stringify({ organId: val, organName: this.organName }))
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
