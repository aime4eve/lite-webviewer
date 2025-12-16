<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
  line-height: 1.6;
}

/* 全局容器样式 */
#app {
  max-width: 100%;
  margin: 0 auto;
  background-color: #fff;
  min-height: 100vh;
}

/* 页面容器通用样式 */
.home-container, .device-detail-container, .subscription-container, .add-device-container,
.devices-container, .profile-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.devices-container, .profile-container {
  padding: 20px;
}

.devices-container h1, .profile-container h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
  color: #333;
}

.devices-container p, .profile-container p {
  font-size: 16px;
  color: #666;
  text-align: center;
  margin-top: 40px;
}
</style>
