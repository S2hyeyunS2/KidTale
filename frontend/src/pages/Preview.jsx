import { useEffect, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import StepIndicator from '../components/StepIndicator'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { getStory } from '../api/story'
import { getTemplates, createBook } from '../api/book'

export default function Preview() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()

  const [story, setStory] = useState(location.state?.story || null)
  const [currentPage, setCurrentPage] = useState(0)
  const [coverTemplates, setCoverTemplates] = useState([])
  const [contentTemplates, setContentTemplates] = useState([])
  const [selectedCover, setSelectedCover] = useState('')
  const [selectedContent, setSelectedContent] = useState('')
  const [loadingStory, setLoadingStory] = useState(!location.state?.story)
  const [loadingTemplates, setLoadingTemplates] = useState(true)
  const [loadingBook, setLoadingBook] = useState(false)
  const [error, setError] = useState('')

  // 동화 조회
  useEffect(() => {
    if (!story) {
      getStory(id)
        .then(setStory)
        .catch((err) => setError(err.message))
        .finally(() => setLoadingStory(false))
    }
  }, [id, story])

  // 템플릿 조회
  useEffect(() => {
    Promise.all([getTemplates('cover'), getTemplates('content')])
      .then(([covers, contents]) => {
        setCoverTemplates(covers)
        setContentTemplates(contents)
        if (covers.length > 0) setSelectedCover(covers[0].templateUid)
        if (contents.length > 0) setSelectedContent(contents[0].templateUid)
      })
      .catch(() => {
        // 템플릿 API 오류 시 더미로 fallback (API 키 미설정 등)
        setCoverTemplates([{ templateUid: 'default-cover', templateName: '기본 표지' }])
        setContentTemplates([{ templateUid: 'default-content', templateName: '기본 내지' }])
        setSelectedCover('default-cover')
        setSelectedContent('default-content')
      })
      .finally(() => setLoadingTemplates(false))
  }, [])

  const handleCreateBook = async () => {
    if (!selectedCover || !selectedContent) {
      setError('표지와 내지 템플릿을 선택해주세요.')
      return
    }
    setLoadingBook(true)
    setError('')
    try {
      await createBook({
        storyId: Number(id),
        coverTemplateUid: selectedCover,
        contentTemplateUid: selectedContent,
      })
      navigate(`/order/${id}`, { state: { story } })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoadingBook(false)
    }
  }

  if (loadingStory) return <LoadingSpinner message="동화를 불러오는 중..." />
  if (loadingBook) return <LoadingSpinner message="책을 제작 중입니다... (약 30초 소요)" />
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

  // Pollinations.ai — 무료 AI 이미지 생성 (API 키 불필요)
  const buildIllustrationUrl = (imageDescription, pageNum) => {
    const desc = imageDescription
      ? `${imageDescription} children book illustration watercolor colorful`
      : `fairy tale scene page ${pageNum} children illustration`
    return `https://image.pollinations.ai/prompt/${encodeURIComponent(desc)}?width=800&height=500&model=flux&nologo=true&seed=${pageNum}`
  }

  const buildCoverUrl = () => {
    const desc = `${story.title} ${story.theme} children fairy tale book cover illustration colorful cute`
    return `https://image.pollinations.ai/prompt/${encodeURIComponent(desc)}?width=800&height=800&model=flux&nologo=true`
  }

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
          {/* 페이지 컨텐츠 */}
          <div className="relative bg-amber-50">
            {/* 페이지 번호 */}
            <div className="absolute top-3 right-3 z-10 text-xs text-gray-400 bg-white/80 px-3 py-1 rounded-full shadow-sm">
              {currentPage + 1} / {pages.length}
            </div>

            {page && (
              <div key={currentPage} className="animate-fadeInUp">
                {/* AI 삽화 */}
                <div className="relative w-full aspect-[16/10] bg-gradient-to-br from-amber-50 to-rose-50 overflow-hidden">
                  <img
                    src={buildIllustrationUrl(page.imageDescription, page.pageNumber)}
                    alt={`페이지 ${page.pageNumber} 삽화`}
                    className="w-full h-full object-cover"
                    loading="lazy"
                    onError={(e) => {
                      e.target.style.display = 'none'
                      e.target.nextSibling.style.display = 'flex'
                    }}
                  />
                  {/* 이미지 로드 실패 시 fallback */}
                  <div className="hidden absolute inset-0 items-center justify-center bg-gradient-to-br from-primary/10 to-rose-100">
                    <span className="text-6xl">
                      {['🌟','🌈','🎨','✨','🌸','🦋','🌻','🎭','🏰','🎪'][currentPage % 10]}
                    </span>
                  </div>
                </div>

                {/* 동화 텍스트 */}
                <div className="px-8 py-6 text-center">
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
            {/* 페이지 도트 */}
            <div className="flex gap-1.5">
              {pages.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setCurrentPage(i)}
                  className={`w-2 h-2 rounded-full transition-all
                    ${i === currentPage ? 'bg-primary w-5' : 'bg-gray-300 hover:bg-primary/50'}`}
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
                  <span className={`text-sm font-bold w-5 shrink-0 ${i === currentPage ? 'text-primary' : 'text-gray-400'}`}>
                    {i + 1}
                  </span>
                  <img
                    src={buildIllustrationUrl(p.imageDescription, p.pageNumber)}
                    alt=""
                    className="w-12 h-8 object-cover rounded shrink-0"
                    loading="lazy"
                  />
                  <p className="text-sm text-gray-700 line-clamp-1 flex-1 text-left">{p.text}</p>
                </div>
              </button>
            ))}
          </div>
        </details>

        {/* 템플릿 선택 */}
        {!loadingTemplates && (
          <div className="card p-6 mb-6 animate-fadeInUp">
            <h2 className="font-bold text-gray-900 mb-1">📚 책 디자인 선택</h2>
            <p className="text-gray-500 text-sm mb-5">표지와 내지 스타일을 선택해주세요</p>

            {/* 표지 템플릿 */}
            {coverTemplates.length > 0 && (
              <div className="mb-5">
                <p className="text-sm font-semibold text-gray-700 mb-3">표지 스타일</p>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                  {coverTemplates.map((t) => (
                    <button
                      key={t.templateUid}
                      onClick={() => setSelectedCover(t.templateUid)}
                      className={`p-3 rounded-xl border-2 text-sm font-medium transition-all text-center
                        ${selectedCover === t.templateUid
                          ? 'border-primary bg-primary/5 text-primary'
                          : 'border-gray-200 text-gray-600 hover:border-primary/40'}`}
                    >
                      <div className="text-2xl mb-1">📖</div>
                      {t.templateName}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* 내지 템플릿 */}
            {contentTemplates.length > 0 && (
              <div>
                <p className="text-sm font-semibold text-gray-700 mb-3">내지 스타일</p>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                  {contentTemplates.map((t) => (
                    <button
                      key={t.templateUid}
                      onClick={() => setSelectedContent(t.templateUid)}
                      className={`p-3 rounded-xl border-2 text-sm font-medium transition-all text-center
                        ${selectedContent === t.templateUid
                          ? 'border-primary bg-primary/5 text-primary'
                          : 'border-gray-200 text-gray-600 hover:border-primary/40'}`}
                    >
                      <div className="text-2xl mb-1">📄</div>
                      {t.templateName}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        <ErrorMessage message={error} />

        {/* CTA 버튼 */}
        <div className="flex flex-col sm:flex-row gap-3 animate-fadeInUp">
          <button
            onClick={() => navigate('/create')}
            className="btn-outline flex-1"
          >
            ← 다시 만들기
          </button>
          <button
            onClick={handleCreateBook}
            disabled={loadingBook || story.status !== 'DRAFT'}
            className="btn-primary flex-1 text-base py-4"
          >
            {story.status !== 'DRAFT' ? '이미 책이 생성되었습니다' : '이 동화로 책 만들기 →'}
          </button>
        </div>

        {story.status === 'BOOK_CREATED' && (
          <div className="mt-4 text-center">
            <button
              onClick={() => navigate(`/order/${id}`, { state: { story } })}
              className="btn-primary w-full py-4 text-base"
            >
              주문하러 가기 →
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
