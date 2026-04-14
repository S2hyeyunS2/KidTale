import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import LoginModal from './LoginModal'

export default function Header() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [showLogin, setShowLogin] = useState(false)

  return (
    <>
      <header className="sticky top-0 z-50 bg-white border-b border-gray-100 shadow-sm">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-1 select-none">
            <span className="text-2xl font-bold text-primary tracking-tight">KidTale</span>
            <span className="text-primary text-xl">♡</span>
          </Link>

          <nav className="flex items-center gap-6 text-sm font-medium text-gray-600">
            <Link to="/" className="hover:text-primary transition-colors">홈</Link>
            <Link to="/create" className="hover:text-primary transition-colors">동화 만들기</Link>
            {user && (
              <Link to="/mypage" className="hover:text-primary transition-colors">마이페이지</Link>
            )}
          </nav>

          <div className="flex items-center gap-2">
            {user ? (
              <>
                <span className="text-sm text-gray-600 hidden sm:block">
                  {user.name}님
                </span>
                <button
                  onClick={() => { logout(); navigate('/') }}
                  className="btn-outline text-sm py-2 px-4"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => setShowLogin(true)}
                  className="text-sm font-medium text-gray-600 hover:text-primary transition-colors"
                >
                  로그인
                </button>
                <Link to="/signup" className="btn-primary text-sm py-2 px-5">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {showLogin && (
        <LoginModal onClose={() => setShowLogin(false)} />
      )}
    </>
  )
}
