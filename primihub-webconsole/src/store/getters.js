const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.userName,
  routes: state => state.permission.routes, // 有权限路由
  permissionList: state => state.user.permissionList, // 后端返回权限列表
  buttonPermissionList: state => state.permission.buttonPermissionList // 按钮权限列表
}
export default getters
