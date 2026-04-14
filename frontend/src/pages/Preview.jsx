import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'
import StepIndicator from '../components/StepIndicator'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { getStory } from '../api/story'
import { createBook } from '../api/book'

// ─── 이미지 URL ────────────────────────────────────────────────────────────────

function buildIllustrationUrls(imageDescription, pageNum, { width = 800, height = 800 } = {}) {
  const base = imageDescription
    ? `children storybook illustration, ${imageDescription}, soft watercolor, cute, vibrant`
    : `children storybook watercolor illustration scene ${pageNum}`
  const encoded = encodeURIComponent(base)
  const fallbackEncoded = encodeURIComponent(`cute children storybook scene ${pageNum} colorful watercolor`)

  return [
    `https://image.pollinations.ai/prompt/${encoded}?width=${width}&height=${height}&seed=${pageNum}&model=flux&nologo=true`,
    `https://image.pollinations.ai/prompt/${encoded}?width=${width}&height=${height}&seed=${pageNum + 101}&model=flux&nologo=true`,
    `https://image.pollinations.ai/prompt/${fallbackEncoded}?width=${width}&height=${height}&seed=${pageNum + 202}&model=flux&nologo=true`,
  ]
}

// ─── StoryImage 컴포넌트 ───────────────────────────────────────────────────────

const TIMEOUT_MS = 90_000   // 90초 — Pollinations.ai 생성 시간 대응

function StoryImage({ sources, alt, className, fallbackIndex = 0 }) {
  const [status, setStatus] = useState('loading')
  const [sourceIndex, setSourceIndex] = useState(0)
  const [retryNonce, setRetryNonce] = useState(0)
  const timerRef = useRef(null)
  const sourceCount = Math.max(1, sources?.length || 0)
  const activeBaseSrc = sources?.[sourceIndex] || sources?.[0] || ''
  const activeSrc = activeBaseSrc
      ? `${activeBaseSrc}${activeBaseSrc.includes('?') ? '&' : '?'}retry=${retryNonce}`
      : ''

  useEffect(() => {
    setStatus('loading')
    setSourceIndex(0)
    setRetryNonce(0)
    timerRef.current = setTimeout(() => setStatus('error'), TIMEOUT_MS)
    return () => clearTimeout(timerRef.current)
  }, [sources])

  const tryNextSource = () => {
    clearTimeout(timerRef.current)
    if (sourceIndex < sourceCount - 1) {
      setSourceIndex((idx) => idx + 1)
      setStatus('loading')
      timerRef.current = setTimeout(() => setStatus('error'), TIMEOUT_MS)
      return
    }
    setStatus('error')
  }

  const handleLoad = () => {
    clearTimeout(timerRef.current)
    setStatus('loaded')
  }
  const handleError = () => {
    tryNextSource()
  }
  const handleRetry = () => {
    clearTimeout(timerRef.current)
    setSourceIndex(0)
    setRetryNonce((n) => n + 1)
    setStatus('loading')
    timerRef.current = setTimeout(() => setStatus('error'), TIMEOUT_MS)
  }

  return (
      <div className={`relative overflow-hidden bg-amber-50 ${className}`}>
        {/* 로딩 스켈레톤 */}
        {status === 'loading' && (
            <div className="absolute inset-0 flex flex-col items-center justify-center bg-gradient-to-br from-rose-50 to-amber-50">
              <div className="w-10 h-10 border-4 border-primary/30 border-t-primary rounded-full animate-spin mb-3" />
              <p className="text-xs text-gray-400">그림 생성 중...</p>
              <p className="text-xs text-gray-300 mt-1">최대 90초 소요될 수 있어요</p>
            </div>
        )}

        <img
            key={`${sourceIndex}-${retryNonce}`}
            src={activeSrc}
            alt={alt}
            className={`w-full h-full object-cover transition-opacity duration-700
          ${status === 'loaded' ? 'opacity-100' : 'opacity-0 absolute inset-0'}`}
            onLoad={handleLoad}
            onError={handleError}
        />

        {/* 실패 fallback */}
        {status === 'error' && (
            <div className="absolute inset-0 flex flex-col items-center justify-center bg-gradient-to-br from-primary/5 to-rose-50 gap-3">
          <span className="text-5xl">
            {['🌟', '🌈', '🎨', '✨', '🌸', '🦋', '🌻', '🎭', '🏰', '🎪'][fallbackIndex % 10]}
          </span>
              <button
                  onClick={handleRetry}
                  className="text-xs text-primary border border-primary/30 px-3 py-1 rounded-full hover:bg-primary/5 transition-colors"
              >
                다시 시도
              </button>
            </div>
        )}
      </div>
  )
}

// ─── 썸네일 ────────────────────────────────────────────────────────────────────

function ThumbImage({ src, pageNum }) {
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState(false)
  useEffect(() => { setLoaded(false); setError(false) }, [src])

  return (
      <div className="w-full h-full bg-gray-100 rounded overflow-hidden">
        {!error && (
            <img
                src={src}
                alt=""
                className={`w-full h-full object-cover transition-opacity duration-300 ${loaded ? 'opacity-100' : 'opacity-0'}`}
                onLoad={() => setLoaded(true)}
                onError={() => setError(true)}
            />
        )}
        {!loaded && !error && (
            <div className="w-full h-full flex items-center justify-center">
              <div className="w-3 h-3 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
            </div>
        )}
      </div>
  )
}

// ─── Preview 페이지 ────────────────────────────────────────────────────────────

export default function Preview() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const thumbsRef = useRef(null)

  const [story, setStory] = useState(location.state?.story || null)
  const previewOnly = location.state?.previewOnly ?? false
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

  // 썸네일 선택 시 해당 항목이 보이도록 스크롤
  useEffect(() => {
    if (!thumbsRef.current) return
    const el = thumbsRef.current.querySelector(`[data-page="${currentPage}"]`)
    el?.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' })
  }, [currentPage])

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
  const canPrev = currentPage > 0
  const canNext = currentPage < pages.length - 1

  return (
      <div className="min-h-screen bg-gray-soft">
        <Header />
        <div className="max-w-6xl mx-auto px-4 py-8">
          <StepIndicator current={2} />

          {/* 동화 헤더 */}
          <div className="text-center mb-6 animate-fadeInUp">
          <span className="text-xs font-bold text-primary bg-primary/10 px-3 py-1 rounded-full">
            {story.theme}
          </span>
            <h1 className="text-2xl font-bold text-gray-900 mt-3">{story.title}</h1>
            <p className="text-gray-400 text-sm mt-1">
              주인공: <strong className="text-gray-600">{story.childName}</strong> ({story.childAge}세) · {pages.length}페이지
            </p>
          </div>

          {/* ── 책 뷰어 ──────────────────────────────────────────────── */}
          <div className="animate-fadeInUp">
            {/* 책 본체 */}
            <div
                className="relative bg-white rounded-2xl overflow-hidden"
                style={{ boxShadow: '0 20px 60px rgba(0,0,0,0.18), 4px 0 12px rgba(0,0,0,0.08)' }}
            >
              {page && (
                  <div className="flex flex-col md:flex-row min-h-[520px]">

                    {/* 왼쪽: 그림 */}
                    <StoryImage
                        key={currentPage}
                        sources={buildIllustrationUrls(page.imageDescription, page.pageNumber)}
                        alt={`페이지 ${page.pageNumber} 삽화`}
                        className="w-full md:w-1/2 min-h-[320px] md:min-h-[520px]"
                        fallbackIndex={currentPage}
                    />

                    {/* 바인딩 선 (데스크탑) */}
                    <div className="hidden md:block w-px bg-gradient-to-b from-gray-100 via-gray-300 to-gray-100 shrink-0" />

                    {/* 오른쪽: 텍스트 */}
                    <div className="w-full md:w-1/2 flex flex-col justify-center px-8 md:px-12 py-10 bg-white">
                      <p className="text-xs font-semibold text-primary/60 tracking-widest uppercase mb-6">
                        Page {page.pageNumber}
                      </p>
                      <p className="text-gray-800 text-lg md:text-xl leading-loose font-medium whitespace-pre-line">
                        {page.text}
                      </p>
                    </div>
                  </div>
              )}

              {/* 페이지 번호 배지 */}
              <div className="absolute top-4 right-4 text-xs text-gray-400 bg-white/90 px-3 py-1 rounded-full shadow-sm">
                {currentPage + 1} / {pages.length}
              </div>
            </div>

            {/* 이전 / 다음 버튼 */}
            <div className="flex items-center justify-between mt-5 px-2">
              <button
                  onClick={() => setCurrentPage((p) => p - 1)}
                  disabled={!canPrev}
                  className="flex items-center gap-2 px-5 py-2.5 rounded-xl font-semibold text-sm
                         border-2 border-gray-200 text-gray-600 hover:border-primary hover:text-primary
                         disabled:opacity-30 disabled:cursor-not-allowed transition-all"
              >
                ← 이전
              </button>

              <span className="text-sm text-gray-400">
              {currentPage + 1} / {pages.length} 페이지
            </span>

              <button
                  onClick={() => setCurrentPage((p) => p + 1)}
                  disabled={!canNext}
                  className="flex items-center gap-2 px-5 py-2.5 rounded-xl font-semibold text-sm
                         border-2 border-gray-200 text-gray-600 hover:border-primary hover:text-primary
                         disabled:opacity-30 disabled:cursor-not-allowed transition-all"
              >
                다음 →
              </button>
            </div>
          </div>

          {/* ── 썸네일 네비게이션 ─────────────────────────────────────── */}
          <div className="mt-6 animate-fadeInUp">
            <div
                ref={thumbsRef}
                className="flex gap-2 overflow-x-auto pb-3 scroll-smooth"
                style={{ scrollbarWidth: 'thin' }}
            >
              {pages.map((p, i) => (
                  <button
                      key={i}
                      data-page={i}
                      onClick={() => setCurrentPage(i)}
                      className={`shrink-0 flex flex-col items-center gap-1 transition-all
                  ${i === currentPage ? 'opacity-100 scale-105' : 'opacity-50 hover:opacity-80'}`}
                  >
                    <div className={`w-16 h-16 rounded-lg overflow-hidden border-2 transition-colors
                  ${i === currentPage ? 'border-primary' : 'border-transparent'}`}>
                      <ThumbImage
                          src={buildIllustrationUrls(p.imageDescription, p.pageNumber, { width: 128, height: 128 })[0]}
                          pageNum={p.pageNumber}
                      />
                    </div>
                    <span className={`text-xs font-medium ${i === currentPage ? 'text-primary' : 'text-gray-400'}`}>
                  {i + 1}
                </span>
                  </button>
              ))}
            </div>
          </div>

          {/* ── 책 제작 안내 + CTA ────────────────────────────────────── */}
          {!previewOnly && (
            <div className="mt-8 animate-fadeInUp">
              <div className="card p-5 mb-5 flex items-start gap-4">
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

              {story.status === 'BOOK_CREATED' ? (
                <div className="flex flex-col sm:flex-row gap-3">
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
                <div className="flex flex-col sm:flex-row gap-3">
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
          )}
        </div>
        <Footer />
      </div>
  )
}