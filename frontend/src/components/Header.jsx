import { Link } from 'react-router-dom'

export default function Header() {
  return (
    <header className="sticky top-0 z-50 bg-white border-b border-gray-100 shadow-sm">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-1 select-none">
          <span className="text-2xl font-bold text-primary tracking-tight">KidTale</span>
          <span className="text-primary text-xl">♡</span>
        </Link>
        <nav className="flex items-center gap-6 text-sm font-medium text-gray-600">
          <Link to="/" className="hover:text-primary transition-colors">홈</Link>
          <Link to="/create" className="hover:text-primary transition-colors">동화 만들기</Link>
        </nav>
        <Link to="/create" className="btn-primary text-sm py-2 px-5">
          시작하기
        </Link>
      </div>
    </header>
  )
}
