import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'
import { useAuth } from '../context/AuthContext'
import { getMyStories } from '../api/story'

export default function MyPage() {
  const navigate = useNavigate()
  const { user, logout, loading } = useAuth()
  const [stories, setStories] = useState([])
  const [storiesLoading, setStoriesLoading] = useState(true)

  useEffect(() => {
    if (!loading && !user) {
      navigate('/')
    }
  }, [user, loading, navigate])

  useEffect(() => {
    if (!user) return
    getMyStories()
      .then((data) => setStories(data || []))
      .catch(() => setStories([]))
      .finally(() => setStoriesLoading(false))
  }, [user])

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  if (loading) return null

  const statusLabel = (status) => {
    const map = {
      DRAFT: '생성 완료',
      BOOK_CREATED: '책 제작 완료',
      ORDERED: '주문 완료',
    }
    return map[status] || status
  }

  const statusColor = (status) => {
    const map = {
      DRAFT: 'bg-blue-100 text-blue-700',
      BOOK_CREATED: 'bg-amber-100 text-amber-700',
      ORDERED: 'bg-green-100 text-green-700',
    }
    return map[status] || 'bg-gray-100 text-gray-600'
  }

  return (
    <div className="min-h-screen bg-gray-soft">
      <Header />
      <div className="max-w-4xl mx-auto px-4 py-10">

        {/* 프로필 카드 */}
        <div className="card p-6 mb-8 animate-fadeInUp">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center text-3xl shrink-0">
              👤
            </div>
            <div className="flex-1">
              <p className="text-lg font-bold text-gray-900">{user?.name}</p>
              <p className="text-sm text-gray-400">{user?.email}</p>
              {user?.phone && <p className="text-sm text-gray-400">{user.phone}</p>}
            </div>
            <button
              onClick={handleLogout}
              className="btn-outline text-sm py-2 px-4 shrink-0"
            >
              로그아웃
            </button>
          </div>
        </div>

        {/* 내 동화 목록 */}
        <div className="animate-fadeInUp">
          <h2 className="text-lg font-bold text-gray-900 mb-4">내 동화 목록</h2>
          {storiesLoading ? (
            <div className="flex justify-center py-12">
              <div className="w-8 h-8 border-4 border-primary/30 border-t-primary rounded-full animate-spin" />
            </div>
          ) : stories.length === 0 ? (
            <div className="card p-12 text-center">
              <div className="text-4xl mb-3">📖</div>
              <p className="text-gray-500 font-medium mb-2">아직 만든 동화가 없어요</p>
              <button onClick={() => navigate('/create')} className="btn-primary mt-4 text-sm py-2 px-6">
                동화 만들기 →
              </button>
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {stories.map((s) => (
                <div
                  key={s.id}
                  className="card p-5 flex items-center gap-4 cursor-pointer hover:shadow-card-hover transition-shadow"
                  onClick={() => navigate(`/preview/${s.id}`, { state: { story: s } })}
                >
                  <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center text-2xl shrink-0">
                    📖
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-gray-900 truncate">{s.title}</p>
                    <p className="text-sm text-gray-400 mt-0.5">
                      주인공: {s.childName} ({s.childAge}세) · {s.theme}
                    </p>
                  </div>
                  <span className={`text-xs font-semibold px-2.5 py-1 rounded-full shrink-0 ${statusColor(s.status)}`}>
                    {statusLabel(s.status)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  )
}
