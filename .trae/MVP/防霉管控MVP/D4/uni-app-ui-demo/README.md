# SmartMoldGuard 防霉管控系统 - uni-app实现

基于uni-app框架开发的SmartMoldGuard防霉管控系统前端应用，支持多平台运行（H5、微信小程序、App等）。

## 项目结构

```
uni-app-ui-demo/
├── pages/                    # 页面文件
│   ├── index/               # 首页
│   ├── device/              # 设备管理页面
│   ├── subscription/        # 订阅管理页面
│   ├── risk/                # 风险监控页面
│   ├── ops/                 # 运维管理页面
│   └── profile/             # 个人中心页面
├── static/                  # 静态资源
│   └── css/                 # 全局样式
├── App.vue                  # 应用主组件
├── main.js                  # 应用入口文件
├── manifest.json            # 应用配置文件
├── pages.json               # 页面路由配置
├── vue.config.js            # Vue配置文件
├── package.json             # 项目依赖配置
└── README.md               # 项目说明
```

## 安装依赖

在项目目录中执行以下命令安装依赖：

```bash
npm install
```

## 运行项目

### 开发模式

```bash
# 运行H5版本（浏览器预览）
npm run dev:h5

# 运行微信小程序版本（需要微信开发者工具）
npm run dev:mp-weixin

# 运行App版本
npm run dev:app
```

### 生产构建

```bash
# 构建H5版本
npm run build:h5

# 构建微信小程序版本
npm run build:mp-weixin

# 构建App版本
npm run build:app
```

## 浏览器预览

运行 `npm run dev:h5` 后，浏览器将自动打开项目预览页面，默认端口为8080。

## 项目特点

- 响应式设计，适配各种屏幕尺寸
- 完整的设备管理功能
- 风险监控和预警系统
- 订阅管理与积分系统
- 运维后台管理功能
- 用户友好的交互界面

## 技术栈

- **框架**: uni-app + Vue 3
- **样式**: CSS3 + Flex布局
- **状态管理**: Vue 3 Composition API
- **构建工具**: Vue CLI

## 开发环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0
- HBuilderX (可选，用于可视化开发)

## 业务功能

1. **首页控制面板**: 显示设备状态、风险指数和防霉战报
2. **设备管理**: 设备绑定、配置、状态监控
3. **智能联动**: 防霉策略配置和设备联动映射
4. **订阅中心**: 套餐选择、权益管理和积分系统
5. **风险监控**: B端用户风险房间清单和处理
6. **运维管理**: 告警处理、工单管理和远程诊断
7. **个人中心**: 用户信息和偏好设置

## API集成

项目预留了API接口集成点，可以与后端服务进行数据交互：

- 设备数据上报
- 风险预测服务
- 订阅管理服务
- 用户认证服务

## 开源许可

此项目为演示项目，仅供学习参考。