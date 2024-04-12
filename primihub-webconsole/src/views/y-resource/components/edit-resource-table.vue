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
      num: 12,
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
        value: 5,
        label: 'Boolean'
      }]
    }
  },
  methods: {
    handleChange(row) {
      const fieldType = typeof row.fieldType === 'number' ? this.fieldTypeList.find(item => item.value === row.fieldType)?.label : row.fieldType
      row.fieldType = fieldType
      if (this.isEditPage) {
        this.updateDataResourceField(row)
        this.$emit('change', this.data)
        return
      } else {
        this.$emit('change', this.data)
      }
    },
    async updateDataResourceField(row) {
      const { fieldId, fieldName, fieldType, fieldDesc = '' } = row
      const data = {
        fieldId,
        fieldName,
        fieldType,
        fieldDesc
      }
      const { code } = await updateDataResourceField(data)
      if (code === 0) this.$message.success('设置成功')
    }
  }

}
</script>

<template>
  <div>
    <el-table
      :data="data"
      class="table-list"
      v-bind="$attrs"
    >
      <el-table-column align="center" label="字段名称" prop="fieldName" />
      <el-table-column align="center" label="字段中文名" prop="fieldDesc">
        <template slot-scope="{row}">
          <el-input
            v-model="row.fieldDesc"
            :disabled="!isEditable"
            size="mini"
            :maxlength="12"
            placeholder="请输入字段中文名"
            show-word-limit
            @change="handleChange(row)"
          />
        </template>
      </el-table-column>
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
      <el-table-column align="center" label="分布类型" prop="fieldType">
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
      <el-table-column align="center" label="字段描述" prop="fieldDesc">
        <template slot-scope="{row}">
          <el-input
            v-model="row.fieldDesc"
            :disabled="!isEditable"
            size="mini"
            :maxlength="12"
            placeholder="请输入描述"
            show-word-limit
            @change="handleChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column align="center" label="字段长度" prop="fieldType">
        <template slot-scope="{row}">
          {{ row.fieldDesc }}
          <el-input v-model="num" type="number" size="mini" :disabled="!isEditable" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="字段精度" prop="fieldType">
        <template slot-scope="{row}">
          {{ row.fieldDesc }}
          <el-input v-model="num" type="number" size="mini" :disabled="!isEditable" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="是否主键" prop="fieldType">
        <template slot-scope="{row}">
          <el-switch v-model="row.relevance" :disabled="!isEditable" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="设为Y值" prop="fieldType">
        <template slot-scope="{row}">
          <el-switch v-model="row.relevance" :disabled="!isEditable" />
        </template>
      </el-table-column>
    </el-table>
  </div>

</template>
