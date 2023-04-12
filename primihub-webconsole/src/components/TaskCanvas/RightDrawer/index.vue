<template>
  <div v-loading="listLoading" class="right-drawer" :class="{'disabled':!options.isEditable}">
    <el-form v-if="nodeData" ref="form" :model="nodeData" :rules="rules" label-width="80px" element-loading-spinner="el-icon-loading">
      <template v-if="isDataSelect">
        <el-form-item>
          <div class="organ-header">
            <p><i class="el-icon-office-building" /> <strong>发起方：</strong>{{ initiateOrgan.organName }}</p>
          </div>
          <el-button v-if="options.isEditable" type="primary" size="small" plain @click="openDialog(initiateOrgan.organId, 1)">选择资源</el-button>
          <ResourceDec v-if="initiateOrgan.resourceId" :disabled="!options.isEditable" :data="initiateOrgan" @change="handleResourceHeaderChange" />
        </el-form-item>
        <el-form-item>
          <template v-if="providerOrganOptions.length>0">
            <el-divider />
            <div class="organ-header">
              <span><i class="el-icon-office-building" /> <strong>协作方</strong></span>
              <div v-if="options.isEditable" class="operation-buttons">
                <el-button icon="el-icon-plus" plain size="mini" @click="openProviderOrganDialog">添加协作方</el-button>
              </div>
            </div>
            <div v-for="(organ,i) in selectedProviderOrgans" :key="i" class="organ">
              <div class="organ-header">
                <p>
                  {{ organ.organName }}
                  <i v-if="options.isEditable" class="el-icon-remove icon-delete" @click="handleProviderRemove(i)" />
                </p>
              </div>
              <el-button v-if="options.isEditable" class="select-button" type="primary" size="mini" plain @click="openDialog(organ.organId,organ.participationIdentity)">选择资源</el-button>
              <ResourceDec v-if="filterData(organ.organId).resourceId" :disabled="!options.isEditable" :data="organ" @change="handleResourceHeaderChange" />
            </div>
          </template>
          <template v-else>
            <i class="el-icon-office-building" /> <span>暂无审核通过的协作方 </span>
          </template>
        </el-form-item>
      </template>
      <template v-else-if="nodeData.componentCode === 'dataAlign' && nodeData.componentTypes[0].inputValue === '2'">
        <el-form-item :label="nodeData.componentTypes[0].typeName">
          <el-select v-model="nodeData.componentTypes[0].inputValue" class="block" placeholder="请选择" @change="handleChange">
            <el-option
              v-for="(v,index) in nodeData.componentTypes[0].inputValues"
              :key="index"
              :label="v.val"
              :value="v.key"
            />
          </el-select>
        </el-form-item>
        <el-row v-if="nodeData.componentCode === 'dataAlign' && nodeData.componentTypes[0].inputValue === '2'" :gutter="20">
          <el-col :span="12">
            <el-button @click="openFeaturesDialog(nodeData.componentCode)">选择特征({{ selectedDataAlignFeatures? 1 : 0 }}/{{ featuresOptions.length }})</el-button>
            <div class="feature-container">
              <el-tag v-if="selectedDataAlignFeatures" type="primary" size="mini">{{ selectedDataAlignFeatures }}</el-tag>
            </div>
            <el-form-item />
          </el-col>
          <el-col :span="12">
            <el-select v-model="nodeData.componentTypes[2].inputValue" class="block" :placeholder="nodeData.componentTypes[2].typeName" @change="handleChange">
              <el-option
                v-for="(v) in nodeData.componentTypes[2].inputValues"
                :key="v.key"
                :label="v.val"
                :value="v.key"
              />
            </el-select>
          </el-col>
        </el-row>
      </template>
      <template v-else-if="nodeData.componentCode === 'exception'">
        <el-form-item :label="nodeData.componentTypes[0].typeName">
          <el-select v-model="nodeData.componentTypes[0].inputValue" class="block" placeholder="请选择" @change="handleChange">
            <el-option
              v-for="(v,index) in nodeData.componentTypes[0].inputValues"
              :key="index"
              :label="v.val"
              :value="v.key"
            />
          </el-select>
        </el-form-item>
      </template>
      <template v-else-if="nodeData.componentCode === 'missing'">
        <el-form-item :label="nodeData.componentTypes[0].typeName">
          <el-select v-model="nodeData.componentTypes[0].inputValue" :disabled="!options.isEditable" class="block" placeholder="请选择" @change="handleChange">
            <el-option
              v-for="(v,index) in nodeData.componentTypes[0].inputValues"
              :key="index"
              :label="v.val"
              :value="v.key"
            />
          </el-select>
        </el-form-item>
        <template v-if="nodeData.componentTypes[0].inputValue === '1'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-button @click="openFeaturesDialog(nodeData.componentCode)">选择特征({{ selectedExceptionFeatures? 1 : 0 }}/{{ featuresOptions.length }})</el-button>
              <div class="feature-container">
                <el-tag v-if="selectedExceptionFeatures" type="primary" size="mini">{{ selectedExceptionFeatures }}</el-tag>
              </div>
              <el-form-item />
            </el-col>
            <el-col :span="12">
              <el-select v-model="nodeData.componentTypes[2].inputValue" :disabled="!options.isEditable" class="block" :placeholder="nodeData.componentTypes[2].typeName" @change="handleChange">
                <el-option
                  v-for="(v) in nodeData.componentTypes[2].inputValues"
                  :key="v.key"
                  :label="v.val"
                  :value="v.key"
                />
              </el-select>
            </el-col>
          </el-row>
        </template>
      </template>
      <template v-else-if="nodeData.componentCode === 'model'">
        <div v-for="(item,n) in nodeData.componentTypes" :key="n">
          <template v-if="item.inputType === 'select' && (!item.parentValue || item.parentValue === '')">
            <p class="component-name"><strong v-if="item.isRequired" class="required">*</strong><span>{{ item.typeName }}</span></p>
            <el-select v-model="item.inputValue" :disabled="!options.isEditable" class="block" placeholder="请选择" @change="handleModelChange">
              <el-option
                v-for="(v,index) in item.inputValues"
                :key="index"
                :label="v.val"
                :value="v.key"
              />
            </el-select>
          </template>
          <template v-if="item.inputType === 'text' && (!item.parentValue || item.parentValue === '')">
            <p class="component-name"><strong v-if="item.isRequired" class="required">*</strong><span>{{ item.typeName }}</span></p>
            <el-input v-model="item.inputValue" :disabled="!options.isEditable" size="small" @change="handleChange" />
          </template>
          <template v-else-if="item.inputType === 'textarea'">
            <p class="component-name"><strong v-if="item.isRequired" class="required">*</strong><span>{{ item.typeName }}</span></p>
            <el-input v-model="item.inputValue" :disabled="!options.isEditable" type="textarea" size="small" @change="handleChange" />
          </template>
        </div>
        <!-- Params part -->
        <template v-if="modelParams">
          <el-row v-for="param in modelParams" :key="param.key">
            <p class="component-name"><strong v-if="param.isRequired" class="required">*</strong><span>{{ param.typeName }}</span></p>
            <el-col v-if="param.inputType === 'number'" :span="12">
              <el-input-number v-model="param.inputValue" :disabled="!options.isEditable" size="mini" :min="filterNumber(param.inputValues,'min')" :max="filterNumber(param.inputValues,'max')" :step="1" step-strictly @change="handleChange" />
            </el-col>
            <el-radio-group v-if="param.inputType === 'radio'" v-model="param.inputValue" @change="handleChange">
              <el-radio v-for="(radio,index) in param.inputValues" :key="index" :disabled="!options.isEditable" :label="radio.val" />
            </el-radio-group>
            <el-col v-if="param.inputType === 'text'" :span="12">
              <el-input v-model="param.inputValue" :disabled="!options.isEditable" size="mini" @change="handleChange" />
            </el-col>
            <el-col v-if="param.inputType === 'select'" :span="12">
              <el-select v-model="param.inputValue" :disabled="!options.isEditable" class="block" placeholder="请选择" @change="handleChange">
                <el-option
                  v-for="v in param.inputValues"
                  :key="v.val"
                  :label="v.key"
                  :value="v.val"
                />
              </el-select>
            </el-col>
            <el-col v-if="param.inputType === 'button'" :span="24">
              <p class="tips">横向联邦需可信第三方(arbiter方)参与</p>
              <span v-if="arbiterOrganName" class="label-text"><i class="el-icon-office-building" /> {{ arbiterOrganName }}</span>
              <el-button v-if="options.isEditable" type="primary" size="mini" class="block" @click="openProviderOrganDialog">请选择</el-button>
            </el-col>
          </el-row>
        </template>
        <el-button v-if="options.isEditable && modelParams && modelParams.find(item => item.typeCode !=='param.arbiterOrgan')" style="margin-top: 10px;" @click="resetModelParams">重置参数</el-button>
      </template>
      <template v-else-if="nodeData.componentCode === 'featuresPoints'">
        <el-form-item :label="nodeData.componentTypes[0].typeName">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-select v-model="nodeData.componentTypes[0].inputValue" class="block" placeholder="请选择" @change="handleChange">
                <el-option
                  v-for="(v,index) in nodeData.componentTypes[0].inputValues"
                  :key="index"
                  :label="v.val"
                  :value="v.key"
                />
              </el-select>
            </el-col>
            <el-col :span="12">
              <el-input-number v-model="nodeData.componentTypes[1].inputValue" controls-position="right" :min="1" :max="100" /> 箱
            </el-col>
          </el-row>
        </el-form-item>
      </template>
      <template v-else>
        <div v-for="(item,index) in nodeData.componentTypes" :key="index">
          <el-form-item :prop="item.typeCode">
            <template v-if="item.inputType === 'label'">
              <p>{{ item.typeName }}</p>
              <span class="label-text">{{ item.inputValue }}</span>
            </template>
            <template v-if="item.inputType === 'text'">
              <p>{{ item.typeName }}</p>
              <el-input v-model="item.inputValue" :disabled="!options.isEditable" size="small" @change="handleChange" />
            </template>
            <template v-if="item.inputType === 'textarea'">
              <p>{{ item.typeName }}</p>
              <el-input v-model="item.inputValue" :disabled="!options.isEditable" type="textarea" size="small" @change="handleChange" />
            </template>
            <template v-if="item.inputType === 'select'">
              <p>{{ item.typeName }}</p>
              <el-select v-model="item.inputValue" :disabled="!options.isEditable" class="block" placeholder="请选择" :value-key="item.typeCode" @change="handleChange">
                <el-option
                  v-for="(v,i) in item.inputValues"
                  :key="i"
                  :label="v.val"
                  :value="v.key"
                />
              </el-select>
            </template>
          </el-form-item>
        </div>
      </template>
    </el-form>
    <!-- add resource dialog -->
    <ModelTaskResourceDialog ref="dialogRef" top="10px" width="800px" :selected-data="selectedResourceId" title="选择资源" :show-tab="participationIdentity === 1" :table-data="resourceList[selectedOrganId]" :visible="dialogVisible" @close="handleDialogCancel" @submit="handleDialogSubmit" />
    <!-- add provider organ dialog -->
    <CooperateOrganDialog v-if="providerOrganDialogVisible" :select-type="selectType" :selected-data="providerOrganIds" :visible.sync="providerOrganDialogVisible" :title="dialogTitle" :data="organData" @submit="handleProviderOrganSubmit" @close="closeProviderOrganDialog" />

    <FeatureSelectDialog v-if="featuresDialogVisible" :visible.sync="featuresDialogVisible" :data="featuresOptions" :has-selected-features="hasSelectedFeatures" :selected-data="selectedFeatures" @submit="handleFeatureDialogSubmit" @close="handleFeatureDialogClose" />
  </div>
</template>

<script>
import { getProjectResourceData, getProjectResourceOrgan } from '@/api/model'
import ModelTaskResourceDialog from '@/components/ModelTaskResourceDialog'
import ResourceDec from '@/components/ResourceDec'
import CooperateOrganDialog from '@/components/CooperateOrganDialog'
import FeatureSelectDialog from '@/components/FeatureSelectDialog'

export default {
  components: {
    ModelTaskResourceDialog,
    ResourceDec,
    CooperateOrganDialog,
    FeatureSelectDialog
  },
  props: {
    graphData: {
      type: Object,
      default: () => {}
    },
    nodeData: {
      type: Object,
      default: () => {}
    },
    options: { // 可配置项
      type: Object,
      default: () => {
        return {
          showSaveButton: false, // 是否展示保存
          isEditable: false // 是否可编辑
        }
      }
    },
    defaultConfig: {
      type: Array,
      default: () => [
        {
          'componentCode': 'start',
          'componentName': '开始',
          'isShow': 0,
          'isMandatory': 0,
          'componentTypes': [
            {
              'typeCode': 'taskName',
              'typeName': '任务名称',
              'inputType': 'text',
              'inputValue': '',
              'isRequired': 1,
              'inputValues': [

              ]
            },
            {
              'typeCode': 'taskDesc',
              'typeName': '任务描述',
              'inputType': 'textarea',
              'inputValue': '',
              'isRequired': 0,
              'inputValues': [

              ]
            }
          ]
        },
        {
          'componentCode': 'dataSet',
          'componentName': '选择数据集',
          'isShow': 0,
          'isMandatory': 0,
          'componentTypes': [
            {
              'typeCode': 'selectData',
              'typeName': '选择数据',
              'inputType': 'none',
              'inputValue': '',
              'isRequired': 1,
              'inputValues': [

              ]
            }
          ]
        },
        {
          'componentCode': 'dataAlign',
          'componentName': '数据对齐',
          'isShow': 0,
          'isMandatory': 1,
          'componentTypes': [
            {
              'typeCode': 'dataAlign',
              'typeName': '数据对齐',
              'inputType': 'select',
              'inputValue': '',
              'isRequired': 0,
              'inputValues': [
                {
                  'key': '1',
                  'val': '样本对齐',
                  'param': null
                },
                {
                  'key': '2',
                  'val': '特征对齐',
                  'param': null
                }
              ]
            },
            {
              'typeCode': 'MultipleSelected',
              'typeName': '可多选特征',
              'inputType': 'none',
              'inputValue': '',
              'isRequired': 0,
              'inputValues': [

              ]
            },
            {
              'typeCode': 'encryption',
              'typeName': '加密方式',
              'inputType': 'none',
              'inputValue': '1',
              'isRequired': 0,
              'inputValues': [
                {
                  'key': '1',
                  'val': 'MD5',
                  'param': null
                }
              ]
            }
          ]
        },
        {
          'componentCode': 'exception',
          'componentName': '异常值处理',
          'isShow': 0,
          'isMandatory': 1,
          'componentTypes': [
            {
              'typeCode': 'exception',
              'typeName': '异常值处理',
              'inputType': 'select',
              'inputValue': '1',
              'isRequired': 0,
              'inputValues': [
                {
                  'key': '1',
                  'val': '异常值处理',
                  'param': null
                }
              ]
            }
          ]
        },
        {
          'componentCode': 'missing',
          'componentName': '缺失值处理',
          'isShow': 0,
          'isMandatory': 1,
          'componentTypes': [
            {
              'typeCode': 'missingValue',
              'typeName': '缺失值处理',
              'inputType': 'select',
              'inputValue': '1',
              'isRequired': 0,
              'inputValues': [
                {
                  'key': '1',
                  'val': '缺失值处理',
                  'param': null
                }
              ]
            },
            {
              'typeCode': 'selectFeatures',
              'typeName': '选择特征',
              'inputType': 'none',
              'inputValue': '',
              'isRequired': 0,
              'inputValues': [

              ]
            },
            {
              'typeCode': 'exceptionType',
              'typeName': '缺失值处理',
              'inputType': 'select',
              'inputValue': '1',
              'isRequired': 0,
              'inputValues': [
                {
                  'key': '1',
                  'val': '平均值',
                  'param': null
                }
              ]
            }
          ]
        },
        {
          'componentCode': 'featureCoding',
          'componentName': '特征编码',
          'isShow': 0,
          'isMandatory': 1,
          'componentTypes': [
            {
              'typeCode': 'featureCoding',
              'typeName': '特征编码',
              'inputType': 'select',
              'inputValue': '',
              'isRequired': 0,
              'inputValues': [
                {
                  'key': '1',
                  'val': '标签编码',
                  'param': null
                },
                {
                  'key': '2',
                  'val': '哈希编码',
                  'param': null
                },
                {
                  'key': '3',
                  'val': '独热编码',
                  'param': null
                },
                {
                  'key': '4',
                  'val': '计数编码',
                  'param': null
                },
                {
                  'key': '5',
                  'val': '直方图编码',
                  'param': null
                },
                {
                  'key': '6',
                  'val': 'WOE编码',
                  'param': null
                },
                {
                  'key': '7',
                  'val': '目标编码',
                  'param': null
                },
                {
                  'key': '8',
                  'val': '平均编码',
                  'param': null
                },
                {
                  'key': '9',
                  'val': '模型编码',
                  'param': null
                }
              ]
            }
          ]
        },
        {
          'componentCode': 'model',
          'componentName': '模型选择',
          'isShow': 0,
          'isMandatory': 0,
          'componentTypes': [
            {
              'typeCode': 'modelType',
              'typeName': '模型选择',
              'inputType': 'select',
              'inputValue': '',
              'isRequired': 1,
              'inputValues': [
                {
                  'key': '2',
                  'val': '纵向-XGBoost',
                  'param': [
                    {
                      'typeCode': 'numTree',
                      'typeName': 'Params.NumTree',
                      'inputType': 'number',
                      'inputValue': '5',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'min',
                          'val': '3',
                          'param': null
                        },
                        {
                          'key': 'max',
                          'val': '10',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'maxDepth',
                      'typeName': 'Params.MaxDepth',
                      'inputType': 'number',
                      'inputValue': '5',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'min',
                          'val': '3',
                          'param': null
                        },
                        {
                          'key': 'max',
                          'val': '10',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'regLambda',
                      'typeName': 'Params.RegLambda',
                      'inputType': 'text',
                      'inputValue': '1',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'min',
                          'val': '1',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'minChildWeight',
                      'typeName': 'Params.MinChildWeight',
                      'inputType': 'number',
                      'inputValue': '3',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'min',
                          'val': '1',
                          'param': null
                        },
                        {
                          'key': 'max',
                          'val': '100',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'isEncrypted',
                      'typeName': 'Params.IsEncrypted',
                      'inputType': 'radio',
                      'inputValue': 'True',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'True',
                          'val': 'True',
                          'param': null
                        },
                        {
                          'key': 'False',
                          'val': 'False',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'mergeGh',
                      'typeName': 'Params.MergeGh',
                      'inputType': 'radio',
                      'inputValue': 'True',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'True',
                          'val': 'True',
                          'param': null
                        },
                        {
                          'key': 'False',
                          'val': 'False',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'rayGroup',
                      'typeName': 'Params.RayGroup',
                      'inputType': 'radio',
                      'inputValue': 'False',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'True',
                          'val': 'True',
                          'param': null
                        },
                        {
                          'key': 'False',
                          'val': 'False',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'sampleType',
                      'typeName': 'Params.SampleType',
                      'inputType': 'select',
                      'inputValue': 'random',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'random',
                          'val': 'random',
                          'param': null
                        },
                        {
                          'key': 'goss',
                          'val': 'goss',
                          'param': null
                        }
                      ]
                    },
                    {
                      'typeCode': 'featureSample',
                      'typeName': 'Params.FeatureSample',
                      'inputType': 'radio',
                      'inputValue': 'True',
                      'isRequired': 0,
                      'inputValues': [
                        {
                          'key': 'True',
                          'val': 'True',
                          'param': null
                        },
                        {
                          'key': 'False',
                          'val': 'False',
                          'param': null
                        }
                      ]
                    }
                  ]
                },
                {
                  'key': '3',
                  'val': '横向-LR',
                  'param': [
                    {
                      'typeCode': 'param.arbiterOrgan',
                      'typeName': '可信第三方选择',
                      'inputType': 'button',
                      'inputValue': '',
                      'isRequired': 1,
                      'inputValues': [

                      ]
                    }
                  ]
                }
              ]
            },
            {
              'typeCode': 'modelName',
              'typeName': '模型名称',
              'inputType': 'text',
              'inputValue': '',
              'isRequired': 1,
              'inputValues': [

              ]
            },
            {
              'typeCode': 'modelDesc',
              'typeName': '模型描述',
              'inputType': 'textarea',
              'inputValue': '',
              'isRequired': 0,
              'inputValues': [

              ]
            }
          ]
        },
        {
          'componentCode': 'assessment',
          'componentName': '评估模型',
          'isShow': 0,
          'isMandatory': 1,
          'componentTypes': [

          ]
        }
      ]
    }
  },
  data() {
    const modelNameValidate = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入模型名称'))
      } else {
        callback()
      }
    }
    const taskNameValidate = (rule, value, callback) => {
      console.log(value)
      if (value === '') {
        callback(new Error('请输入任务名称'))
      } else {
        callback()
      }
    }
    return {
      selectType: 'radio',
      emptyMissingData: {
        selectedExceptionFeatures: [],
        inputValue: '',
        inputValues: [],
        typeName: ''
      },
      exceptionItems: [ // filling items
        {
          selectedExceptionFeatures: '',
          exceptionType: ''
        }
      ],
      selectedFeaturesCode: '',
      selectedFeaturesIndex: '', //  exception component feature select index
      selectedDataAlignFeatures: null,
      selectedExceptionFeatures: null,
      selectedFeatures: null,
      selectedFeatureIndex: -1,
      organData: [],
      arbiterOrganName: '',
      arbiterOrganId: '',
      providerOrganIds: [],
      providerOrganDialogVisible: false,
      featuresDialogVisible: false,
      form: {
        dynamicError: {
          name: ''
        }
      },
      listLoading: false,
      initiateOrgan: {},
      providerOrgans: [],
      providerOrganOptions: [],
      providerOrganId: '',
      providerOrganName: '',
      dialogVisible: false,
      selectedOrganId: '',
      resourceList: [],
      selectedResourceId: '',
      participationIdentity: 2,
      inputValues: [],
      inputValue: '',
      modelTypeValue: '',
      resourceChanged: false,
      rules: {
        modelName: [
          { required: true, trigger: 'blur', validator: modelNameValidate }
        ],
        taskName: [
          { required: true, trigger: 'blur', validator: taskNameValidate }
        ],
        arbiterOrgan: [
          { required: true, trigger: 'change' }
        ]
      },
      selectedProviderOrgans: []
    }
  },
  computed: {
    modelParams: {
      get() {
        const modelType = this.nodeData.componentTypes.find(item => item.typeCode === 'modelType')
        const currentData = modelType.inputValues.find(item => item.key === modelType.inputValue)
        return currentData?.param
      },
      set() {}
    },
    defaultComponentConfig() {
      console.log('this.defaultConfig', this.defaultConfig)
      const model = this.defaultConfig.find(item => item.componentCode === 'model')
      console.log('model', model)
      const currentInputValues = model.componentTypes.find(item => item.typeCode === 'modelType').inputValues
      const param = currentInputValues.find(item => item.key === this.modelTypeValue)?.param
      console.log('defaultComponentConfig param', param)
      return param
    },
    // has selected features collection, A feature can perform only one operation
    hasSelectedFeatures() {
      return this.exceptionItems.map((item, index) => {
        if (item.selectedExceptionFeatures !== '' && this.selectedFeaturesIndex !== index) {
          return item.selectedExceptionFeatures
        }
      })
    },
    isDataSelect() {
      return this.nodeData && this.nodeData.componentCode === 'dataSet'
    },
    isModelSelect() {
      return this.nodeData && this.nodeData.componentCode === 'model'
    },
    showArbiterOrgan() {
      if (this.nodeData && this.nodeData.componentTypes.find(item => item.typeCode === 'modelType')?.inputValue === '3') {
        return true
      } else {
        return false
      }
    },
    featuresOptions() {
      if (this.nodeData.componentCode === 'dataAlign' || this.nodeData.componentCode === 'missing') {
        this.getDataSetComValue(this.graphData)
      }
      if (this.selectedProviderOrgans.length > 0 && this.selectedProviderOrgans[0].fileHandleField && this.initiateOrgan.fileHandleField) {
        let fileHandleField = []
        if (this.selectedProviderOrgans.length > 1) {
          fileHandleField = this.selectedProviderOrgans[0].fileHandleField.concat(this.selectedProviderOrgans[1].fileHandleField)
          fileHandleField = [...new Set(fileHandleField)]
        } else {
          fileHandleField = this.selectedProviderOrgans[0].fileHandleField
        }
        let intersection = fileHandleField.filter(v => this.initiateOrgan.fileHandleField.includes(v))
        intersection = intersection.map((val, key) => {
          return {
            key: key + '',
            val
          }
        })
        return intersection
      } else {
        return []
      }
    },
    dialogTitle() {
      return this.nodeData.componentCode === 'dataSet' ? '添加协作方' : this.nodeData.componentCode === 'model' ? '添加可信第三方' : ''
    }
  },
  watch: {
    async nodeData(newVal) {
      console.log('watch newVal', newVal)
      if (newVal) {
        if (newVal.componentCode === 'dataSet') {
          this.inputValue = newVal.componentTypes[0].inputValue
          if (this.inputValue !== '') {
            this.getDataSetNodeData()
          } else {
            this.initiateOrgan.resourceId = ''
            this.selectedProviderOrgans = []
            this.providerOrganIds = []
            this.selectedResourceId = ''
          }
        } else if (newVal.componentCode === 'model') {
          this.getDataSetComValue(this.graphData)
          const modelTypeIndex = newVal.componentTypes.findIndex(item => item.typeCode === 'modelType')
          this.modelTypeValue = newVal.componentTypes[modelTypeIndex]?.inputValue
          // model select type is H-lr, filter arbiter organ
          if (this.modelTypeValue === '3') {
            const param = newVal.componentTypes[modelTypeIndex].inputValues.find(item => item.key === this.modelTypeValue).param
            this.arbiterOrganId = param.find(item => item.typeCode === 'param.arbiterOrgan').inputValue
            this.arbiterOrganName = this.organs.find(item => item.organId === this.arbiterOrganId)?.organName
          }
        } else if (newVal.componentCode === 'dataAlign') {
          this.selectedDataAlignFeatures = this.nodeData.componentTypes[1]?.inputValue !== '' ? this.nodeData.componentTypes[1]?.inputValue : null
          this.selectedFeatures = this.selectedDataAlignFeatures
        } else if (newVal.componentCode === 'missing') {
          this.selectedExceptionFeatures = newVal.componentTypes[1].inputValue !== '' ? newVal.componentTypes[1].inputValue : null
          this.selectedFeatures = this.selectedExceptionFeatures
          console.log('watch selectedExceptionFeatures', this.selectedExceptionFeatures)
        }
      }
    },
    deep: true,
    immediate: true
  },
  async created() {
    this.projectId = Number(this.$route.query.projectId) || Number(this.$route.params.id)
    await this.getProjectResourceOrgan()
  },
  methods: {
    handleModelChange(val) {
      this.modelTypeValue = val
      // reset before params
      this.resetModelParams()
      this.handleChange()
    },
    // 添加填充策略
    addFilling() {
      this.exceptionItems.push({
        selectedExceptionFeatures: '',
        exceptionType: ''
      })
      this.handleChange('exception')
    },
    removeFilling(index) {
      this.exceptionItems.splice(index, 1)
      this.selectedFeatures = ''
      this.handleChange('exception')
    },
    resetModelParams() {
      this.defaultComponentConfig.forEach((item, index) => {
        this.$set(this.modelParams, index, item)
      })
      // if (param) {
      //   param.map((item, index) => {
      //     this.$set(this.modelParams, index, item)
      //   })
      // }
      console.log('modelParams', this.modelParams)
      this.handleChange()
    },
    filterNumber(data, name) {
      const filterData = data.find(item => item.key === name)
      if (filterData) {
        return Number(filterData.val)
      } else {
        return 1
      }
    },
    filterData(organId) {
      return this.selectedProviderOrgans.find(item => item.organId === organId)
    },
    getDataSetComValue(value) {
      const dataSetCom = value.cells.find(item => item.componentCode === 'dataSet')
      if (dataSetCom) {
        const dataSetComVal = dataSetCom.data.componentTypes[0].inputValue
        this.inputValue = dataSetComVal
        if (this.inputValue !== '') {
          this.getDataSetNodeData()
        }
      }
    },
    async openProviderOrganDialog() {
      if (this.nodeData.componentCode === 'dataSet') {
        // multiple selection
        this.selectType = 'checkbox'
        if (this.selectedProviderOrgans.length === 2) {
          this.$message({
            message: '最多选择2个协作方',
            type: 'error'
          })
          return
        }
        console.log('23e44', this.selectedProviderOrgans)
        console.log('openProviderOrganDialog  222', this.providerOrganIds)
        this.providerOrganDialogVisible = true
        this.organData = this.providerOrganOptions
      } else {
        // single selection
        this.selectType = 'radio'
        if (this.initiateOrgan.organId && this.selectedProviderOrgans.length > 0) {
          const organs = this.selectedProviderOrgans.concat([this.initiateOrgan])
          this.organData = [...this.organs].filter(x => [...organs].every(y => y.organId !== x.organId))
          this.providerOrganDialogVisible = true
        } else {
          this.$message({
            message: '请先选择数据集',
            type: 'warning'
          })
        }
      }
    },
    handleProviderRemove(index) {
      if (this.inputValue !== '') {
        const posIndex = this.inputValue?.findIndex(item => item.organId === this.selectedProviderOrgans[index].organId)
        this.inputValue.splice(posIndex, 1)
        this.nodeData.componentTypes[0].inputValue = JSON.stringify(this.inputValue)
        this.handleChange()
      }

      this.selectedProviderOrgans.splice(index, 1)
      this.providerOrganIds = this.selectedProviderOrgans.map(item => item.organId)
    },
    closeProviderOrganDialog(data) {
      this.providerOrganDialogVisible = false
    },
    handleProviderOrganSubmit(data) {
      if (this.nodeData.componentCode === 'dataSet') {
        // multiple select type
        if (Array.isArray(data)) {
          if (data.length === 0) {
            this.selectedProviderOrgans = []
          } else {
            data.map(item => {
              const index = this.selectedProviderOrgans.findIndex(v => v.organId === item.organId)
              console.log(index)
              if (index === -1) {
                this.selectedProviderOrgans.push(item)
              } else {
                this.selectedProviderOrgans[index] = Object.assign(this.selectedProviderOrgans[index], item)
              }
            })
          }
          this.providerOrganIds = this.selectedProviderOrgans.map(item => item.organId)
          this.setInputValue(this.selectedProviderOrgans)
        } else {
          this.selectedProviderOrgans.push(data)
        }
      } else {
        this.arbiterOrganName = data.organName
        this.arbiterOrganId = data.organId
        if (this.modelParams) {
          const index = this.modelParams.findIndex(item => item.typeCode === 'param.arbiterOrgan')
          this.modelParams[index].inputValue = this.arbiterOrganId
        }
      }

      this.providerOrganDialogVisible = false
      this.$emit('change', this.nodeData)
    },
    getDataSetNodeData() {
      this.inputValue = JSON.parse(this.inputValue)
      const initiateOrgan = this.inputValue?.find(item => item.participationIdentity === 1)
      this.initiateOrgan = initiateOrgan || this.initiateOrgan
      const providerOrgans = this.inputValue?.filter(item => item.participationIdentity === 2)
      if (providerOrgans && providerOrgans.length) {
        this.selectedProviderOrgans = providerOrgans
        this.providerOrganIds = providerOrgans.map(item => item.organId)
      } else {
        this.$set(this.initiateOrgan, 'resourceId', '')
        this.selectedProviderOrgans.splice(0)
      }
    },
    handleResourceHeaderChange(data) {
      this.setInputValue(data)
      this.save()
    },
    async openDialog(organId, participationIdentity) {
      this.participationIdentity = participationIdentity
      this.selectedOrganId = organId
      sessionStorage.setItem('organ', JSON.stringify({ organId: this.selectedOrganId, participationIdentity: this.participationIdentity }))
      if (this.selectedOrganId === '') {
        this.$message({
          message: '请先选择机构',
          type: 'warning'
        })
        return
      }
      if (this.inputValue) {
        const currentOrgan = this.inputValue.find(item => item.organId === organId)
        if (currentOrgan) {
          this.selectedResourceId = currentOrgan.resourceId
        } else {
          this.selectedResourceId = ''
        }
      }
      await this.getProjectResourceData()
      this.dialogVisible = true
    },
    handleChange(name) {
      if (name === 'exception') {
        this.nodeData.componentTypes[1].inputValue = JSON.stringify(this.exceptionItems)
      }
      console.log('handleChange defaultConfig', this.defaultComponentConfig)
      this.$emit('change', this.nodeData)
    },
    handleProviderOrganChange(value) {
      this.providerOrganId = value
      this.providerOrganName = this.providerOrganOptions.filter(item => item.organId === value)[0].organName
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      // not selecting resource
      if (!data.resourceId) {
        this.dialogVisible = false
        return
      }
      if (data.fileHandleField.includes('id')) {
        data.fileHandleField = data.fileHandleField.filter(v => v !== 'id')
      }
      if (this.participationIdentity === 1) {
        // is not first select
        if ('resourceId' in this.initiateOrgan && this.initiateOrgan.resourceId !== data.resourceId) {
          this.resourceChanged = true
        }
        data.organName = this.initiateOrgan.organName
        this.initiateOrgan = []
        this.initiateOrgan = data
      } else {
        // is not first select
        if (this.selectedProviderOrgans.length > 0 && 'resourceId' in this.selectedProviderOrgans[0]) {
          this.resourceChanged = true
        }
        const posIndex = this.selectedProviderOrgans.findIndex(organ => organ.organId === data.organId)
        data.organName = this.selectedProviderOrgans[posIndex].organName
        this.selectedProviderOrgans[posIndex] = data
      }
      this.selectedResourceId = data.resourceId
      // set input value
      this.setInputValue(data)
      this.save()
      this.dialogVisible = false
      this.$emit('change', this.nodeData)
    },
    setInputValue(data) {
      if (this.inputValue) {
        this.inputValues = this.inputValue
      }
      if (data.length) {
        data.forEach(item => {
          // set default feature value
          item.calculationField = item.calculationField ? item.calculationField : item.fileHandleField ? item.fileHandleField : ''
          const posIndex = this.inputValues.findIndex(v => item.organId === v.organId)
          if (posIndex !== -1) {
            this.inputValues.splice(posIndex, 1, item)
          } else {
            this.inputValues.push(item)
          }
        })
      } else {
        // set default feature value
        data.calculationField = data.calculationField ? data.calculationField : data.fileHandleField ? data.fileHandleField : ''
        const posIndex = this.inputValues.findIndex(item => item.organId === data.organId)
        const currentData = data
        if (posIndex !== -1) {
          this.inputValues.splice(posIndex, 1, currentData)
        } else {
          this.inputValues.push(currentData)
        }
      }
      this.nodeData.componentTypes[0].inputValue = JSON.stringify(this.inputValues)
      this.handleChange()
    },
    async getProjectResourceData() {
      this.resourceList = []
      const params = {
        projectId: this.projectId,
        organId: this.selectedOrganId
      }
      const { code, result } = await getProjectResourceData(params)
      if (code === 0) {
        this.resourceList[this.selectedOrganId] = result
      }
    },
    async getProjectResourceOrgan() {
      this.listLoading = true
      const res = await getProjectResourceOrgan({ projectId: this.projectId })
      if (res.code === 0) {
        this.organs = res.result
        this.providerOrganOptions = this.organs.filter(item => item.participationIdentity === 2)
        this.providerOrgans = this.organs.filter(item => item.participationIdentity === 2)
        this.initiateOrgan = this.organs.filter(item => item.participationIdentity === 1)[0]
      }
      this.listLoading = false
    },
    save() {
      this.$emit('save')
    },
    openFeaturesDialog(code, index) {
      this.selectedFeaturesCode = code
      if (this.selectedFeaturesCode === 'dataAlign') {
        this.selectedFeatures = this.selectedDataAlignFeatures
      } else if (this.selectedFeaturesCode === 'exception') {
        this.selectedFeaturesIndex = index
        this.selectedFeatures = this.exceptionItems[this.selectedFeaturesIndex].selectedExceptionFeatures
      }
      this.featuresDialogVisible = true
    },
    handleFeatureDialogSubmit(data) {
      if (this.selectedFeaturesCode === 'dataAlign') {
        this.selectedDataAlignFeatures = data
        this.nodeData.componentTypes[1].inputValue = this.selectedDataAlignFeatures
        this.selectedFeatures = this.selectedDataAlignFeatures
      } else if (this.selectedFeaturesCode === 'missing') {
        this.selectedExceptionFeatures = data
        this.nodeData.componentTypes[1].inputValue = this.selectedExceptionFeatures
        this.selectedFeatures = this.selectedExceptionFeatures
        console.log(this.selectedExceptionFeatures)
      }
      this.featuresDialogVisible = false
      this.handleChange(this.selectedFeaturesCode)
    },
    handleFeatureDialogClose() {
      this.featuresDialogVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
p {
  margin-block-start: .2em;
  margin-block-end: .2em;
}
::v-deep .el-select .el-input__inner{
  padding: 12px 10px!important;
}
::v-deep .el-button--mini.is-circle{
  padding: 3px;
  width: 20px;
  height: 20px;
}
::v-deep .el-form-item__content{
  display: block;
  margin-left: 0!important;
  width: 100%;
}
::v-deep .el-form-item__label{
  float: none;
}
::v-deep .el-input-number{
  box-sizing: border-box;
  width: calc(100% - 20px)
}

.select{
  width: 100%;
}
.right-drawer {
  width: 300px;
  background: #fff;
  padding: 10px;
  max-height: 800px;
  overflow-y: scroll;
}
.label-text{
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  display: inline-block;
}
::v-deep .detail-title{
  width: 100px;
}
::v-deep .el-descriptions .is-bordered .el-descriptions-item__cell{
  padding:2px 5px!important;
}
::v-deep .el-checkbox__label{
  font-size: 12px!important;
}
.component-name{
  line-height: 40px;
  span{
    font-size: 14px;
    display: inline-block;
    vertical-align: middle;
  }
}
.resource-data{
  font-size: 12px;
  margin-top: 10px;
}
.organ-select{
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.required{
  color: red;
  margin-right: 5px;
  height: 35px;
  display: inline-block;
  vertical-align: middle;
}
.not-clickable{
  cursor: default;
  pointer-events: none;
}
.tips{
  font-size: 12px;
  color: #999;
  line-height: 1;
  margin-bottom: 10px;
}
.block{
  width: 100%;
  display: block;
}
.organ-header{
  display: flex;
  align-items: center;
  justify-content: space-between;
  line-height: 20px;
  margin: 10px 0;
}
.icon-delete{
  color: #F56C6C;
}

</style>
