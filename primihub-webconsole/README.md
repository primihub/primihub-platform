## Preparation
You will need [node](https://nodejs.org/en/), [git](https://git-scm.com/). The project is based on [ES2015+](https://es6.ruanyifeng.com/)、[Vue.Js](https://vuejs.org/)、[Vuex](https://vuex.vuejs.org/)、[Vue-Router](https://router.vuejs.org/)、[antv-x6](https://x6.antv.vision/zh) and [vue-element-admin](https://panjiachen.github.io/vue-element-admin-site/). It would be helpful if you have pre-existing knowledge on those.

## Getting Started

1.enter the project directory
```bash
cd primihub-webconsole
```
2.install dependency
```bash
npm install
```
3.modify vue.config.js file, the target is your gateway url 
```bash
proxy: {
  '/dev-api': {
    target: 'your gateway url',
    ws: true,
    changeOrigin: true,
    pathRewrite: {
      '^/dev-api': ''
    }
  }
}
```
4.start the project
```bash
npm run dev
```

This will automatically open http://localhost:8080

## Build

```bash
# build for test environment
npm run build:stage

# build for production environment
npm run build:prod
```

## Browsers Support
Modern browsers and Internet Explorer 10+.

<html>
 <head></head>
 <body>
  <table>
   <thead>
    <tr>
     <th><a href="http://godban.github.io/browsers-support-badges/" target="_blank" rel="noopener noreferrer"><img src="./src/assets/browsers-icon/edge_48x48.png" alt="IE / Edge" width="24px" height="24px" class="no-margin" />
       <svg xmlns="http://www.w3.org/2000/svg" aria-hidden="true" x="0px" y="0px" viewbox="0 0 100 100" width="15" height="15" class="icon outbound">
        <path fill="currentColor" d="M18.8,85.1h56l0,0c2.2,0,4-1.8,4-4v-32h-8v28h-48v-48h28v-8h-32l0,0c-2.2,0-4,1.8-4,4v56C14.8,83.3,16.6,85.1,18.8,85.1z"></path> 
        <polygon fill="currentColor" points="45.7,48.7 51.3,54.3 77.2,28.5 77.2,37.2 85.2,37.2 85.2,14.9 62.8,14.9 62.8,22.9 71.5,22.9"></polygon>
       </svg></a><br />IE / Edge</th> 
     <th><a href="http://godban.github.io/browsers-support-badges/" target="_blank" rel="noopener noreferrer"><img src="./src/assets/browsers-icon/firefox_48x48.png" alt="Firefox" width="24px" height="24px" class="no-margin" />
       <svg xmlns="http://www.w3.org/2000/svg" aria-hidden="true" x="0px" y="0px" viewbox="0 0 100 100" width="15" height="15" class="icon outbound">
        <path fill="currentColor" d="M18.8,85.1h56l0,0c2.2,0,4-1.8,4-4v-32h-8v28h-48v-48h28v-8h-32l0,0c-2.2,0-4,1.8-4,4v56C14.8,83.3,16.6,85.1,18.8,85.1z"></path> 
        <polygon fill="currentColor" points="45.7,48.7 51.3,54.3 77.2,28.5 77.2,37.2 85.2,37.2 85.2,14.9 62.8,14.9 62.8,22.9 71.5,22.9"></polygon>
       </svg></a><br />Firefox</th> 
     <th><a href="http://godban.github.io/browsers-support-badges/" target="_blank" rel="noopener noreferrer"><img src="./src/assets/browsers-icon/chrome_48x48.png" alt="Chrome" width="24px" height="24px" class="no-margin" />
       <svg xmlns="http://www.w3.org/2000/svg" aria-hidden="true" x="0px" y="0px" viewbox="0 0 100 100" width="15" height="15" class="icon outbound">
        <path fill="currentColor" d="M18.8,85.1h56l0,0c2.2,0,4-1.8,4-4v-32h-8v28h-48v-48h28v-8h-32l0,0c-2.2,0-4,1.8-4,4v56C14.8,83.3,16.6,85.1,18.8,85.1z"></path> 
        <polygon fill="currentColor" points="45.7,48.7 51.3,54.3 77.2,28.5 77.2,37.2 85.2,37.2 85.2,14.9 62.8,14.9 62.8,22.9 71.5,22.9"></polygon>
       </svg></a><br />Chrome</th> 
     <th><a href="http://godban.github.io/browsers-support-badges/" target="_blank" rel="noopener noreferrer"><img src="./src/assets/browsers-icon/safari_48x48.png" alt="Safari" width="24px" height="24px" class="no-margin" />
       <svg xmlns="http://www.w3.org/2000/svg" aria-hidden="true" x="0px" y="0px" viewbox="0 0 100 100" width="15" height="15" class="icon outbound">
        <path fill="currentColor" d="M18.8,85.1h56l0,0c2.2,0,4-1.8,4-4v-32h-8v28h-48v-48h28v-8h-32l0,0c-2.2,0-4,1.8-4,4v56C14.8,83.3,16.6,85.1,18.8,85.1z"></path> 
        <polygon fill="currentColor" points="45.7,48.7 51.3,54.3 77.2,28.5 77.2,37.2 85.2,37.2 85.2,14.9 62.8,14.9 62.8,22.9 71.5,22.9"></polygon>
       </svg></a><br />Safari</th>
    </tr>
   </thead> 
   <tbody>
    <tr>
     <td>IE10, IE11, Edge</td> 
     <td>last 2 versions</td> 
     <td>last 2 versions</td> 
     <td>last 2 versions</td>
    </tr>
   </tbody>
  </table>
 </body>
</html>

