import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'
import { getStories } from '../api/story'

const FALLBACK_STORIES = [
  { id: null, title: '지우의 별나라 여행', childName: '지우', theme: '우주 탐험', emoji: '🚀' },
  { id: null, title: '수아의 바닷속 친구들', childName: '수아', theme: '바닷속 마법', emoji: '🐠' },
  { id: null, title: '민준이의 마법 레시피', childName: '민준', theme: '숲속 요리사', emoji: '🍄' },
]

const THEMES = [
  { label: '우주 탐험',    emoji: '🚀', desc: '별나라 친구들과의 모험' },
  { label: '바닷속 마법',  emoji: '🐠', desc: '신비로운 바다 세계 탐험' },
  { label: '숲속 모험',    emoji: '🌲', desc: '동물 친구들과 숲 여행' },
  { label: '공룡 왕국',    emoji: '🦕', desc: '공룡들이 사는 땅으로' },
  { label: '마법사 학교',  emoji: '🧙', desc: '마법을 배우는 특별한 학교' },
  { label: '요리 대모험',  emoji: '🍳', desc: '세상에서 가장 맛있는 요리' },
]

const THEME_EMOJI_MAP = {
  '우주': '🚀', '별': '⭐', '바다': '🐠', '숲': '🌲', '공룡': '🦕',
  '마법': '🧙', '요리': '🍳', '동물': '🐾', '여행': '✈️', '모험': '🗺️',
}

function storyEmoji(theme = '') {
  const matched = Object.entries(THEME_EMOJI_MAP).find(([key]) => theme.includes(key))
  return matched ? matched[1] : '📖'
}

export default function Home() {
  const navigate = useNavigate()
  const [sampleStories, setSampleStories] = useState([])
  const [storiesLoading, setStoriesLoading] = useState(true)

  useEffect(() => {
    getStories()
      .then((data) => {
        if (data && data.length > 0) {
          setSampleStories(data.slice(0, 3))
        } else {
          setSampleStories(FALLBACK_STORIES)
        }
      })
      .catch(() => setSampleStories(FALLBACK_STORIES))
      .finally(() => setStoriesLoading(false))
  }, [])

  return (
    <div className="min-h-screen bg-white">
      <Header />

      {/* Hero */}
      <section className="relative bg-gradient-to-br from-pink-50 via-white to-rose-50 overflow-hidden">
        <div className="absolute top-10 left-10 w-40 h-40 bg-primary/5 rounded-full blur-3xl" />
        <div className="absolute bottom-10 right-10 w-60 h-60 bg-rose-200/20 rounded-full blur-3xl" />
        <div className="max-w-6xl mx-auto px-6 py-20 flex flex-col lg:flex-row items-center gap-12">
          <div className="flex-1 animate-fadeInUp">
            <p className="text-primary font-semibold text-sm tracking-widest uppercase mb-3">
              AI 맞춤 동화책 제작 서비스
            </p>
            <h1 className="text-4xl lg:text-5xl font-bold text-gray-900 leading-tight mb-5">
              우리 아이만을 위한<br />
              <span className="text-primary">특별한 동화책</span>을<br />
              만들어보세요
            </h1>
            <p className="text-gray-500 text-lg leading-relaxed mb-8">
              아이 이름과 테마를 입력하면 AI가 세상에 하나뿐인<br />
              동화를 만들고, 실제 책으로 인쇄해 배송해드립니다.
            </p>
            <div className="flex flex-wrap gap-3">
              <button onClick={() => navigate('/create')} className="btn-primary text-base px-10 py-4">
                동화책 만들기 →
              </button>
              <button
                onClick={() => document.getElementById('samples').scrollIntoView({ behavior: 'smooth' })}
                className="btn-outline text-base px-10 py-4"
              >
                예시 보기
              </button>
            </div>
          </div>
          {/* 책 일러스트 카드 */}
          <div className="flex-1 flex justify-center">
            <div className="relative">
              <div className="w-72 h-96 bg-gradient-to-br from-primary to-primary-dark rounded-card shadow-2xl flex flex-col items-center justify-center text-white p-8 rotate-3 hover:rotate-0 transition-transform duration-300">
                <div className="text-6xl mb-4">📖</div>
                <p className="text-xl font-bold text-center">지우의 별나라 여행</p>
                <p className="text-white/70 text-sm mt-2">AI 동화 × SweetBook 인쇄</p>
                <div className="mt-6 flex gap-2">
                  {['🌟','✨','🚀','⭐'].map((e, i) => (
                    <span key={i} className="text-lg">{e}</span>
                  ))}
                </div>
              </div>
              <div className="absolute -bottom-4 -right-4 w-72 h-96 bg-rose-100 rounded-card -z-10 rotate-6" />
            </div>
          </div>
        </div>
      </section>

      {/* 특징 */}
      <section className="py-20 bg-gray-soft">
        <div className="max-w-6xl mx-auto px-6">
          <h2 className="section-title text-center mb-3">KidTale만의 특별함</h2>
          <p className="text-gray-500 text-center mb-12">3단계로 완성하는 세상에 하나뿐인 동화책</p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[
              { step: '01', icon: '✍️', title: '아이 정보 입력', desc: '이름·나이·좋아하는 테마만 입력하세요. 3분이면 충분합니다.' },
              { step: '02', icon: '🤖', title: 'AI 동화 생성',  desc: 'Gemini AI가 24페이지 분량의 맞춤 동화를 즉시 만들어드립니다.' },
              { step: '03', icon: '📬', title: '실제 책으로 배송', desc: 'SweetBook 전문 인쇄소에서 고품질 하드커버 책을 제작해 배송합니다.' },
            ].map((item) => (
              <div key={item.step} className="card p-8 text-center hover:shadow-card-hover transition-shadow duration-200">
                <div className="text-xs font-bold text-primary/50 tracking-widest mb-3">STEP {item.step}</div>
                <div className="text-4xl mb-4">{item.icon}</div>
                <h3 className="font-bold text-lg text-gray-900 mb-2">{item.title}</h3>
                <p className="text-gray-500 text-sm leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 테마 선택 */}
      <section className="py-20 bg-white">
        <div className="max-w-6xl mx-auto px-6">
          <h2 className="section-title text-center mb-3">인기 동화 테마</h2>
          <p className="text-gray-500 text-center mb-12">어떤 테마든 아이 이름을 넣어 특별한 이야기로 만들어드립니다</p>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {THEMES.map((t) => (
              <button
                key={t.label}
                onClick={() => navigate('/create', { state: { theme: t.label } })}
                className="card p-6 text-left hover:shadow-card-hover hover:border-primary/20 border border-transparent transition-all duration-200 group"
              >
                <div className="text-3xl mb-3">{t.emoji}</div>
                <p className="font-semibold text-gray-900 group-hover:text-primary transition-colors">{t.label}</p>
                <p className="text-gray-400 text-sm mt-1">{t.desc}</p>
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* 샘플 동화 */}
      <section id="samples" className="py-20 bg-gray-soft">
        <div className="max-w-6xl mx-auto px-6">
          <h2 className="section-title text-center mb-3">실제 생성된 동화들</h2>
          <p className="text-gray-500 text-center mb-12">AI가 만든 동화를 미리 감상해보세요</p>

          {storiesLoading ? (
            <div className="flex justify-center gap-3 py-8">
              <div className="dot-bounce">
                <span /><span /><span />
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {sampleStories.map((s, idx) => {
                const emoji = s.emoji || storyEmoji(s.theme)
                const canPreview = !!s.id
                return (
                  <div
                    key={s.id ?? idx}
                    onClick={() => canPreview && navigate(`/preview/${s.id}`, { state: { story: s, previewOnly: true } })}
                    className={`card overflow-hidden transition-all duration-200
                      ${canPreview ? 'cursor-pointer hover:shadow-card-hover hover:-translate-y-1' : 'cursor-default'}`}
                  >
                    <div className="h-52 bg-gradient-to-br from-primary/10 to-rose-100 relative overflow-hidden">
                      <SampleCoverImage
                        title={s.title}
                        theme={s.theme}
                        emoji={emoji}
                        seed={(s.id ?? idx) + 200}
                        childAge={s.childAge}
                      />
                    </div>
                    <div className="p-5">
                      <span className="text-xs font-semibold text-primary bg-primary/10 px-2 py-1 rounded-full">
                        {s.theme}
                      </span>
                      <h3 className="font-bold text-gray-900 text-lg mt-3 mb-1">{s.title}</h3>
                      <p className="text-gray-400 text-sm">주인공: {s.childName}</p>
                      {canPreview && (
                        <p className="mt-3 text-primary text-sm font-semibold">미리보기 →</p>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 bg-gradient-to-r from-primary to-primary-light text-white text-center">
        <div className="max-w-2xl mx-auto px-6">
          <p className="text-white/80 text-sm font-semibold tracking-widest uppercase mb-4">Limited Story</p>
          <h2 className="text-3xl lg:text-4xl font-bold mb-5">
            오늘, 우리 아이의 이름이 들어간<br />동화책을 만들어보세요
          </h2>
          <p className="text-white/80 mb-8">세상에 단 하나뿐인 선물 · AI 생성 · 전문 인쇄 배송</p>
          <button
            onClick={() => navigate('/create')}
            className="bg-white text-primary font-bold px-10 py-4 rounded-full hover:bg-gray-50 transition-colors text-base"
          >
            지금 바로 만들기 →
          </button>
        </div>
      </section>

      <Footer />
    </div>
  )
}

// 샘플 동화 커버 이미지 — 로드 실패 시 이모지 fallback
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 지브리/애니 스타일 공통 suffix — cute child 명시로 성인 이미지 방지
const STYLE = 'cute young child protagonist, Studio Ghibli anime style, soft watercolor, warm pastel colors, children storybook illustration, kid-friendly, highly detailed'

// 테마 → 영어 핵심 키워드 매핑
const THEME_KEYWORDS = {
  '우주 탐험':   `child astronaut flying through space, colorful stars and planets, magical rocket, ${STYLE}`,
  '바닷속 마법': `child swimming underwater with cute fish and sea creatures, glowing ocean magic, ${STYLE}`,
  '숲속 모험':   `child exploring enchanted forest with woodland animals, magical trees, ${STYLE}`,
  '숲속 요리사': `child cooking magical recipe in forest kitchen, flying ingredients, cute animals watching, ${STYLE}`,
  '공룡 왕국':   `child riding friendly colorful dinosaur in prehistoric jungle, ${STYLE}`,
  '마법사 학교': `child wizard casting spells in magical school, glowing wand and stars, ${STYLE}`,
  '요리 대모험': `child on cooking adventure, giant magical food ingredients, delicious fantasy world, ${STYLE}`,
  '동물 농장':   `child playing with friendly farm animals, sunny meadow, ${STYLE}`,
  '공주와 왕자': `child dressed as princess or prince in magical castle, fairy tale, ${STYLE}`,
  '로봇 친구':   `child playing with cute friendly robot, futuristic colorful world, ${STYLE}`,
}

function themePrompt(theme, childAge) {
  const agePrefix = childAge ? `${childAge} year old child` : 'young child'
  const base = THEME_KEYWORDS[theme] || `children storybook, ${theme} adventure, ${STYLE}`
  return base.replace(/^(cute young child|child)/, agePrefix)
}

const HOME_NEGATIVE = encodeURIComponent('adult, teenager, teen, mature, grown up, elderly, old person, woman, man, sexy, realistic photo')

function SampleCoverImage({ title, theme, emoji, seed = 1, childAge }) {
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState(false)
  const prompt = encodeURIComponent(themePrompt(theme, childAge))
  const src = `${API_BASE}/api/images/generate?prompt=${prompt}&seed=${seed}&width=400&height=300&negative_prompt=${HOME_NEGATIVE}`

  if (error) {
    return (
      <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary/10 to-rose-100">
        <span className="text-6xl">{emoji}</span>
      </div>
    )
  }
  return (
    <div className="w-full h-full relative bg-gradient-to-br from-primary/10 to-rose-100">
      {/* 로딩 스켈레톤 */}
      {!loaded && (
        <div className="absolute inset-0 flex flex-col items-center justify-center gap-2">
          <div className="w-8 h-8 border-4 border-primary/30 border-t-primary rounded-full animate-spin" />
          <p className="text-xs text-gray-400">그림 생성 중...</p>
        </div>
      )}
      <img
        src={src}
        alt={title}
        className={`w-full h-full object-cover transition-opacity duration-500 ${loaded ? 'opacity-100' : 'opacity-0'}`}
        onLoad={() => setLoaded(true)}
        onError={() => setError(true)}
      />
    </div>
  )
}
