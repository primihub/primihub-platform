<template>
  <el-cascader ref="connectRef" v-model="cascaderValue" :options="cascaderOptions" :props="props" v-bind="$attrs" @change="handleOrganCascaderChange">
    <template slot-scope="{ node, data }">
      <span>{{ data.label }}</span>
    </template>
  </el-cascader>
</template>

<script>
import { getLocalOrganInfo, findMyGroupOrgan } from '@/api/center'

export default {
  name: 'OrganCascader',
  data() {
    return {
      cascaderValue: [],
      cascaderOptions: [],
      sysLocalOrganInfo: null,
      fusionList: [],
      serverAddress: '',
      groupList: [],
      serverAddressList: [],
      serverAddressValue: 0,
      props: {
        lazy: true,
        lazyLoad(node, resolve) {
          const { level } = node
          if (level === 1) {
            const params = {
              serverAddress: node.label
            }
            findMyGroupOrgan(params).then(({ result }) => {
              const data = result.dataList.organList.map((item) => {
                return {
                  label: item.globalName,
                  value: item.globalId,
                  leaf: true
                }
              })
              resolve(data)
            })
          } else {
            resolve([])
          }
        }
      }
    }
  },
  async created() {
    await this.getLocalOrganInfo()
  },
  methods: {
    async getLocalOrganInfo() {
      const { result } = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      if (!result.sysLocalOrganInfo) return
      this.fusionList = result.sysLocalOrganInfo?.fusionList
      this.fusionList && this.fusionList.map((item, index) => {
        this.cascaderOptions.push({
          label: item.serverAddress,
          value: item.serverAddress,
          registered: item.registered,
          show: item.show
        })
      })
      this.serverAddressValue = 0
      this.serverAddress = this.cascaderOptions[this.serverAddressValue].label
    },
    async handleOrganCascaderChange(value) {
      this.cascaderValue = value
      console.log(value)
      const nodes = this.$refs.connectRef.getCheckedNodes()
      const organId = value[1]
      const organName = nodes[0].label
      const serverAddress = this.cascaderOptions.filter(item => item.value === value[0])[0].label
      this.$emit('change', {
        serverAddress: serverAddress,
        organId,
        organName
      })
    }
  }

}
</script>

