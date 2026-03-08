import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import OrderCreateView from '../views/OrderCreateView.vue'
import ChatView from '../views/ChatView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import KnowledgeBaseView from '../views/KnowledgeBaseView.vue'
import StationFleetView from '../views/StationFleetView.vue'
import DriverTaskView from '../views/DriverTaskView.vue'

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
    path: '/order/create',
    name: 'OrderCreate',
    component: OrderCreateView
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatView
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
    path: '/driver/tasks',
    name: 'DriverTasks',
    component: DriverTaskView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
