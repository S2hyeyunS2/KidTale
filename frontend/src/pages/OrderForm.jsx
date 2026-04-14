import { useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'
import StepIndicator from '../components/StepIndicator'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import LoginModal from '../components/LoginModal'
import { createOrder } from '../api/order'
import { useAuth } from '../context/AuthContext'

export default function OrderForm() {
  const { storyId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const story = location.state?.story || null
  const { user } = useAuth()
  const [showLogin, setShowLogin] = useState(false)

  const [form, setForm] = useState({
    recipientName: '',
    recipientPhone: '',
    postalCode: '',
    address1: '',
    address2: '',
    memo: '',
    quantity: 1,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleQuantity = (delta) => {
    setForm((prev) => ({ ...prev, quantity: Math.max(1, Math.min(10, prev.quantity + delta)) }))
  }

  const isValid =
    form.recipientName.trim().length > 0 &&
    form.recipientPhone.trim().length > 0 &&
    form.postalCode.trim().length > 0 &&
    form.address1.trim().length > 0

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!isValid) return
    setLoading(true)
    setError('')
    try {
      const order = await createOrder({
        storyId: Number(storyId),
        recipientName: form.recipientName.trim(),
        recipientPhone: form.recipientPhone.trim(),
        postalCode: form.postalCode.trim(),
        address1: form.address1.trim(),
        address2: form.address2.trim(),
        memo: form.memo.trim(),
        quantity: form.quantity,
      })
      navigate(`/complete/${order.id}`, { state: { order, story } })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <LoadingSpinner message="주문을 처리 중입니다..." />

  // 비로그인 상태 — 로그인 안내
  if (!user) {
    return (
      <div className="min-h-screen bg-gray-soft">
        <Header />
        {showLogin && (
          <LoginModal
            onClose={() => setShowLogin(false)}
            redirectPath={`/order/${storyId}`}
          />
        )}
        <div className="flex flex-col items-center justify-center py-32 px-6 text-center">
          <div className="text-5xl mb-4">🔐</div>
          <p className="text-gray-700 font-semibold text-lg mb-2">로그인이 필요합니다</p>
          <p className="text-gray-400 text-sm mb-8">주문하려면 먼저 로그인해주세요</p>
          <div className="flex gap-3">
            <button onClick={() => navigate(-1)} className="btn-outline">이전으로</button>
            <button onClick={() => setShowLogin(true)} className="btn-primary">로그인하기</button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-soft">
      <Header />
      <div className="max-w-2xl mx-auto px-6 py-10">
        <StepIndicator current={3} />

        {/* 주문 헤더 */}
        <div className="text-center mb-8 animate-fadeInUp">
          <div className="text-4xl mb-3">📦</div>
          <h1 className="text-2xl font-bold text-gray-900">배송 정보를 입력해주세요</h1>
          <p className="text-gray-500 mt-2 text-sm">입력하신 주소로 직접 인쇄된 동화책을 배송해드립니다</p>
        </div>

        {/* 주문할 동화 정보 */}
        {story && (
          <div className="card p-5 mb-6 flex items-center gap-4 animate-fadeInUp">
            <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center text-2xl shrink-0">
              📖
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-bold text-gray-900 truncate">{story.title}</p>
              <p className="text-sm text-gray-500">주인공: {story.childName} · {story.theme}</p>
            </div>
            <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full shrink-0">
              하드커버
            </span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5 animate-fadeInUp">

          {/* 수령인 */}
          <div className="card p-6 space-y-4">
            <h2 className="font-bold text-gray-900 flex items-center gap-2">
              <span className="text-primary">👤</span> 수령인 정보
            </h2>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                수령인 이름 <span className="text-primary">*</span>
              </label>
              <input
                type="text"
                name="recipientName"
                className="input-field"
                placeholder="홍길동"
                maxLength={30}
                value={form.recipientName}
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                연락처 <span className="text-primary">*</span>
              </label>
              <input
                type="tel"
                name="recipientPhone"
                className="input-field"
                placeholder="010-0000-0000"
                maxLength={20}
                value={form.recipientPhone}
                onChange={handleChange}
              />
            </div>
          </div>

          {/* 배송지 */}
          <div className="card p-6 space-y-4">
            <h2 className="font-bold text-gray-900 flex items-center gap-2">
              <span className="text-primary">📍</span> 배송지 정보
            </h2>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                우편번호 <span className="text-primary">*</span>
              </label>
              <input
                type="text"
                name="postalCode"
                className="input-field"
                placeholder="12345"
                maxLength={10}
                value={form.postalCode}
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                기본 주소 <span className="text-primary">*</span>
              </label>
              <input
                type="text"
                name="address1"
                className="input-field"
                placeholder="서울특별시 강남구 테헤란로 123"
                maxLength={100}
                value={form.address1}
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                상세 주소
              </label>
              <input
                type="text"
                name="address2"
                className="input-field"
                placeholder="101동 202호"
                maxLength={100}
                value={form.address2}
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                배송 메모
              </label>
              <input
                type="text"
                name="memo"
                className="input-field"
                placeholder="부재 시 경비실에 맡겨주세요"
                maxLength={100}
                value={form.memo}
                onChange={handleChange}
              />
            </div>
          </div>

          {/* 수량 */}
          <div className="card p-6">
            <h2 className="font-bold text-gray-900 flex items-center gap-2 mb-4">
              <span className="text-primary">📚</span> 주문 수량
            </h2>
            <div className="flex items-center gap-4">
              <button
                type="button"
                onClick={() => handleQuantity(-1)}
                disabled={form.quantity <= 1}
                className="w-10 h-10 rounded-xl border-2 border-gray-200 flex items-center justify-center text-lg font-bold
                           text-gray-600 hover:border-primary hover:text-primary transition-colors
                           disabled:opacity-30 disabled:cursor-not-allowed"
              >
                −
              </button>
              <div className="flex-1 text-center">
                <span className="text-3xl font-bold text-gray-900">{form.quantity}</span>
                <span className="text-gray-500 ml-1">권</span>
              </div>
              <button
                type="button"
                onClick={() => handleQuantity(1)}
                disabled={form.quantity >= 10}
                className="w-10 h-10 rounded-xl border-2 border-gray-200 flex items-center justify-center text-lg font-bold
                           text-gray-600 hover:border-primary hover:text-primary transition-colors
                           disabled:opacity-30 disabled:cursor-not-allowed"
              >
                +
              </button>
            </div>
            <p className="text-xs text-gray-400 text-center mt-3">최대 10권까지 주문 가능합니다</p>
          </div>

          <ErrorMessage message={error} />

          {/* 주문 요약 */}
          {isValid && (
            <div className="rounded-xl bg-primary/5 border border-primary/20 p-4 text-sm text-gray-700 animate-fadeInUp">
              <p className="font-semibold text-primary mb-2">✅ 주문 확인</p>
              <div className="space-y-1">
                <p>수령인: <strong>{form.recipientName}</strong></p>
                <p>연락처: <strong>{form.recipientPhone}</strong></p>
                <p>배송지: <strong>{form.address1} {form.address2}</strong></p>
                <p>수량: <strong>{form.quantity}권</strong></p>
              </div>
            </div>
          )}

          {/* 버튼 */}
          <div className="flex flex-col sm:flex-row gap-3">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="btn-outline flex-1"
            >
              ← 이전 단계
            </button>
            <button
              type="submit"
              disabled={!isValid || loading}
              className="btn-primary flex-1 text-base py-4"
            >
              주문하기 →
            </button>
          </div>
        </form>
      </div>
      <Footer />
    </div>
  )
}
