const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  userName: state => state.user.userName,
  userOrganId: state => state.user.userOrganId,
  userOrganName: state => state.user.userOrganName,
  userAccount: state => state.user.userAccount,
  registerType: state => state.user.registerType,
  routes: state => state.permission.routes, // has permission router
  permissionList: state => state.user.permissionList, // the permissionList from server
  buttonPermissionList: state => state.permission.buttonPermissionList // permission button list
}
export default getters
