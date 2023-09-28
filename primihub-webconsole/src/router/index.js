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
        meta: { title: '求交任务' },
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
        path: 'derivedDataList',
        name: 'DerivedDataList',
        component: () => import('@/views/resource/derivedDataList'),
        meta: { title: '衍生数据资源' }
      },
      {
        path: 'unionList',
        name: 'UnionList',
        component: () => import('@/views/resource/unionList'),
        meta: { title: '协作方资源' }
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
        path: 'ui',
        name: 'UISetting',
        component: () => import('@/views/setting/ui'),
        meta: { title: '界面设置' },
        hidden: true
      }
    ]
  },
  {
    path: '/log',
    component: Layout,
    name: 'Log',
    redirect: '/log/index',
    meta: { title: '日志管理', icon: 'el-icon-warning-outline' },
    children: [{
      path: 'index',
      name: 'LogList',
      component: () => import('@/views/log/index'),
      meta: { title: '日志管理', breadcrumb: false }

    }]
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
