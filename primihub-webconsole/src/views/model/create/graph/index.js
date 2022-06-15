import { Graph, Addon, FunctionExt, Shape } from '@antv/x6'
import '@antv/x6-vue-shape'
import './shape'
import { getModelComponent } from '@/api/model'

export default class FlowGraph {
  // public static graph: Graph
  // private static stencil: Addon.Stencil

  static async init() {
    // 获取左侧组件列表
    await this.getModelComponentsInfo()

    const container = document.getElementById('flowContainer')
    this.graph = new Graph({
      container: container,
      width: 600,
      height: 500,
      panning: { // 拖拽平移
        enabled: true,
        eventTypes: ['leftMouseDown', 'mouseWheel']
      },
      grid: {
        size: 10,
        visible: true,
        type: 'mesh',
        args: [
          {
            color: '#cccccc',
            thickness: 1
          },
          {
            color: '#cccccc',
            thickness: 1,
            factor: 4
          }
        ]
      },
      mousewheel: {
        enabled: true,
        modifiers: 'ctrl',
        factor: 1.1,
        maxScale: 1.5,
        minScale: 0.5
      },
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
        router: {
          name: 'manhattan',
          args: {
            padding: 1
          }
        },
        connector: {
          name: 'rounded',
          args: {
            radius: 8
          }
        },
        anchor: 'center',
        connectionPoint: 'anchor',
        allowBlank: false,
        snap: {
          radius: 20
        },
        validateConnection({ targetMagnet }) {
          return !!targetMagnet
        },
        createEdge() {
          return new Shape.Edge({
            attrs: {
              line: {
                stroke: '#A2B1C3',
                strokeWidth: 2,
                targetMarker: {
                  name: 'block',
                  width: 12,
                  height: 8
                }
              }
            },
            zIndex: 0
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
      },
      history: true,
      clipboard: {
        enabled: true
      },
      keyboard: {
        enabled: true
      }
    })
    this.initStencil()
    this.initShape()
    this.initGraphShape()
    this.initEvent()

    this.graph.centerContent()

    this.graph.on('edge:connected', ({ edge }) => {
      edge.attr({
        line: {
          strokeDasharray: ''
        }
      })
    })

    this.graph.on('node:change:data', ({ node }) => {
      const edges = this.graph.getIncomingEdges(node)
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

    return this.graph
  }

  static initStencil() {
    this.stencil = new Addon.Stencil({
      title: '',
      target: this.graph,
      stencilGraphWidth: 260,
      stencilGraphHeight: 600,
      collapsable: true,
      layoutOptions: {
        columns: 1,
        columnWidth: 180,
        rowHeight: 55
      }
    })
    const stencilContainer = document.getElementById('stencil')
    stencilContainer.appendChild(this.stencil.container)
  }

  static initShape() {
    const { graph } = this

    const imageNodes = this.components.map((item) =>
      graph.createNode({
        shape: 'custom-image',
        label: item.componentName,
        data: item
      })
    )

    this.stencil.load(imageNodes)
  }

  static initGraphShape() {
    // this.graph.fromJSON(nodeData)
  }

  static async getModelComponentsInfo() {
    const res = await getModelComponent()
    this.components = res.result
  }

  static showPorts(ports, show) {
    for (let i = 0, len = ports.length; i < len; i = i + 1) {
      ports[i].style.visibility = show ? 'visible' : 'hidden'
    }
  }

  static initEvent() {
    const { graph } = this
    const container = document.getElementById('flowContainer')
    graph.on(
      'node:click',
      FunctionExt.debounce(({ node, view }) => {
        this.selectCellData = node.store.data.data
        this.nodeData = this.selectCellData.componentTypes
        console.log('click', this.nodeData)
        console.log('click2', view)
      }),
      500
    )

    graph.on(
      'node:mouseenter',
      FunctionExt.debounce(() => {
        const ports = container.querySelectorAll(
          '.x6-port-body'
        )
        this.showPorts(ports, true)
      }),
      500
    )
    graph.on('node:mouseleave', () => {
      const ports = container.querySelectorAll(
        '.x6-port-body'
      )
      this.showPorts(ports, false)
    })

    graph.on('node:collapse', ({ node, e }) => {
      e.stopPropagation()
      node.toggleCollapse()
      const collapsed = node.isCollapsed()
      const cells = node.getDescendants()
      cells.forEach((n) => {
        if (collapsed) {
          n.hide()
        } else {
          n.show()
        }
      })
    })
    graph.on('selection:changed', (args) => {
      // this.nodeData = this.selectCellData.componentTypes
      this.nodeData = args.selected[0].store.data.data.componentTypes
      console.log('this.nodeData', this.nodeData)
    })
    graph.bindKey('backspace', () => {
      const cells = graph.getSelectedCells()
      if (cells.length) {
        graph.removeCells(cells)
      }
    })
  }

  // 销毁
  static destroy() {
    this.graph.dispose()
  }
}
