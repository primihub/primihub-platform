<template>
  <el-cascader ref="connectRef" v-model="value" :options="cascaderOptions" :props="propOption" v-bind="$attrs" @change="handleOrganCascaderChange">
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
    },
    options: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      showCascader: false,
      value: this.cascaderValue,
      cascaderOptions: [],
      sysLocalOrganInfo: null,
      fusionList: [],
      serverAddress: '',
      groupList: [],
      serverAddressList: [],
      serverAddressValue: 0,
      propOption: {
        lazy: true,
        lazyLoad: this.lazyLoad,
        emitPath: true,
        expandTrigger: 'hover'
      }
    }
  },
  async created() {
    await this.getLocalOrganInfo()
    if (this.value.length > 0) {
      this.findMyGroupOrgan()
    }
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
            this.showCascader = true
            resolve(data)
          }
        })
      } else {
        resolve([])
      }
    },
    findMyGroupOrgan() {
      findMyGroupOrgan({ serverAddress: this.serverAddress }).then(({ result }) => {
        const organList = result.dataList.organList || []
        console.log(organList)
        if (organList.length > 0) {
          const data = organList && organList.map((item) => {
            return {
              label: item.globalName,
              value: item.globalId,
              leaf: true
            }
          })
          if (this.options.length > 0) {
            this.cascaderOptions = this.options
          } else {
            const posIndex = this.cascaderOptions.findIndex(item => item.label === this.serverAddress)
            this.$set(this.cascaderOptions[posIndex], 'children', data)
          }
        }
      })
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
          disabled: false,
          children: []
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
      console.log(value)
    }
  }

}
</script>

