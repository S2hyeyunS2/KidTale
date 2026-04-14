import { useEffect, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'
import StepIndicator from '../components/StepIndicator'
import LoadingSpinner from '../components/LoadingSpinner'
import { getOrder } from '../api/order'

export default function OrderComplete() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()

  const [order, setOrder] = useState(location.state?.order || null)
  const [story, setStory] = useState(location.state?.story || null)
  const [loading, setLoading] = useState(!location.state?.order)

  useEffect(() => {
    if (!order) {
      getOrder(orderId)
        .then(setOrder)
        .catch(() => {})
        .finally(() => setLoading(false))
    }
  }, [orderId, order])

  if (loading) return <LoadingSpinner message="주문 정보를 불러오는 중..." />

  return (
    <div className="min-h-screen bg-gray-soft">
      <Header />
      <div className="max-w-2xl mx-auto px-6 py-10">
        <StepIndicator current={4} />

        {/* 성공 메시지 */}
        <div className="text-center mb-8 animate-fadeInUp">
          <div className="text-6xl mb-4">🎉</div>
          <h1 className="text-3xl font-bold text-gray-900 mb-3">주문이 완료되었어요!</h1>
          <p className="text-gray-500 leading-relaxed">
            세상에 하나뿐인 동화책이 곧 배송될 예정이에요.<br />
            전문 인쇄소에서 정성껏 제작해 보내드립니다.
          </p>
        </div>

        {/* 주문 상세 카드 */}
        {order && (
          <div className="card p-6 mb-6 animate-fadeInUp space-y-4">
            <h2 className="font-bold text-gray-900 flex items-center gap-2">
              <span className="text-primary">📋</span> 주문 상세
            </h2>

            <div className="divide-y divide-gray-100">
              {/* 주문 번호 */}
              <div className="py-3 flex justify-between text-sm">
                <span className="text-gray-500">주문 번호</span>
                <span className="font-semibold text-gray-900">#{order.id}</span>
              </div>

              {/* SweetBook 주문 ID */}
              {order.sweetBookOrderId && (
                <div className="py-3 flex justify-between text-sm">
                  <span className="text-gray-500">인쇄 주문 ID</span>
                  <span className="font-mono text-xs text-gray-600 bg-gray-100 px-2 py-1 rounded">
                    {order.sweetBookOrderId}
                  </span>
                </div>
              )}

              {/* 동화 제목 */}
              {story && (
                <div className="py-3 flex justify-between text-sm">
                  <span className="text-gray-500">동화 제목</span>
                  <span className="font-semibold text-gray-900 text-right max-w-[60%]">{story.title}</span>
                </div>
              )}

              {/* 수령인 */}
              <div className="py-3 flex justify-between text-sm">
                <span className="text-gray-500">수령인</span>
                <span className="font-semibold text-gray-900">{order.recipientName}</span>
              </div>

              {/* 연락처 */}
              <div className="py-3 flex justify-between text-sm">
                <span className="text-gray-500">연락처</span>
                <span className="font-semibold text-gray-900">{order.recipientPhone}</span>
              </div>

              {/* 배송지 */}
              <div className="py-3 flex flex-col gap-1 text-sm">
                <span className="text-gray-500">배송지</span>
                <span className="font-semibold text-gray-900">
                  {order.address1}{order.address2 ? ` ${order.address2}` : ''}
                </span>
                <span className="text-gray-400 text-xs">(우편번호: {order.postalCode})</span>
              </div>

              {/* 수량 */}
              <div className="py-3 flex justify-between text-sm">
                <span className="text-gray-500">수량</span>
                <span className="font-semibold text-gray-900">{order.quantity}권</span>
              </div>

              {/* 주문 상태 */}
              <div className="py-3 flex justify-between text-sm">
                <span className="text-gray-500">주문 상태</span>
                <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-600 bg-emerald-50 px-3 py-1 rounded-full">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  주문 접수 완료
                </span>
              </div>
            </div>
          </div>
        )}

        {/* 배송 안내 */}
        <div className="card p-6 mb-8 animate-fadeInUp">
          <h2 className="font-bold text-gray-900 flex items-center gap-2 mb-4">
            <span className="text-primary">🚚</span> 배송 안내
          </h2>
          <ul className="space-y-3 text-sm text-gray-600">
            {[
              { icon: '🏭', text: '전문 인쇄소에서 하드커버 제본으로 정성껏 제작합니다.' },
              { icon: '📅', text: '제작 후 3~5 영업일 내 배송이 시작됩니다.' },
              { icon: '📱', text: '배송 시작 시 입력하신 연락처로 안내 문자를 보내드립니다.' },
              { icon: '💝', text: '세상에 단 하나뿐인 책이 곧 도착할 거예요!' },
            ].map((item, i) => (
              <li key={i} className="flex items-start gap-3">
                <span className="text-lg shrink-0">{item.icon}</span>
                <span className="leading-relaxed">{item.text}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* 하단 버튼 */}
        <div className="flex flex-col sm:flex-row gap-3 animate-fadeInUp">
          <button
            onClick={() => navigate('/')}
            className="btn-outline flex-1"
          >
            홈으로 돌아가기
          </button>
          <button
            onClick={() => navigate('/create')}
            className="btn-primary flex-1"
          >
            새 동화 만들기 ✨
          </button>
        </div>
      </div>
      <Footer />
    </div>
  )
}
