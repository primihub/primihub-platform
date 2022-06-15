const graphData = {
  'cells': [
    {
      'shape': 'edge',
      'attrs': {
        'line': {
          'stroke': '#A2B1C3',
          'targetMarker': {
            'name': 'block',
            'width': 12,
            'height': 8
          }
        }
      },
      'id': 'f63f39cb-07a9-41d4-b0c4-756b49779f99',
      'zIndex': 0,
      'source': {
        'cell': 'dataAlignment',
        'port': 'port2'
      },
      'target': {
        'cell': 'features',
        'port': 'port1'
      }
    },
    {
      'position': {
        'x': -270,
        'y': -140
      },
      'size': {
        'width': 180,
        'height': 50
      },
      'attrs': {
        'text': {
          'text': '特征筛选'
        }
      },
      'shape': 'dag-node',
      'ports': {
        'groups': {
          'top': {
            'position': 'top',
            'attrs': {
              'circle': {
                'r': 4,
                'magnet': true,
                'stroke': '#5F95FF',
                'strokeWidth': 1,
                'fill': '#fff',
                'style': {
                  'visibility': 'hidden'
                }
              }
            }
          },
          'bottom': {
            'position': 'bottom',
            'attrs': {
              'circle': {
                'r': 4,
                'magnet': true,
                'stroke': '#5F95FF',
                'strokeWidth': 1,
                'fill': '#fff',
                'style': {
                  'visibility': 'hidden'
                }
              }
            }
          }
        },
        'items': [
          {
            'group': 'top',
            'id': 'port1'
          },
          {
            'group': 'bottom',
            'id': 'port2'
          }
        ]
      },
      'id': '85e0f6e0-7df5-4f47-bfbd-ae50e102ca9b',
      'data': {
        'id': 'features',
        'componentCode': 'features',
        'componentName': '特征筛选',
        'complete': true,
        'componentTypes': [
          {
            'typeCode': 'features',
            'typeName': '特征筛选',
            'inputType': 'select',
            'inputValue': '',
            'inputValues': [
              {
                'key': '1',
                'val': '唯一值筛选'
              },
              {
                'key': '2',
                'val': '缺失值比例筛选'
              },
              {
                'key': '3',
                'val': 'IV值筛选'
              },
              {
                'key': '4',
                'val': '相关性筛选'
              },
              {
                'key': '5',
                'val': '用户自定义筛选'
              }
            ]
          }
        ]
      },
      'zIndex': 1
    },
    {
      'position': {
        'x': -250,
        'y': 0
      },
      'size': {
        'width': 180,
        'height': 50
      },
      'attrs': {
        'text': {
          'text': '数据对齐'
        }
      },
      'shape': 'dag-node',
      'ports': {
        'groups': {
          'top': {
            'position': 'top',
            'attrs': {
              'circle': {
                'r': 4,
                'magnet': true,
                'stroke': '#5F95FF',
                'strokeWidth': 1,
                'fill': '#fff',
                'style': {
                  'visibility': 'hidden'
                }
              }
            }
          },
          'bottom': {
            'position': 'bottom',
            'attrs': {
              'circle': {
                'r': 4,
                'magnet': true,
                'stroke': '#5F95FF',
                'strokeWidth': 1,
                'fill': '#fff',
                'style': {
                  'visibility': 'hidden'
                }
              }
            }
          }
        },
        'items': [
          {
            'group': 'in',
            'id': 'port1'
          },
          {
            'group': 'out',
            'id': 'port2'
          }
        ]
      },
      'id': '4dbb4014-00d1-4346-89b5-90a863361050',
      'data': {
        'id': 'dataAlignment',
        'componentCode': 'dataAlignment',
        'componentName': '数据对齐',
        'complete': true,
        'componentTypes': [
          {
            'typeCode': 'projectName',
            'typeName': '所属项目',
            'inputType': 'label',
            'inputValue': '',
            'inputValues': [

            ]
          },
          {
            'typeCode': 'projectId',
            'typeName': '项目id',
            'inputType': 'hidden',
            'inputValue': '',
            'inputValues': [

            ]
          },
          {
            'typeCode': 'modelName',
            'typeName': '模型名称',
            'inputType': 'text',
            'inputValue': '',
            'inputValues': [

            ]
          },
          {
            'typeCode': 'selectData',
            'typeName': '选择数据',
            'inputType': 'select',
            'inputValue': '',
            'inputValues': [

            ]
          },
          {
            'typeCode': 'yField',
            'typeName': 'Y值字段',
            'inputType': 'select',
            'inputValue': '',
            'inputValues': [

            ]
          }
        ]
      },
      'zIndex': 2
    }
  ]
}

export default graphData
