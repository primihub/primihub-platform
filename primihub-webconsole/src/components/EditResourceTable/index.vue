<template>
  <el-table
    :data="data"
    class="table-list"
    v-bind="$attrs"
  >
    <el-table-column align="center" label="字段名称" prop="fieldName" />
    <el-table-column align="center" label="数据类型" prop="fieldType">
      <template slot-scope="{row}">
        <el-select v-model="row.fieldType" placeholder="请选择" :disabled="!isEditable" @change="handleChange(row)">
          <el-option
            v-for="(item,index) in fieldTypeList"
            :key="index"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="关联键" prop="relevance" align="center" width="60">
      <template slot-scope="{row}">
        <el-checkbox v-model="row.relevance" :disabled="!isEditable" @change="handleChange(row)" />
      </template>
    </el-table-column>
    <el-table-column label="分组键" prop="grouping" align="center" width="60">
      <template slot-scope="{row}">
        <el-checkbox v-model="row.grouping" :disabled="!isEditable" @change="handleChange(row)" />
      </template>
    </el-table-column>
    <el-table-column label="保护开关" prop="protectionStatus" align="center" width="80">
      <template slot-scope="{row}">
        <el-switch
          v-model="row.protectionStatus"
          :disabled="!isEditable"
          active-color="#13ce66"
          inactive-color="#ccc"
          @change="handleChange(row)"
        />
      </template>
    </el-table-column>
    <el-table-column align="center" label="描述" prop="fieldDesc">
      <template slot-scope="{row}">
        <el-input
          v-model="row.fieldDesc"
          :disabled="!isEditable"
          size="mini"
          @change="handleChange(row)"
        />
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import { updateDataResourceField } from '@/api/resource'

export default {
  name: 'EditResourceTable',
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      },
      required: true
    },
    isEditable: { // if not editable, parent component must set it false
      type: Boolean,
      default: true
    },
    isEditPage: { // if is not edit page, don not call updateDataResourceField Api
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      tableLoading: false,
      fieldTypeList: [{
        value: 1,
        label: 'Integer'
      }, {
        value: 2,
        label: 'Long'
      }, {
        value: 3,
        label: 'Double'
      }, {
        value: 4,
        label: 'Enum'
      }, {
        value: 5,
        label: 'String'
      }],
      params: []
    }
  },
  methods: {
    handleChange(row) {
      if (this.isEditPage) {
        this.updateDataResourceField(row)
        this.$emit('change', this.data)
        return
      } else {
        this.$emit('change', this.data)
      }
    },
    updateDataResourceField(row) {
      const { fieldId, fieldName, fieldType, fieldDesc = '', relevance, grouping, protectionStatus } = row
      const data = {
        fieldId,
        fieldName,
        fieldType,
        fieldDesc,
        relevance: relevance === true ? 1 : 0,
        grouping: grouping === true ? 1 : 0,
        protectionStatus: protectionStatus === true ? 1 : 0
      }
      updateDataResourceField(data).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '设置成功',
            type: 'success'
          })
        }
      })
    }
  }

}
</script>

<style lang="scss" scoped>
::v-deep .el-table .el-table__cell{
  padding: 5px 0;
}
</style>
