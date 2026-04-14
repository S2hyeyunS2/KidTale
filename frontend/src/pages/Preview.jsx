import { useEffect, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import StepIndicator from '../components/StepIndicator'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { getStory } from '../api/story'
import { createBook } from '../api/book'

/**
 * Pollinations.ai 이미지 URL 생성
 * - scene description을 앞에 배치해 내용 일치도를 높임
 * - flux-anime 모델로 동화책 스타일 일러스트 생성
 */
function buildIllustrationUrl(imageDescription, pageNum, { width = 800, height = 600 } = {}) {
  const scene = imageDescription || `fairy tale scene page ${pageNum}`
  const style = 'cute chibi Korean children storybook illustration, soft pastel colors, sparkles, kawaii style, high quality'
  const desc = `${scene}, ${style}`
  return (
    `https://image.pollinations.ai/prompt/${encodeURIComponent(desc)}` +
    `?width=${width}&height=${height}&model=flux&nologo=true&enhance=true&seed=${pageNum}`
  )
}

/**
 * 로딩 → 이미지 → 실패 fallback 상태를 관리하는 이미지 컴포넌트
 */
function StoryImage({ src, alt, className, pageIndex }) {
  const [status, setStatus] = useState('loading')

  // src가 바뀌면 (페이지 전환) 다시 로딩 상태로
  useEffect(() => { setStatus('loading') }, [src])

  return (
    <div className={`relative ${className}`}>
      {/* 로딩 중 스켈레톤 */}
      {status === 'loading' && (
        <div className="absolute inset-0 flex flex-col items-center justify-center bg-gradient-to-br from-rose-50 to-amber-50">
          <div className="w-10 h-10 border-4 border-primary/30 border-t-primary rounded-full animate-spin mb-3" />
          <p className="text-xs text-gray-400 font-medium">그림 생성 중...</p>
        </div>
      )}

      <img
        src={src}
        alt={alt}
        className={`w-full h-full object-cover transition-opacity duration-500 ${status === 'loaded' ? 'opacity-100' : 'opacity-0'}`}
        onLoad={() => setStatus('loaded')}
        onError={() => setStatus('error')}
      />

      {/* 실패 fallback */}
      {status === 'error' && (
        <div className="absolute inset-0 flex flex-col items-center justify-center bg-gradient-to-br from-primary/10 to-rose-100">
          <span className="text-5xl mb-2">
            {['🌟', '🌈', '🎨', '✨', '🌸', '🦋', '🌻', '🎭', '🏰', '🎪'][pageIndex % 10]}
          </span>
          <p className="text-xs text-gray-400">이미지 생성 실패</p>
        </div>
      )}
    </div>
  )
}

/**
 * 페이지 목록 썸네일용 — 작은 사이즈, 로딩 시 회색 placeholder
 */
function ThumbImage({ src }) {
  const [status, setStatus] = useState('loading')
  useEffect(() => { setStatus('loading') }, [src])

  return (
    <div className="w-12 h-8 rounded shrink-0 overflow-hidden bg-gray-100 flex items-center justify-center">
      {status !== 'error' && (
        <img
          src={src}
          alt=""
          className={`w-full h-full object-cover transition-opacity duration-300 ${status === 'loaded' ? 'opacity-100' : 'opacity-0'}`}
          onLoad={() => setStatus('loaded')}
          onError={() => setStatus('error')}
        />
      )}
      {status === 'loading' && (
        <div className="absolute w-3 h-3 border-2 border-primary/40 border-t-primary rounded-full animate-spin" />
      )}
    </div>
  )
}

export default function Preview() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()

  const [story, setStory] = useState(location.state?.story || null)
  const [currentPage, setCurrentPage] = useState(0)
  const [loadingStory, setLoadingStory] = useState(!location.state?.story)
  const [loadingBook, setLoadingBook] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!story) {
      getStory(id)
        .then(setStory)
        .catch((err) => setError(err.message))
        .finally(() => setLoadingStory(false))
    }
  }, [id, story])

  const handleCreateBook = async () => {
    setLoadingBook(true)
    setError('')
    try {
      await createBook({ storyId: Number(id) })
      navigate(`/order/${id}`, { state: { story } })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoadingBook(false)
    }
  }

  if (loadingStory) return <LoadingSpinner message="동화를 불러오는 중..." />
  if (loadingBook) return <LoadingSpinner message="책을 제작 중입니다... (약 3분 소요)" />
  if (!story && !loadingStory) return (
    <div className="min-h-screen bg-gray-soft">
      <Header />
      <div className="flex flex-col items-center justify-center py-32 px-6 text-center">
        <div className="text-5xl mb-4">😢</div>
        <p className="text-gray-600 font-semibold text-lg mb-2">동화를 찾을 수 없어요</p>
        <p className="text-gray-400 text-sm mb-8">
          {error || '요청하신 동화가 존재하지 않거나 삭제되었습니다.'}
        </p>
        <div className="flex gap-3">
          <button onClick={() => navigate('/')} className="btn-outline">홈으로</button>
          <button onClick={() => navigate('/create')} className="btn-primary">새 동화 만들기</button>
        </div>
      </div>
    </div>
  )

  const pages = story.pages || []
  const page = pages[currentPage]

  return (
    <div className="min-h-screen bg-gray-soft">
      <Header />
      <div className="max-w-4xl mx-auto px-6 py-10">
        <StepIndicator current={2} />

        {/* 동화 헤더 */}
        <div className="text-center mb-8 animate-fadeInUp">
          <span className="text-xs font-bold text-primary bg-primary/10 px-3 py-1 rounded-full">
            {story.theme}
          </span>
          <h1 className="text-3xl font-bold text-gray-900 mt-3">{story.title}</h1>
          <p className="text-gray-500 mt-1">
            주인공: <strong>{story.childName}</strong> ({story.childAge}세) · {pages.length}페이지
          </p>
        </div>

        {/* 책 뷰어 */}
        <div className="card mb-6 overflow-hidden animate-fadeInUp">
          <div className="relative bg-amber-50">
            {/* 페이지 번호 */}
            <div className="absolute top-3 right-3 z-10 text-xs text-gray-400 bg-white/80 px-3 py-1 rounded-full shadow-sm">
              {currentPage + 1} / {pages.length}
            </div>

            {page && (
              <div key={currentPage} className="animate-fadeInUp">
                {/* AI 삽화 */}
                <StoryImage
                  src={buildIllustrationUrl(page.imageDescription, page.pageNumber)}
                  alt={`페이지 ${page.pageNumber} 삽화`}
                  className="w-full aspect-[4/3]"
                  pageIndex={currentPage}
                />

                {/* 동화 텍스트 */}
                <div className="px-8 py-6 text-center bg-amber-50">
                  <p className="text-gray-800 text-lg leading-relaxed font-medium">{page.text}</p>
                </div>
              </div>
            )}
          </div>

          {/* 페이지 네비게이션 */}
          <div className="flex items-center justify-between px-6 py-4 bg-white border-t border-gray-100">
            <button
              onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
              disabled={currentPage === 0}
              className="flex items-center gap-2 text-sm font-medium text-gray-600 hover:text-primary
                         disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              ← 이전 페이지
            </button>
            {/* 페이지 도트 — 24개는 도트 대신 슬라이더 스타일 */}
            <div className="flex gap-1 flex-wrap justify-center max-w-xs">
              {pages.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setCurrentPage(i)}
                  className={`rounded-full transition-all
                    ${i === currentPage
                      ? 'bg-primary w-5 h-2'
                      : 'bg-gray-300 hover:bg-primary/50 w-2 h-2'}`}
                />
              ))}
            </div>
            <button
              onClick={() => setCurrentPage((p) => Math.min(pages.length - 1, p + 1))}
              disabled={currentPage === pages.length - 1}
              className="flex items-center gap-2 text-sm font-medium text-gray-600 hover:text-primary
                         disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              다음 페이지 →
            </button>
          </div>
        </div>

        {/* 전체 페이지 목록 */}
        <details className="card mb-6">
          <summary className="px-6 py-4 cursor-pointer font-semibold text-gray-700 hover:text-primary transition-colors select-none">
            📄 전체 페이지 목록 보기 ({pages.length}페이지)
          </summary>
          <div className="divide-y divide-gray-100">
            {pages.map((p, i) => (
              <button
                key={i}
                onClick={() => setCurrentPage(i)}
                className={`w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors
                  ${i === currentPage ? 'bg-primary/5 border-l-2 border-primary' : ''}`}
              >
                <div className="flex items-center gap-3">
                  <span className={`text-sm font-bold w-6 shrink-0 ${i === currentPage ? 'text-primary' : 'text-gray-400'}`}>
                    {i + 1}
                  </span>
                  <ThumbImage
                    src={buildIllustrationUrl(p.imageDescription, p.pageNumber, { width: 96, height: 64 })}
                  />
                  <p className="text-sm text-gray-700 line-clamp-1 flex-1 text-left">{p.text}</p>
                </div>
              </button>
            ))}
          </div>
        </details>

        {/* 책 제작 안내 */}
        <div className="card p-5 mb-6 animate-fadeInUp flex items-start gap-4">
          <div className="text-3xl shrink-0">📚</div>
          <div>
            <p className="font-semibold text-gray-900">SweetBook 하드커버로 인쇄됩니다</p>
            <p className="text-sm text-gray-500 mt-1">
              AI가 생성한 삽화와 동화 텍스트로 전문 인쇄소에서 고품질 책을 제작해드립니다.
              표지 및 내지 디자인은 자동으로 선택됩니다.
            </p>
          </div>
        </div>

        <ErrorMessage message={error} />

        {/* CTA 버튼 */}
        {story.status === 'BOOK_CREATED' ? (
          <div className="flex flex-col sm:flex-row gap-3 animate-fadeInUp">
            <button onClick={() => navigate('/create')} className="btn-outline flex-1">
              ← 새 동화 만들기
            </button>
            <button
              onClick={() => navigate(`/order/${id}`, { state: { story } })}
              className="btn-primary flex-1 text-base py-4"
            >
              주문하러 가기 →
            </button>
          </div>
        ) : (
          <div className="flex flex-col sm:flex-row gap-3 animate-fadeInUp">
            <button onClick={() => navigate('/create')} className="btn-outline flex-1">
              ← 다시 만들기
            </button>
            <button
              onClick={handleCreateBook}
              disabled={loadingBook}
              className="btn-primary flex-1 text-base py-4"
            >
              이 동화로 책 만들기 →
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
