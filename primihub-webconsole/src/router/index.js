import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true,
    meta: { title: '登录页' }
  },
  {
    path: '/auth',
    component: () => import('@/views/auth/index'),
    hidden: true,
    meta: { title: '授权页' }
  },
  {
    path: '/register',
    component: () => import('@/views/register/index'),
    hidden: true,
    meta: { title: '注册页' }
  },
  {
    path: '/forgotPwd',
    component: () => import('@/views/forgotPwd'),
    hidden: true,
    meta: { title: '忘记密码' }
  },
  {
    path: '/updatePwd',
    component: () => import('@/views/updatePwd'),
    hidden: true,
    name: '更新密码',
    meta: { title: '更新密码' }
  },
  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true,
    meta: { title: 'not found' }
  },
  {
    path: '/applicationIndex',
    name: 'applicationIndex',
    hidden: true,
    component: () => import('@/views/applicationMarket'),
    meta: { title: '应用市场' }
  },
  {
    path: '/bigModel',
    name: 'BigModel',
    hidden: true,
    component: () => import('@/views/bigModel/index'),
    meta: { title: 'PrimiHub隐私计算大模型' }
  },
  {
    path: '/applicationIndex/detail/:name',
    name: 'ApplicationDetail',
    hidden: true,
    component: () => import('@/views/applicationMarket/detail'),
    meta: { title: '应用详情' }
  },
  {
    path: '/applicationIndex/application/:name',
    name: 'Application',
    hidden: true,
    component: () => import('@/views/applicationMarket/application'),
    meta: { title: '应用页' }
  },
  {
    path: '/map',
    component: Layout,
    name: 'Map',
    hidden: true,
    redirect: '/map/index',
    meta: { title: '地图' },
    children: [
      {
        path: 'index',
        name: 'mapIndex',
        meta: { title: '地图', breadcrumb: false },
        component: () => import('@/views/map/index')
      }
    ]
  },
  {
    path: '/',
    component: Layout,
    redirect: '/map/index'
  }
]

export const asyncRoutes = [
  {
    path: '/privateSearch',
    component: Layout,
    name: 'PrivateSearch',
    redirect: '/privateSearch/list',
    meta: { title: '隐匿查询', icon: 'el-icon-search' },
    children: [{
      path: 'list',
      name: 'PrivateSearchList',
      component: () => import('@/views/privateSearch/index'),
      meta: { title: '隐匿查询', breadcrumb: false }
    }, {
      path: 'task',
      name: 'PIRTask',
      hidden: true,
      component: () => import('@/views/privateSearch/task'),
      meta: {
        title: '隐匿查询任务',
        activeMenu: '/privateSearch/list',
        parent: { name: 'PrivateSearchList' }
      }
    }, {
      path: 'detail/:id',
      name: 'PIRDetail',
      component: () => import('@/views/privateSearch/detail'),
      meta: {
        title: '任务详情',
        activeMenu: '/privateSearch/list'
      },
      hidden: true
    }]
  },
  {
    path: '/PSI',
    component: Layout,
    name: 'PSI',
    redirect: '/PSI/list',
    meta: { title: '隐私求交', icon: 'el-icon-lock' },
    children: [
      {
        path: 'task',
        name: 'PSITask',
        component: () => import('@/views/PSI/task'),
        meta: {
          title: '求交任务',
          activeMenu: '/PSI/list'
        },
        hidden: true
      }, {
        path: 'list',
        name: 'PSIList',
        component: () => import('@/views/PSI/list'),
        meta: { title: '隐私求交', breadcrumb: false }
      }, {
        path: 'detail/:id',
        name: 'PSIDetail',
        component: () => import('@/views/PSI/detail'),
        meta: {
          title: '任务详情',
          activeMenu: '/PSI/list'
        },
        hidden: true
      }]
  },
  {
    path: '/Difference',
    component: Layout,
    name: 'Difference',
    redirect: '/Difference/list',
    meta: { title: '联邦求差', icon: 'el-icon-minus' },
    hidden: true,
    children: [
      {
        path: 'list',
        name: 'DifferenceList',
        component: () => import('@/views/Difference/list'),
        meta: { title: '联邦求差', breadcrumb: false }
      }
    ]
  },
  {
    path: '/Union',
    component: Layout,
    name: 'Union',
    redirect: '/Union/list',
    meta: { title: '联邦求并', icon: 'el-icon-plus' },
    hidden: true,
    children: [
      {
        path: 'list',
        name: 'UnionList',
        component: () => import('@/views/Union/list'),
        meta: { title: '联邦求并', breadcrumb: false }
      }
    ]
  },
  {
    path: '/FederatedLearning',
    component: Layout,
    name: 'FederatedLearning',
    redirect: '/FederatedLearning/list',
    meta: { title: '联邦学习', icon: 'el-icon-data-analysis' },
    hidden: true,
    children: [
      {
        path: 'list',
        name: 'FederatedLearningList',
        component: () => import('@/views/FederatedLearning/list'),
        meta: { title: '联邦学习', breadcrumb: false }
      }
    ]
  },
  {
    path: '/project',
    name: 'Project',
    component: Layout,
    redirect: '/project/list',
    meta: { icon: 'el-icon-menu', title: '项目管理' },
    children: [
      {
        path: 'list',
        name: 'ProjectList',
        meta: { icon: 'el-icon-menu', title: '项目管理', breadcrumb: false },
        component: () => import('@/views/project/list')
      },
      {
        path: 'create',
        name: 'ProjectCreate',
        meta: {
          title: '新建项目'
        },
        hidden: true,
        component: () => import('@/views/project/create')
      },
      {
        path: 'detail/:id',
        name: 'ProjectDetail',
        meta: {
          title: '项目详情',
          activeMenu: '/project/list'
        },
        hidden: true,
        component: () => import('@/views/project/detail')
      },
      {
        path: 'detail/:id/createTask',
        name: 'ModelCreate',
        meta: {
          title: '创建任务',
          activeMenu: '/project/list',
          parent: { name: 'ProjectDetail' }
        },
        hidden: true,
        component: () => import('@/views/model/create/index')
      },
      {
        path: 'detail/:id/task/:taskId',
        name: 'ModelTaskDetail',
        meta: {
          title: '任务详情',
          activeMenu: '/project/list',
          parent: { name: 'ProjectDetail' }
        },
        hidden: true,
        component: () => import('@/views/project/taskDetail')
      }
    ]
  },
  {
    path: '/model',
    component: Layout,
    name: 'Model',
    redirect: '/model/list',
    meta: { title: '模型管理', icon: 'el-icon-files' },
    children: [
      {
        path: 'list',
        name: 'ModelList',
        component: () => import('@/views/model/list'),
        meta: { title: '模型管理', breadcrumb: false }
      },
      {
        path: 'detail/:id',
        name: 'ModelDetail',
        meta: {
          title: '模型详情',
          activeMenu: '/model/list'
        },
        hidden: true,
        component: () => import('@/views/model/detail')
      }
    ]
  },
  {
    path: '/reasoning',
    component: Layout,
    name: 'ModelReasoning',
    redirect: '/reasoning/list',
    meta: { title: '服务管理', icon: 'el-icon-aim' },
    children: [
      {
        path: 'list',
        name: 'ModelReasoningList',
        component: () => import('@/views/reasoning/list'),
        meta: { title: '服务管理', breadcrumb: false }
      },
      {
        path: 'task',
        name: 'ModelReasoningTask',
        hidden: true,
        component: () => import('@/views/reasoning/task'),
        meta: {
          title: '模型推理任务',
          activeMenu: '/reasoning/list'
        }
      },
      {
        path: 'detail/:id',
        name: 'ModelReasoningDetail',
        meta: {
          title: '模型推理详情',
          activeMenu: '/reasoning/list'
        },
        hidden: true,
        component: () => import('@/views/reasoning/detail')
      }
    ]
  },
  {
    path: '/resource',
    component: Layout,
    name: 'ResourceMenu',
    redirect: '/resource/list',
    meta: { title: '资源管理', icon: 'el-icon-s-operation' },
    children: [
      {
        path: 'list',
        name: 'ResourceList',
        component: () => import('@/views/resource/list'),
        meta: { title: '我的资源' }
      },
      {
        path: 'unionList',
        name: 'UnionList',
        component: () => import('@/views/resource/unionList'),
        meta: { title: '协作方资源' }
      },
      {
        path: 'availableResources',
        name: 'AvailableResources',
        component: () => import('@/views/resource/availableResources'),
        meta: { title: '可申请的资源' }
      },
      {
        path: 'derivedDataList',
        name: 'DerivedDataList',
        component: () => import('@/views/resource/derivedDataList'),
        meta: { title: '衍生数据资源' }
      },
      {
        path: 'create',
        name: 'ResourceUpload',
        hidden: true,
        component: () => import('@/views/resource/create'),
        meta: { title: '新建资源', activeMenu: '/resource/list' }
      },
      {
        path: 'edit/:id',
        name: 'ResourceEdit',
        hidden: true,
        component: () => import('@/views/resource/create'),
        meta: { title: '编辑资源', activeMenu: '/resource/list' }
      },
      {
        path: 'detail/:id',
        name: 'ResourceDetail',
        meta: {
          title: '资源详情',
          activeMenu: '/resource/list'
        },
        hidden: true,
        component: () => import('@/views/resource/detail')
      },
      {
        path: 'unionResourceDetail/:id',
        name: 'UnionResourceDetail',
        meta: {
          title: '协作方资源详情',
          activeMenu: '/resource/unionList'
        },
        hidden: true,
        component: () => import('@/views/resource/unionResourceDetail')
      },
      {
        path: 'derivedDataResourceDetail/:id',
        name: 'DerivedDataResourceDetail',
        meta: {
          title: '衍生数据资源详情',
          activeMenu: '/resource/derivedDataList'
        },
        hidden: true,
        component: () => import('@/views/resource/derivedDataResourceDetail')
      },
      {
        path: 'requirementList',
        name: 'DataRequirementList',
        component: () => import('@/views/resource/requirementList'),
        meta: { title: '数据需求列表' }
      },
      {
        path: 'requirementConfig',
        name: 'DataRequirementConfig',
        component: () => import('@/views/resource/requirementConfig'),
        meta: { title: '数据需求配置' }
      },
      {
        path: 'requirementMatch',
        name: 'DataRequirementMatch',
        component: () => import('@/views/resource/requirementMatch'),
        meta: { title: '匹配数据需求所需数据' }
      }

    ]
  },
  {
    path: '/setting',
    component: Layout,
    name: 'Setting',
    redirect: '/setting/user',
    meta: { title: '系统设置', icon: 'el-icon-s-tools' },
    children: [
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/setting/user'),
        meta: { title: '用户管理' }
      },
      {
        path: 'role',
        name: 'RoleManage',
        component: () => import('@/views/setting/role'),
        meta: { title: '角色管理' }
      },
      {
        path: 'center',
        name: 'CenterManage',
        component: () => import('@/views/setting/center'),
        meta: { title: '节点管理' }
      },
      {
        path: 'accessManagement',
        name: 'AccessManagement',
        component: () => import('@/views/setting/accessManagement'),
        meta: { title: '接入方管理' }
      },
      {
        path: 'cooperation',
        name: 'CooperationManagement',
        component: () => import('@/views/setting/cooperation'),
        meta: { title: '合作方管理' }
      },
      {
        path: 'approval',
        name: 'ApprovalWorkflow',
        component: () => import('@/views/setting/approval'),
        meta: { title: '审批工作流' }
      },
      {
        path: 'dataExchange',
        name: 'DataExchangeLog',
        component: () => import('@/views/setting/dataExchange'),
        meta: { title: '数据交换日志' }
      },
      {
        path: 'system',
        name: 'SystemConfig',
        component: () => import('@/views/setting/system'),
        meta: { title: '系统配置' }
      },
      {
        path: 'ui',
        name: 'UISetting',
        component: () => import('@/views/setting/ui'),
        meta: { title: '界面设置' },
        hidden: true
      }
    ]
  },
  {
    path: '/whitelist',
    component: Layout,
    name: 'Whitelist',
    redirect: '/whitelist/list',
    meta: { title: '白名单管理', icon: 'el-icon-s-check' },
    children: [
      {
        path: 'list',
        name: 'WhitelistList',
        component: () => import('@/views/whitelist/list'),
        meta: { title: '白名单列表' }
      },
      {
        path: 'config',
        name: 'WhitelistConfig',
        component: () => import('@/views/whitelist/config'),
        meta: { title: '白名单配置' }
      },
      {
        path: 'accessLog',
        name: 'WhitelistAccessLog',
        component: () => import('@/views/whitelist/accessLog'),
        meta: { title: '访问日志记录' }
      }
    ]
  },
  {
    path: '/tenant',
    component: Layout,
    name: 'Tenant',
    redirect: '/tenant/list',
    meta: { title: '租户管理', icon: 'el-icon-office-building' },
    children: [
      {
        path: 'list',
        name: 'TenantList',
        component: () => import('@/views/tenant/list'),
        meta: { title: '租户列表' }
      },
      {
        path: 'resource/:id',
        name: 'TenantResource',
        component: () => import('@/views/tenant/resource'),
        meta: {
          title: '资源分配',
          activeMenu: '/tenant/list'
        },
        hidden: true
      }
    ]
  },
  {
    path: '/federatedSql',
    component: Layout,
    name: 'FederatedSql',
    redirect: '/federatedSql/query',
    meta: { title: '联邦SQL查询', icon: 'el-icon-data-analysis' },
    children: [{
      path: 'query',
      name: 'FederatedSqlQuery',
      component: () => import('@/views/federatedSql/index'),
      meta: { title: '联邦SQL查询', breadcrumb: false }
    }]
  },
  {
    path: '/evidence',
    component: Layout,
    name: 'Evidence',
    redirect: '/evidence/query',
    meta: { title: '存证管理', icon: 'el-icon-document-checked' },
    children: [
      {
        path: 'query',
        name: 'EvidenceQuery',
        component: () => import('@/views/evidence/query'),
        meta: { title: '存证查询' }
      },
      {
        path: 'timestamp',
        name: 'EvidenceTimestamp',
        component: () => import('@/views/evidence/timestamp'),
        meta: { title: '时间戳管理' }
      },
      {
        path: 'config',
        name: 'EvidenceConfig',
        component: () => import('@/views/evidence/config'),
        meta: { title: '存证配置' }
      },
      {
        path: 'export',
        name: 'EvidenceExport',
        component: () => import('@/views/evidence/export'),
        meta: { title: '存证加密导出' }
      },
      {
        path: 'api',
        name: 'EvidenceApi',
        component: () => import('@/views/evidence/api'),
        meta: { title: '存证接口对接' }
      }
    ]
  },
  {
    path: '/monitor',
    component: Layout,
    name: 'Monitor',
    redirect: '/monitor/index',
    meta: { title: '监控管理', icon: 'el-icon-data-line' },
    children: [{
      path: 'index',
      name: 'MonitorIndex',
      component: () => import('@/views/monitor/index'),
      meta: { title: '监控管理', breadcrumb: false }
    }]
  },
  {
    path: '/api',
    component: Layout,
    name: 'ApiManage',
    redirect: '/api/list',
    meta: { title: '接口管理', icon: 'el-icon-connection' },
    children: [
      {
        path: 'list',
        name: 'ApiList',
        component: () => import('@/views/api/list'),
        meta: { title: '接口列表' }
      },
      {
        path: 'auth',
        name: 'ApiAuth',
        component: () => import('@/views/api/auth'),
        meta: { title: '接口授权' }
      },
      {
        path: 'log',
        name: 'ApiLog',
        component: () => import('@/views/api/log'),
        meta: { title: '接口日志' }
      }
    ]
  },
  {
    path: '/log',
    component: Layout,
    name: 'Log',
    redirect: '/log/index',
    meta: { title: '日志管理', icon: 'el-icon-warning-outline' },
    children: [
      {
        path: 'index',
        name: 'LogList',
        component: () => import('@/views/log/index'),
        meta: { title: '任务日志', breadcrumb: false }
      },
      {
        path: 'operationDefinition',
        name: 'OperationLogDefinition',
        component: () => import('@/views/logManagement/operationDefinition'),
        meta: { title: '操作日志定义' }
      },
      {
        path: 'scheduleDefinition',
        name: 'ScheduleLogDefinition',
        component: () => import('@/views/logManagement/scheduleDefinition'),
        meta: { title: '调度日志定义' }
      },
      {
        path: 'computeDefinition',
        name: 'ComputeLogDefinition',
        component: () => import('@/views/logManagement/computeDefinition'),
        meta: { title: '计算日志定义' }
      },
      {
        path: 'operationLog',
        name: 'OperationLog',
        component: () => import('@/views/logManagement/operationLog'),
        meta: { title: '操作日志记录' }
      },
      {
        path: 'scheduleLog',
        name: 'ScheduleLog',
        component: () => import('@/views/logManagement/scheduleLog'),
        meta: { title: '调度日志记录' }
      },
      {
        path: 'computeLog',
        name: 'ComputeLog',
        component: () => import('@/views/logManagement/computeLog'),
        meta: { title: '计算日志记录' }
      }
    ]
  },
  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]
const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}
// export function selfAddRouter() {
//   const newRouter = createRouter()
//   router.matcher = newRouter.matcher // reset router

// }

export default router
