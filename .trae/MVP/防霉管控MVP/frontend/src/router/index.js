import { createRouter, createWebHistory } from 'vue-router'

// C端路由
import HomeView from '../views/HomeView.vue'
import DevicesView from '../views/DevicesView.vue'
import DeviceDetailView from '../views/DeviceDetailView.vue'
import SubscriptionView from '../views/SubscriptionView.vue'
import AddDeviceView from '../views/AddDeviceView.vue'
import ProfileView from '../views/ProfileView.vue'
const CRoomManagementView = () => import('../views/c端/CRoomManagementView.vue')

// 门户路由
const PortalView = () => import('../views/PortalView.vue')

// B端路由
const BHomeView = () => import('../views/b端/BHomeView.vue')
const BDevicesView = () => import('../views/b端/BDevicesView.vue')
const BDeviceDetailView = () => import('../views/b端/BDeviceDetailView.vue')
const BSpaceManagementView = () => import('../views/b端/BSpaceManagementView.vue')
const BStrategyCustomizationView = () => import('../views/b端/BStrategyCustomizationView.vue')
const BSubscriptionView = () => import('../views/SubscriptionView.vue')
const BAddDeviceView = () => import('../views/AddDeviceView.vue')
const BProfileView = () => import('../views/ProfileView.vue')

// 运营运维端路由
const OperatorDashboardView = () => import('../views/operator/OperatorDashboardView.vue')
const UserManagementView = () => import('../views/operator/UserManagementView.vue')
const DeviceRegistrationView = () => import('../views/operator/DeviceRegistrationView.vue')
const DeviceFaultMonitoringView = () => import('../views/operator/DeviceFaultMonitoringView.vue')
const DeviceHealthView = () => import('../views/operator/DeviceHealthView.vue')
const StrategyManagementView = () => import('../views/operator/StrategyManagementView.vue')
const BillingManagementView = () => import('../views/operator/BillingManagementView.vue')

// New Views
const DeviceHealthViewNew = () => import('../views/device/DeviceHealth.vue')
const RiskPredictionView = () => import('../views/device/RiskPrediction.vue')
const ProvisioningView = () => import('../views/device/Provisioning.vue')
const CleaningTasksView = () => import('../views/service/CleaningTasks.vue')
const DiagnosticsView = () => import('../views/device/Diagnostics.vue')
const AlarmsView = () => import('../views/device/Alarms.vue')
const PointsHistoryView = () => import('../views/subscription/PointsHistory.vue')
const ExportReportView = () => import('../views/report/ExportReport.vue')

const routes = [
  // C端路由
  {
    path: '/c/',
    name: 'c-home',
    component: HomeView
  },
  {
    path: '/c/devices',
    name: 'c-devices',
    component: DevicesView
  },
  {
    path: '/c/device-detail/:id',
    name: 'c-device-detail',
    component: DeviceDetailView,
    props: true
  },
  {
    path: '/c/subscription',
    name: 'c-subscription',
    component: SubscriptionView
  },
  {
    path: '/c/add-device',
    name: 'c-add-device',
    component: AddDeviceView
  },
  {
    path: '/c/profile',
    name: 'c-profile',
    component: ProfileView
  },
  {
    path: '/c/room-management',
    name: 'c-room-management',
    component: CRoomManagementView
  },
  
  // B端路由
  {
    path: '/b/',
    name: 'b-home',
    component: BHomeView
  },
  {
    path: '/b/devices',
    name: 'b-devices',
    component: BDevicesView
  },
  {
    path: '/b/device-detail/:id',
    name: 'b-device-detail',
    component: BDeviceDetailView,
    props: true
  },
  {
    path: '/b/subscription',
    name: 'b-subscription',
    component: BSubscriptionView
  },
  {
    path: '/b/add-device',
    name: 'b-add-device',
    component: BAddDeviceView
  },
  {
    path: '/b/profile',
    name: 'b-profile',
    component: BProfileView
  },
  {
    path: '/b/space-management',
    name: 'b-space-management',
    component: BSpaceManagementView
  },
  {
    path: '/b/strategy-customization',
    name: 'b-strategy-customization',
    component: BStrategyCustomizationView
  },
  
  // 运营运维端路由
  {
    path: '/operator/',
    name: 'operator-dashboard',
    component: OperatorDashboardView
  },
  {
    path: '/operator/user-management',
    name: 'operator-user-management',
    component: UserManagementView
  },
  {
    path: '/operator/device-registration',
    name: 'operator-device-registration',
    component: DeviceRegistrationView
  },
  {
    path: '/operator/device-fault-monitoring',
    name: 'operator-device-fault-monitoring',
    component: DeviceFaultMonitoringView
  },
  {
    path: '/operator/device-health',
    name: 'operator-device-health',
    component: DeviceHealthView
  },
  {
    path: '/operator/strategy-management',
    name: 'operator-strategy-management',
    component: StrategyManagementView
  },
  {
    path: '/operator/billing-management',
    name: 'operator-billing-management',
    component: BillingManagementView
  },

  // New Features Routes
  // C端
  { path: '/c/device-health/:id', name: 'c-device-health', component: DeviceHealthViewNew },
  { path: '/c/risk-prediction/:id', name: 'c-risk-prediction', component: RiskPredictionView },
  { path: '/c/provisioning', name: 'c-provisioning', component: ProvisioningView },
  { path: '/c/points-history', name: 'c-points-history', component: PointsHistoryView },

  // B端
  { path: '/b/reports', name: 'b-reports', component: ExportReportView },
  { path: '/b/cleaning-tasks', name: 'b-cleaning-tasks', component: CleaningTasksView },

  // Operator
  { path: '/operator/diagnostics', name: 'operator-diagnostics', component: DiagnosticsView },
  { path: '/operator/alarms', name: 'operator-alarms', component: AlarmsView },
  { path: '/operator/climate-config', name: 'operator-climate-config', component: ClimateConfigView },
  
  // 网站门户路由
  {
    path: '/portal',
    name: 'portal',
    component: PortalView
  },
  // 默认重定向到门户
  {
    path: '/',
    redirect: '/portal'
  },
  // 404重定向
  {
    path: '/:pathMatch(.*)*',
    redirect: '/portal'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router