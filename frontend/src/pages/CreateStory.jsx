import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import StepIndicator from '../components/StepIndicator'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'
import { createStory } from '../api/story'

const THEME_OPTIONS = [
  { value: '우주 탐험', emoji: '🚀' },
  { value: '바닷속 마법', emoji: '🐠' },
  { value: '숲속 모험', emoji: '🌲' },
  { value: '공룡 왕국', emoji: '🦕' },
  { value: '마법사 학교', emoji: '🧙' },
  { value: '요리 대모험', emoji: '🍳' },
  { value: '동물 농장', emoji: '🐄' },
  { value: '공주와 왕자', emoji: '👑' },
  { value: '로봇 친구', emoji: '🤖' },
  { value: '직접 입력', emoji: '✏️' },
]

const AGE_OPTIONS = Array.from({ length: 10 }, (_, i) => i + 1) // 1~10세

export default function CreateStory() {
  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({
    childName: '',
    childAge: '',
    theme: location.state?.theme || '',
  })
  const [customTheme, setCustomTheme] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const selectedTheme = form.theme === '직접 입력' ? customTheme : form.theme

  const isValid =
    form.childName.trim().length > 0 &&
    form.childAge !== '' &&
    selectedTheme.trim().length > 0

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!isValid) return
    setLoading(true)
    setError('')
    try {
      const story = await createStory({
        childName: form.childName.trim(),
        childAge: Number(form.childAge),
        theme: selectedTheme.trim(),
      })
      navigate(`/preview/${story.id}`, { state: { story } })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-soft">
      {loading && <LoadingSpinner message="AI가 동화를 쓰고 있어요... (약 30초 소요)" />}
      <Header />

      <div className="max-w-2xl mx-auto px-6 py-10">
        <StepIndicator current={1} />

        <div className="card p-8 animate-fadeInUp">
          <div className="text-center mb-8">
            <div className="text-4xl mb-3">✍️</div>
            <h1 className="text-2xl font-bold text-gray-900">아이 정보를 입력해주세요</h1>
            <p className="text-gray-500 mt-2 text-sm">입력하신 정보로 세상에 하나뿐인 동화를 만들어드립니다</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 아이 이름 */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                아이 이름 <span className="text-primary">*</span>
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="예) 지우, 수아, 민준"
                maxLength={20}
                value={form.childName}
                onChange={(e) => setForm({ ...form, childName: e.target.value })}
              />
            </div>

            {/* 나이 */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                나이 <span className="text-primary">*</span>
              </label>
              <div className="grid grid-cols-5 gap-2">
                {AGE_OPTIONS.map((age) => (
                  <button
                    key={age}
                    type="button"
                    onClick={() => setForm({ ...form, childAge: age })}
                    className={`py-2.5 rounded-xl text-sm font-semibold border-2 transition-all
                      ${form.childAge === age
                        ? 'border-primary bg-primary text-white'
                        : 'border-gray-200 text-gray-600 hover:border-primary/50'}`}
                  >
                    {age}세
                  </button>
                ))}
              </div>
            </div>

            {/* 테마 선택 */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                동화 테마 <span className="text-primary">*</span>
              </label>
              <div className="grid grid-cols-2 gap-2">
                {THEME_OPTIONS.map((t) => (
                  <button
                    key={t.value}
                    type="button"
                    onClick={() => setForm({ ...form, theme: t.value })}
                    className={`flex items-center gap-2 px-4 py-3 rounded-xl border-2 text-sm font-medium transition-all text-left
                      ${form.theme === t.value
                        ? 'border-primary bg-primary/5 text-primary'
                        : 'border-gray-200 text-gray-600 hover:border-primary/40'}`}
                  >
                    <span className="text-lg">{t.emoji}</span>
                    {t.value}
                  </button>
                ))}
              </div>
              {form.theme === '직접 입력' && (
                <input
                  type="text"
                  className="input-field mt-3"
                  placeholder="원하는 테마를 직접 입력해주세요"
                  maxLength={30}
                  value={customTheme}
                  onChange={(e) => setCustomTheme(e.target.value)}
                  autoFocus
                />
              )}
            </div>

            <ErrorMessage message={error} />

            {/* 미리보기 */}
            {isValid && (
              <div className="rounded-xl bg-primary/5 border border-primary/20 p-4 text-sm text-gray-700 animate-fadeInUp">
                <p className="font-semibold text-primary mb-1">✨ 이런 동화가 만들어져요</p>
                <p>
                  <strong>{form.childName}</strong>({form.childAge}세)이(가) 주인공인{' '}
                  <strong>'{selectedTheme}'</strong> 테마의 10페이지 동화
                </p>
              </div>
            )}

            <button
              type="submit"
              disabled={!isValid || loading}
              className="btn-primary w-full text-base py-4"
            >
              동화 생성하기 ✨
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
