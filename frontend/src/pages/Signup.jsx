import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'
import { useAuth } from '../context/AuthContext'
import Header from '../components/Header'
import Footer from '../components/Footer'

export default function Signup() {
  const navigate = useNavigate()
  const { saveAuth } = useAuth()
  const [form, setForm] = useState({
    username: '',
    password: '',
    passwordConfirm: '',
    name: '',
    email: '',
    phone: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  const validate = () => {
    if (form.username.length < 4) return '아이디는 4자 이상이어야 합니다.'
    if (form.password.length < 8) return '비밀번호는 8자 이상이어야 합니다.'
    if (form.password !== form.passwordConfirm) return '비밀번호가 일치하지 않습니다.'
    if (!form.name.trim()) return '이름을 입력해주세요.'
    if (!form.email.includes('@')) return '올바른 이메일 형식이 아닙니다.'
    return null
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const validationError = validate()
    if (validationError) {
      setError(validationError)
      return
    }
    setError('')
    setLoading(true)
    try {
      const { username, password, name, email, phone } = form
      const data = await signup({ username, password, name, email, phone })
      saveAuth(data)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-soft">
      <Header />
      <div className="max-w-md mx-auto px-4 py-12">
        <div className="card p-8 animate-fadeInUp">
          <div className="text-center mb-8">
            <span className="text-4xl">✨</span>
            <h1 className="text-2xl font-bold text-gray-900 mt-3">회원가입</h1>
            <p className="text-gray-400 text-sm mt-1">KidTale과 함께 특별한 동화를 만들어보세요</p>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Field label="아이디" name="username" value={form.username} onChange={handleChange}
              placeholder="4~20자 영문/숫자" />

            <Field label="비밀번호" name="password" type="password" value={form.password}
              onChange={handleChange} placeholder="8자 이상" />

            <Field label="비밀번호 확인" name="passwordConfirm" type="password"
              value={form.passwordConfirm} onChange={handleChange} placeholder="비밀번호 재입력" />

            <Field label="이름" name="name" value={form.name} onChange={handleChange}
              placeholder="홍길동" />

            <Field label="이메일" name="email" type="email" value={form.email}
              onChange={handleChange} placeholder="example@email.com" />

            <Field label="연락처 (선택)" name="phone" value={form.phone}
              onChange={handleChange} placeholder="010-0000-0000" required={false} />

            {error && (
              <p className="text-sm text-red-500 text-center">{error}</p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn-primary py-3 mt-2"
            >
              {loading ? '처리 중...' : '회원가입'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-400 mt-5">
            이미 계정이 있으신가요?{' '}
            <button
              onClick={() => navigate('/')}
              className="text-primary font-semibold hover:underline"
            >
              로그인
            </button>
          </p>
        </div>
      </div>
      <Footer />
    </div>
  )
}

function Field({ label, name, type = 'text', value, onChange, placeholder, required = true }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label} {required && <span className="text-red-400">*</span>}
      </label>
      <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
      />
    </div>
  )
}
