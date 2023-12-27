<template>
  <div class="text-center">
    <el-transfer
      ref="userTransfer"
      v-model="userValue"
      v-loading="organLoading"
      v-bind="$attrs"
      class="transfer-container"
      filterable
      :left-default-checked="userLeftDefaultChecked"
      :right-default-checked="userRightDefaultChecked"
      :titles="['机构内所有成员', '已选']"
      :format="{
        noChecked: '${total}',
        hasChecked: '${checked}/${total}'
      }"
      :data="userList"
      @change="handleChange($event)"
    >
      <span slot-scope="{ option }">{{ option.label }}</span>
    </el-transfer>
  </div>
</template>

<script>
import { getUserList } from '@/api/userAdmin'

export default {
  name: 'AuthorizationUserTransfer',
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      organLoading: false,
      userValue: this.value,
      userLeftDefaultChecked: [],
      userRightDefaultChecked: [],
      userList: []
    }
  },
  watch: {
    value(newValue, oldValue) {
      console.log('newValue', newValue)
      if (newValue) {
        this.userValue = newValue
        // this.userRightDefaultChecked = newValue
      }
    }
  },

  mounted() {
    this.getUserList()
  },

  methods: {
    handleChange(value) {
      this.userRightDefaultChecked = value
      this.$emit('change', this.userRightDefaultChecked)
    },
    getUserList() {
      const params = {
        pageSize: 100,
        pageNum: 1
      }
      getUserList(params).then((res) => {
        if (res.code === 0) {
          const { sysUserList } = res.result
          const userList = sysUserList.map(item => {
            return {
              key: item.userId,
              label: item.userName
            }
          })
          // this.userList = sysUserList.map(item => {
          //   return {
          //     key: item.userId,
          //     label: item.userName
          //   }
          // })
          // if (this.userValue.length > 0) {
          //   this.userValue.forEach((item) => {
          //     this.userList.push(userList.find(user => user.userId !== item))
          //   })
          // } else {
          //   this.userList = userList
          // }
          this.userList = userList
          if (this.userValue.length > 0) {
            this.userRightDefaultChecked = this.userValue
          }
        }
      })
    }
  }
}
</script>
