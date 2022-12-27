<template>
  <div>
    <h3>字段信息</h3>
    <el-table
      :data="data"
      class="table-list"
      v-bind="$attrs"
    >
      <el-table-column align="center" label="字段名称" prop="fieldName" />
      <el-table-column align="center" label="数据类型" prop="fieldType">
        <template slot-scope="{row}">
          <el-select v-model="row.fieldType" size="mini" placeholder="请选择" :disabled="!isEditable" @change="handleChange(row)">
            <el-option
              v-for="item in fieldTypeList"
              :key="item.value"
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
  </div>

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
        value: 0,
        label: 'String'
      }, {
        value: 1,
        label: 'Integer'
      }, {
        value: 2,
        label: 'Double'
      }, {
        value: 3,
        label: 'Long'
      }, {
        value: 4,
        label: 'Enum'
      }, {
        value: 5,
        label: 'Boolean'
      }],
      params: [],
      fieldType: ''
    }
  },
  methods: {
    handleChange(row) {
      this.data.fieldType = row.fieldType
      if (this.isEditPage) {
        this.updateDataResourceField(row)
        this.$emit('change', this.data)
        return
      } else {
        this.$emit('change', this.data)
      }
    },
    updateDataResourceField(row) {
      const { fieldId, fieldName, fieldType, fieldDesc = '', relevance } = row
      const data = {
        fieldId,
        fieldName,
        fieldType,
        fieldDesc,
        relevance: relevance === true ? 1 : 0
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
