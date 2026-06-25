import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000
})

service.interceptors.request.use(config => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (user?.role) {
    config.headers['X-User-Role'] = user.role
    config.headers['X-User-Id'] = user.id
  }
  return config
})

service.interceptors.response.use(
  response => {
    const result = response.data
    if (result.code !== 200) {
      ElMessage.error(result.message || '请求失败')
      if (result.message === '请先登录') {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result.data
  },
  error => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
