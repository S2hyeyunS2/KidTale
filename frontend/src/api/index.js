import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 60000, // Gemini 응답 대기를 위해 60초
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const message =
      err.response?.data?.message || '서버와의 통신 중 오류가 발생했습니다.'
    return Promise.reject(new Error(message))
  }
)

export default api
