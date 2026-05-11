import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import OrderCreateView from '../views/OrderCreateView.vue'
import HomeDashboardView from '../views/HomeDashboardView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import KnowledgeBaseView from '../views/KnowledgeBaseView.vue'
import StationFleetView from '../views/StationFleetView.vue'
import DriverTaskView from '../views/DriverTaskView.vue'
import AdminUserAuditView from '../views/AdminUserAuditView.vue'
import ChatView from '../views/ChatView.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeDashboardView
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatView
  },
  {
    path: '/ai-assistant',
    redirect: '/chat'
  },
  {
    path: '/order/create',
    name: 'OrderCreate',
    component: OrderCreateView
  },
  {
    path: '/order/history',
    name: 'OrderHistory',
    component: () => import('../views/OrderListView.vue')
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetailView.vue')
  },
  {
    path: '/order/list',
    redirect: '/order/history'
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: AdminDashboardView
  },
  {
    path: '/admin/kb',
    name: 'KnowledgeBase',
    component: KnowledgeBaseView
  },
  {
    path: '/admin/sf',
    name: 'StationFleet',
    component: StationFleetView
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUserAuditView
  },
  {
    path: '/driver/tasks',
    name: 'DriverTasks',
    component: DriverTaskView
  },
  {
    path: '/driver/orders',
    name: 'DriverOrderList',
    component: () => import('../views/DriverOrderListView.vue')
  },
  {
    path: '/driver/order/:id',
    name: 'DriverOrderDetail',
    component: () => import('../views/DriverOrderDetailView.vue')
  },
  // Fleet Operations Routes
  {
    path: '/fleet/profile',
    name: 'FleetProfile',
    component: () => import('../views/FleetProfileView.vue')
  },
  {
    path: '/fleet/drivers',
    name: 'FleetDrivers',
    component: () => import('../views/FleetDriverManagementView.vue')
  },
  {
    path: '/fleet/orders',
    name: 'FleetOrders',
    component: () => import('../views/FleetOrderManagementView.vue')
  },
  {
    path: '/fleet/dispatch',
    name: 'FleetDispatch',
    component: () => import('../views/FleetDispatchView.vue')
  },
  {
    path: '/fleet/grab-pool',
    name: 'FleetGrabPool',
    component: () => import('../views/FleetGrabPoolView.vue')
  },
  {
    path: '/fleet/projects',
    name: 'FleetProjects',
    component: () => import('../views/FleetProjectsView.vue')
  },
  {
    path: '/fleet/projects/create',
    name: 'ProjectCreate',
    component: () => import('../views/ProjectCreatePage.vue')
  },
  {
    path: '/fleet/orders/create',
    name: 'FleetOrderCreate',
    component: () => import('../views/FleetOrderCreateView.vue')
  },
  {
    path: '/fleet/stats',
    name: 'FleetStats',
    component: () => import('../views/FleetStatsView.vue')
  },
  {
    path: '/user/profile',
    redirect: '/profile/user'
  },
  {
    path: '/vip/dashboard',
    redirect: '/profile/user'
  },
  {
    path: '/admin/personal',
    redirect: '/admin/dashboard'
  },
  {
    path: '/profile/user',
    name: 'UserProfile',
    component: () => import('../views/UserProfileView.vue')
  },
  {
    path: '/profile/enterprise',
    name: 'EnterpriseProfile',
    component: () => import('../views/EnterpriseProfileView.vue')
  },
  {
    path: '/profile/driver',
    name: 'DriverProfile',
    component: () => import('../views/DriverProfileView.vue')
  },
  {
    path: '/property/dashboard',
    name: 'PropertyDashboard',
    component: () => import('../views/PropertyDashboardView.vue')
  },
  {
    path: '/station/work',
    name: 'StationWork',
    component: () => import('../views/StationWorkView.vue')
  },
  {
    path: '/mall',
    name: 'Mall',
    component: () => import('../views/MallView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Global Navigation Guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // Public routes that don't require authentication
  const publicRoutes = ['/login', '/register', '/mall']
  
  if (publicRoutes.includes(to.path)) {
    // If user is already logged in and tries to access login/register, redirect to chat
    if (token && user && (to.path === '/login' || to.path === '/register')) {
      return next('/chat')
    }
    return next()
  }

  // Authentication required
  if (!token || !user) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // Role-based Access Control (RBAC)
  const role = user.role

  if (to.path.startsWith('/admin') && role !== 'admin') {
    return next('/home') // Or a 403 Forbidden page
  }

  if (to.path.startsWith('/driver') && role !== 'driver') {
    return next('/home')
  }

  if (to.path.startsWith('/property') && role !== 'property') {
    return next('/home')
  }

  if (to.path.startsWith('/station') && role !== 'station') {
    return next('/home')
  }

  if (to.path.startsWith('/fleet') && !['fleet_owner', 'fleet_dispatcher'].includes(role)) {
    return next('/home')
  }

  // If none of the above caught it, proceed
  next()
})

export default router
