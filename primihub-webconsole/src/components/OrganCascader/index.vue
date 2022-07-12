<template>
  <el-cascader ref="connectRef" :options="cascaderOptions" :props="props" v-bind="$attrs" @change="handleOrganCascaderChange">
    <template slot-scope="{ node, data }">
      <span>{{ data.label }}</span>
    </template>
  </el-cascader>
</template>

<script>
import { getLocalOrganInfo, findMyGroupOrgan } from '@/api/center'

export default {
  name: 'OrganCascader',
  props: {
    cascaderValue: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      cascaderOptions: [],
      sysLocalOrganInfo: null,
      fusionList: [],
      serverAddress: '',
      groupList: [],
      serverAddressList: [],
      serverAddressValue: 0,
      props: {
        lazy: true,
        lazyLoad: this.lazyLoad
      }
    }
  },
  async created() {
    await this.getLocalOrganInfo()
  },
  methods: {
    lazyLoad(node, resolve) {
      const { level } = node
      if (level === 1) {
        const params = {
          serverAddress: node.label
        }
        findMyGroupOrgan(params).then(({ result }) => {
          const organList = result.dataList.organList || []
          if (organList.length > 0) {
            const data = organList && organList.map((item) => {
              return {
                label: item.globalName,
                value: item.globalId,
                leaf: true
              }
            })
            resolve(data)
          } else {
            this.$message({
              message: '节点下暂无其他机构'
            })
            const index = this.cascaderOptions.findIndex(item => item.label === node.label)
            this.cascaderOptions[index].disabled = true
            resolve([])
          }
        })
      } else {
        resolve([])
      }
    },
    async getLocalOrganInfo() {
      const res = await getLocalOrganInfo()
      this.sysLocalOrganInfo = res.result?.sysLocalOrganInfo
      if (!res.result.sysLocalOrganInfo) return
      this.fusionList = res.result.sysLocalOrganInfo?.fusionList
      this.fusionList && this.fusionList.map((item, index) => {
        this.cascaderOptions.push({
          label: item.serverAddress,
          value: item.serverAddress,
          registered: item.registered,
          show: item.show,
          disabled: false
        })
      })
      this.serverAddressValue = 0
      this.serverAddress = this.cascaderOptions[this.serverAddressValue].label
    },
    async handleOrganCascaderChange(value) {
      const nodes = this.$refs.connectRef.getCheckedNodes()
      const organId = value[1]
      const organName = nodes[0].label
      const serverAddress = this.cascaderOptions.filter(item => item.value === value[0])[0].label
      this.$emit('change', {
        serverAddress: serverAddress,
        organId,
        organName,
        cascaderValue: value
      })
    }
  }

}
</script>

