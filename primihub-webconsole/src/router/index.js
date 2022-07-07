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
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/project/list'
  }
]

export const asyncRoutes = [
  {
    path: '/project',
    name: 'Project',
    component: Layout,
    meta: { icon: 'el-icon-menu', title: '我的项目' },
    redirect: '/project/list',
    children: [
      {
        path: 'list',
        name: 'ProjectList',
        meta: {
          title: '我的项目'
        },
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
          title: '项目详情'
        },
        hidden: true,
        component: () => import('@/views/project/detail')
      }
    ]
  },
  {
    path: '/dag',
    component: Layout,
    meta: {},
    hidden: true,
    children: [{
      path: '',
      name: 'dagGraph',
      component: () => import('@/views/dag')
    }]
  },
  {
    path: '/model',
    component: Layout,
    name: 'ModelMenu',
    meta: { icon: 'el-icon-s-opportunity', title: '模型管理' },
    redirect: '/model/list',
    hidden: true,
    children: [
      {
        path: 'list',
        name: 'ModelList',
        meta: {
          title: '模型管理'
        },
        component: () => import('@/views/model/list')
      },
      {
        path: 'create',
        name: 'ModelCreate',
        meta: {
          title: '添加模型'
        },
        hidden: true,
        component: () => import('@/views/model/create/index')
      },
      {
        path: 'detail/:id',
        name: 'ModelDetail',
        meta: {
          title: '模型详情'
        },
        hidden: true,
        component: () => import('@/views/model/detail')
      }
    ]
  },
  {
    path: '/privateSearch',
    component: Layout,
    name: 'PrivateSearch',
    redirect: '/privateSearch/list',
    children: [{
      path: 'list',
      name: 'PrivateSearchList',
      component: () => import('@/views/privateSearch/index'),
      meta: { title: '匿踪查询', icon: 'el-icon-search' }
    }]
  },
  {
    path: '/PSI',
    component: Layout,
    name: 'PSI',
    redirect: '/PSI/list',
    meta: { title: '隐私求交', icon: 'el-icon-lock' },
    children: [
      // {
      //   path: 'list',
      //   name: 'PSIDirectory',
      //   component: () => import('@/views/PSI/list'),
      //   meta: { title: '资源目录' }
      // },
      {
        path: 'task',
        name: 'PSITask',
        component: () => import('@/views/PSI/task'),
        meta: { title: '求交任务' }
      }, {
        path: 'result',
        name: 'PSIResult',
        component: () => import('@/views/PSI/result'),
        meta: { title: '求交结果' }
      }]
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
        meta: { title: '联邦资源' }
      },
      {
        path: 'create',
        name: 'ResourceUpload',
        hidden: true,
        component: () => import('@/views/resource/create'),
        meta: { title: '新建资源' }
      },
      {
        path: 'edit/:id',
        name: 'ResourceEdit',
        hidden: true,
        component: () => import('@/views/resource/create'),
        meta: { title: '编辑资源' }
      },
      {
        path: 'detail/:id',
        name: 'ResourceDetail',
        meta: {
          title: '资源详情'
        },
        hidden: true,
        component: () => import('@/views/resource/detail')
      },
      {
        path: 'unionResourceDetail/:id',
        name: 'UnionResourceDetail',
        meta: {
          title: '联邦资源详情'
        },
        hidden: true,
        component: () => import('@/views/resource/unionResourceDetail')
      },
      {
        path: 'authRecord',
        name: 'ResourceRecord',
        meta: { title: '授权申请记录' },
        component: () => import('@/views/resource/authRecord')
      },
      {
        path: 'authAudit',
        name: 'ResourceApprovalList',
        meta: { title: '授权审批' },
        component: () => import('@/views/resource/authAudit')
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
      // {
      //   path: 'organ',
      //   name: 'OrganManage',
      //   component: () => import('@/views/setting/organ'),
      //   meta: { title: '机构管理', icon: 'el-icon-office-building' }
      // },
      {
        path: 'center',
        name: 'CenterManage',
        component: () => import('@/views/setting/center'),
        meta: { title: '中心管理' }
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
