export default [
  {
    'id': 'ziyuanjihe',
    'shape': 'dag-node',
    'x': 0,
    'y': 0,
    'data': {
      'label': '资源集合',
      'status': 'running'
    },
    'ports': [
      {
        'id': 'ziyuanjihe-bottom-1',
        'group': 'bottom'
      }
    ]
  },
  {
    'id': 'shujuduiqi',
    'shape': 'dag-node',
    'x': 0,
    'y': 100,
    'data': {
      'label': '数据对齐',
      'status': 'success'
    },
    'ports': [
      {
        'id': 'shujuduiqi-top-1',
        'group': 'top'
      },
      {
        'id': 'shujuduiqi-bottom-1',
        'group': 'bottom'
      }
    ]
  },
  {
    'id': 'tezhengshaixuan',
    'shape': 'dag-node',
    'x': 0,
    'y': 200,
    'data': {
      'label': '特征筛选',
      'status': 'success'
    },
    'ports': [
      {
        'id': 'tezhengshaixuan-top-1',
        'group': 'top'
      },
      {
        'id': 'tezhengshaixuan-bottom-1',
        'group': 'bottom'
      }
    ]
  },
  {
    'id': 'yichangchuli',
    'shape': 'dag-node',
    'x': 0,
    'y': 300,
    'data': {
      'label': '异常处理',
      'status': 'success'
    },
    'ports': [
      {
        'id': 'yichangchuli-top-1',
        'group': 'top'
      },
      {
        'id': 'yichangchuli-bottom-1',
        'group': 'bottom'
      }
    ]
  },
  {
    'id': 'tezhengbianma',
    'shape': 'dag-node',
    'x': 0,
    'y': 400,
    'data': {
      'label': '特征编码',
      'status': 'success'
    },
    'ports': [
      {
        'id': 'tezhengbianma-top-1',
        'group': 'top'
      }
    ]
  },
  {
    'id': '5',
    'shape': 'dag-edge',
    'source': {
      'cell': 'ziyuanjihe',
      'port': 'ziyuanjihe-bottom-1'
    },
    'target': {
      'cell': 'shujuduiqi',
      'port': 'shujuduiqi-top-1'
    },
    'zIndex': 0
  },
  {
    'id': '6',
    'shape': 'dag-edge',
    'source': {
      'cell': 'shujuduiqi',
      'port': 'shujuduiqi-bottom-1'
    },
    'target': {
      'cell': 'tezhengshaixuan',
      'port': 'tezhengshaixuan-top-1'
    },
    'zIndex': 0
  },
  {
    'id': '7',
    'shape': 'dag-edge',
    'source': {
      'cell': 'tezhengshaixuan',
      'port': 'tezhengshaixuan-bottom-1'
    },
    'target': {
      'cell': 'yichangchuli',
      'port': 'yichangchuli-top-1'
    },
    'zIndex': 0
  },
  {
    'id': '8',
    'shape': 'dag-edge',
    'source': {
      'cell': 'yichangchuli',
      'port': 'yichangchuli-bottom-1'
    },
    'target': {
      'cell': 'tezhengbianma',
      'port': 'tezhengbianma-top-1'
    },
    'zIndex': 0
  }
]
