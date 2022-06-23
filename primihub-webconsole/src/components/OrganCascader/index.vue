<template>
  <el-cascader ref="connectRef" v-model="cascaderValue" :options="cascaderOptions" :props="props" v-bind="$attrs" @change="handleOrganCascaderChange">
    <template slot-scope="{ node, data }">
      <span>{{ data.label }}</span>
    </template>
  </el-cascader>
</template>

<script>
import { getLocalOrganInfo, findAllGroup, findOrganInGroup } from '@/api/center'

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
            findAllGroup(params).then(({ result }) => {
              const data = result.organList.groupList.map((item) => {
                return {
                  label: item.groupName,
                  value: item.id
                }
              })
              resolve(data)
            })
          } else if (level === 2) {
            const params = {
              groupId: node.value,
              serverAddress: node.parent.label
            }
            findOrganInGroup(params).then(({ code = -1, result }) => {
              this.organList = result.dataList.organList
              const data = this.organList.map(item => {
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
          value: index,
          registered: item.registered,
          show: item.show
        })
      })
      this.serverAddressValue = 0
      this.serverAddress = this.cascaderOptions[this.serverAddressValue].label
    },
    async findAllGroup() {
      const { result } = await findAllGroup({ serverAddress: this.serverAddress })
      this.groupList = result.organList.groupList
      const data = this.groupList.map((item) => {
        return {
          label: item.groupName,
          value: item.id,
          serverAddress: this.serverAddress
        }
      })
      return data
    },
    async findOrganInGroup() {
      const params = {
        groupId: this.query.groupId,
        serverAddress: this.serverAddress
      }
      const { result } = await findOrganInGroup(params)
      this.organList = result.dataList.organList
    },
    async handleOrganCascaderChange(value) {
      this.cascaderValue = value
      const nodes = this.$refs.connectRef.getCheckedNodes()
      const organId = value[2]
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

