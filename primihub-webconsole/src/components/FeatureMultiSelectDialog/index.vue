<template>
  <el-dialog
    title="选择多方特征列"
    width="50%"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <el-tabs v-model="activeName">
      <el-tab-pane v-for="(item,index) in selectedData" :key="index" :label="item.organName" :name="item.organId">
        <checkbox :organ-id="item.organId" :options="filterData(item.resourceField)" :checked="item.checked" @change="handleChange" />
      </el-tab-pane>
    </el-tabs>
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="handleClose">取 消</el-button>
      <el-button size="small" type="primary" @click="handleDialogSubmit">确 定</el-button>
    </span>
  </el-dialog>

</template>

<script>
import checkbox from '@/components/BaseCheckbox'
import { String, Boolean } from '@/const/filedType'

export default {
  name: 'FeatureMultiSelectDialog',
  components: {
    checkbox
  },
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      activeName: '',
      selectedData: []
    }
  },
  created() {
    this.selectedData = JSON.parse(JSON.stringify(this.data))
    this.activeName = this.selectedData && this.selectedData[0].organId
  },
  methods: {
    filterData(data) {
      if (data) {
        return data.map(item => item.fieldName)
      } else {
        return []
      }
    },
    handleClose() {
      this.$emit('close')
    },
    handleDialogSubmit() {
      const hasNoCheck = this.selectedData.find(item => item.checked.length === 0)
      if (hasNoCheck) {
        this.$message.error('联合统计多方特征需保持一致，请核验')
        return
      }
      for (let i = 0; i < this.selectedData[0].checked.length; i++) {
        const fieldType = this.selectedData[0].resourceField.find(resource => resource.fieldName === this.selectedData[0].checked[i])?.fieldType
        if (fieldType === String || fieldType === Boolean) {
          this.$message({
            message: '联合统计特征类型需为Integer、Double或Long类型，请核验',
            type: 'error',
            duration: 5000
          })
          return
        }
      }
      // same features
      const features = []
      this.selectedData.forEach(item => {
        const posIndex = features.findIndex(v => v.organId === item.organId)
        if (item.checked.length > 0) {
          if (posIndex === -1) {
            features.push(item.checked)
          } else {
            features[posIndex].checked = item.checked
          }
        }
      })
      for (let i = 0; i < features.length; i++) {
        const difference = this.compareFeature(features[i], features[i + 1])
        if (difference) {
          this.$message.error('联合统计多方特征需保持一致,请核验')
          return
        }
      }
      this.$emit('submit', this.selectedData)
    },
    handleChange(data) {
      const { organId, checked } = data
      const posIndex = this.selectedData.findIndex(item => item.organId === organId)
      this.selectedData[posIndex].checked = checked
    },
    compareFeature(arr, arr2) {
      if (!arr2) return
      if (arr.length !== arr2.length) {
        return true
      } else {
        for (let i = 0; i < arr.length; i++) {
          if (!arr2.find(item => item === arr[i])) {
            return true
          } else {
            return false
          }
        }
      }
    }
  }
}
</script>
