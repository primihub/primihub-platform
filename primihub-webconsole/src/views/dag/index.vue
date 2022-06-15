<template>
  <div class="dag-container">
    <div id="container" />
  </div>
</template>

<script>
/* eslint-disable */
import DagNodeComponent from './components/DagNode'
import { Graph, Path } from '@antv/x6'
import '@antv/x6-vue-shape'
import nodeData from './data.js'

// 初始化节点/边
const init = (data) => {
  const cells = []
  data.forEach((item) => {
    if (item.shape === 'dag-node') {
      cells.push(graph.createNode(item))
    } else {
      cells.push(graph.createEdge(item))
    }
  })
  graph.resetCells(cells)
}

const nodeStatusList = [
  [
    {
      id: '1',
      status: 'running'
    },
    {
      id: '2',
      status: 'default'
    },
    {
      id: '3',
      status: 'default'
    },
    {
      id: '4',
      status: 'default'
    }
  ],
  [
    {
      id: '1',
      status: 'success'
    },
    {
      id: '2',
      status: 'running'
    },
    {
      id: '3',
      status: 'default'
    },
    {
      id: '4',
      status: 'default'
    }
  ],
  [
    {
      id: '1',
      status: 'success'
    },
    {
      id: '2',
      status: 'success'
    },
    {
      id: '3',
      status: 'running'
    },
    {
      id: '4',
      status: 'running'
    }
  ],
  [
    {
      id: '1',
      status: 'success'
    },
    {
      id: '2',
      status: 'success'
    },
    {
      id: '3',
      status: 'success'
    },
    {
      id: '4',
      status: 'failed'
    }
  ]
]

// 显示节点状态
const showNodeStatus = async(statusList) => {
  const status = statusList.shift()
  // console.log({ status })
  status && status.forEach((item) => {
    const { id, status } = item
    const node = graph.getCellById(id)
    const data = node.getData()
    node.setData({
      ...data,
      status: status
    })
  })
  setTimeout(() => {
    showNodeStatus(statusList)
  }, 3000)
}

let graph = null

export default {
  name: 'DagGraph',
  components: {
    // DagNode
  },
  props: {
    width: { default: 800, type: Number },
    height: { default: 600, type: Number },
  },
  data() {
    return {
    }
  },
  mounted() {
    this.initGraph()
    this.initData()
  },
  methods: {
    initGraph() {
      Graph.registerNode(
        'dag-node',
        {
          inherit: 'vue-shape',
          width: 180,
          height: 36,
          component: {
            template: `<dag-node-component />`,
            components: {
              DagNodeComponent
            }
          },
          ports: {
            groups: {
              top: {
                position: 'top',
                attrs: {
                  circle: {
                    r: 4,
                    magnet: true,
                    stroke: '#C2C8D5',
                    strokeWidth: 1,
                    fill: '#fff'
                  }
                }
              },
              bottom: {
                position: 'bottom',
                attrs: {
                  circle: {
                    r: 4,
                    magnet: true,
                    stroke: '#C2C8D5',
                    strokeWidth: 1,
                    fill: '#fff'
                  }
                }
              }
            }
          }
        },
        true
      )
      Graph.registerEdge(
        'dag-edge',
        {
          inherit: 'edge',
          attrs: {
            line: {
              stroke: '#C2C8D5',
              strokeWidth: 1,
              targetMarker: null
            }
          }
        },
        true
      )
      Graph.registerConnector(
        'algo-connector',
        (s, e) => {
          const offset = 4
          const deltaY = Math.abs(e.y - s.y)
          const control = Math.floor((deltaY / 3) * 2)

          const v1 = { x: s.x, y: s.y + offset + control }
          const v2 = { x: e.x, y: e.y - offset - control }

          return Path.normalize(
            `M ${s.x} ${s.y}
            L ${s.x} ${s.y + offset}
            C ${v1.x} ${v1.y} ${v2.x} ${v2.y} ${e.x} ${e.y - offset}
            L ${e.x} ${e.y}
            `
          )
        },
        true
      )

      graph = new Graph({
        container: document.getElementById('container'),
        width: this.width || 800,
        height: this.height || 600,
        panning: {
          enabled: true,
          eventTypes: ['leftMouseDown', 'mouseWheel']
        },
        mousewheel: {
          enabled: true,
          modifiers: 'ctrl',
          factor: 1.1,
          maxScale: 1.5,
          minScale: 0.5
        },
        grid: true,
        highlighting: {
          magnetAdsorbed: {
            name: 'stroke',
            args: {
              attrs: {
                fill: '#fff',
                stroke: '#31d0c6',
                strokeWidth: 4
              }
            }
          }
        },
        connecting: {
          snap: true,
          allowBlank: false,
          allowLoop: false,
          highlight: true,
          connector: 'algo-connector',
          connectionPoint: 'anchor',
          anchor: 'center',
          validateMagnet({ magnet }) {
            return magnet.getAttribute('port-group') !== 'top'
          },
          createEdge() {
            return graph.createEdge({
              shape: 'dag-edge',
              attrs: {
                line: {
                  strokeDasharray: '5 5'
                }
              },
              zIndex: -1
            })
          }
        },
        selecting: {
          enabled: true,
          multiple: true,
          rubberEdge: true,
          rubberNode: true,
          modifiers: 'shift',
          rubberband: true
        }
      })
      console.log({ graph })
      graph.on('edge:connected', ({ edge }) => {
        edge.attr({
          line: {
            strokeDasharray: ''
          }
        })
      })

      graph.on('node:change:data', ({ node }) => {
        const edges = graph.getIncomingEdges(node)
        const { status } = node.getData()
        edges && edges.forEach((edge) => {
          if (status === 'running') {
            edge.attr('line/strokeDasharray', 5)
            edge.attr('line/style/animation', 'running-line 30s infinite linear')
          } else {
            edge.attr('line/strokeDasharray', '')
            edge.attr('line/style/animation', '')
          }
        })
      })
    },
    initData() {
      init(nodeData)
      // console.log('nodeStatusList', nodeStatusList)
      // showNodeStatus(nodeStatusList)
      graph.centerContent()
    }
  }
}
</script>

<style lang="scss">
#container {
  
}
@keyframes running-line {
  to {
    stroke-dashoffset: -1000;
  }
}
@keyframes spin {
  from {
      transform: rotate(0deg);
  }
  to {
      transform: rotate(360deg);
  }
}
</style>
