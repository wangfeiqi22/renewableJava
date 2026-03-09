import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
      } else {
        const msg = error.response.data.message || error.response.data.error || '请求失败'
        ElMessage.error(`${error.response.status}: ${msg}`)
      }
    }
    return Promise.reject(error)
  }
)

export default api
