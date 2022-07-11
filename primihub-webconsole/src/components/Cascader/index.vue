<template>
  <el-cascader-panel
    v-model="valueList"
    :options="optionsList"
    :props="props"
    v-bind="$attrs"
    @change="handleChange"
  />
</template>

<script>
import { getLocalOrganInfo, findMyGroupOrgan } from '@/api/center'

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      // 级联选择器
      optionsList: [],
      valueList: this.value,
      props: {
        multiple: true,
        lazyLoad: this.lazyLoad,
        leaf: 'leaf',
        lazy: true
      },
      loading: false,
      organList: []
    }
  },
  mounted() {
  },
  methods: {
    async lazyLoad(node, resolve) {
      if (node.level === 0) {
        if (this.valueList.length > 0) {
          await this.formatting()
        } else {
          const res = await this.getLocalOrganInfo()
          resolve(res)
        }
      } else if (node.level === 1) {
        const list = this.valueList
        if (!node.data.children) {
          const res = await this.findMyGroupOrgan(node.label)
          resolve(res)
        } else {
          resolve([])
        }
        this.valueList = [...new Set([...this.valueList, ...list])]
      } else {
        resolve([])
      }
    },
    async getLocalOrganInfo() {
      this.loading = true
      const { result } = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      if (!result.sysLocalOrganInfo) return
      this.fusionList = result.sysLocalOrganInfo?.fusionList
      return this.fusionList && this.fusionList.map((item, index) => {
        return {
          label: item.serverAddress,
          value: item.serverAddress,
          registered: item.registered,
          show: item.show
        }
      })
    },
    async findMyGroupOrgan(serverAddress) {
      const { result } = await findMyGroupOrgan({ serverAddress })
      this.organList = result.dataList.organList || []
      this.loading = false
      return result.dataList?.organList.map((item) => {
        return {
          label: item.globalName,
          value: item.globalId,
          leaf: true
        }
      })
    },
    async formatting() {
      const res = await this.getLocalOrganInfo()
      this.valueList.forEach((item) => {
        if (item.length > 1) {
          item.forEach(async(s, index) => {
            if (index === item.length - 1) {
              return
            }
            const arr = await this.findMyGroupOrgan(s)
            this.findItem(res, arr, s)
          })
        }
      })
    },
    findItem(res, arr, id) {
      for (let i = 0; i < res.length; i++) {
        if (res[i].value === id) {
          res[i].children = arr
          this.optionsList = res
        }
        if (res[i].children) {
          this.findItem(res[i].children, arr, id)
        }
      }
    },
    handleChange() {
      // save params
      const fusionOrganList = this.valueList.map(item => {
        return {
          organServerAddress: item[0],
          organGlobalId: item[1],
          organName: this.organList.filter(n => n.globalId === item[1])[0].globalName
        }
      })
      this.$emit('change', {
        valueList: this.valueList,
        fusionOrganList
      })
    }
  }
}
</script>

<style>

</style>
