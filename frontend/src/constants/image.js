// ─── 이미지 생성 공통 상수 ────────────────────────────────────────────────────

export const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/** 성인·10대 묘사를 차단하는 negative prompt */
export const NEGATIVE_PROMPT = encodeURIComponent(
  'adult, teenager, teen, mature, grown up, elderly, old person, woman, man, sexy, realistic photo'
)

/** 지브리/수채화 스타일 공통 suffix */
export const GHIBLI_STYLE =
  'Studio Ghibli anime style, soft watercolor, warm pastel colors, children storybook illustration, kid-friendly, highly detailed'

/**
 * 나이대별 지브리 스타일 프롬프트 생성
 * @param {number|undefined} age
 */
export function ghibliStyle(age) {
  let ageDesc, bodyDesc
  if (!age) {
    ageDesc = 'young child'
    bodyDesc = 'small round face, child body'
  } else if (age <= 3) {
    ageDesc = `${age} year old toddler`
    bodyDesc = 'toddler body, baby face, chubby cheeks, very small child, short stature'
  } else if (age <= 6) {
    ageDesc = `${age} year old preschooler`
    bodyDesc = 'small child body, round chubby face, big eyes, short stature, preschool age'
  } else if (age <= 10) {
    ageDesc = `${age} year old child`
    bodyDesc = 'child body, young kid face, elementary school age'
  } else {
    ageDesc = `${age} year old child`
    bodyDesc = 'child body, young face'
  }
  return `${ageDesc} protagonist, ${bodyDesc}, ${GHIBLI_STYLE}`
}

/**
 * 동화 페이지 삽화 URL 목록 생성 (폴백 포함)
 * @param {string} imageDescription
 * @param {number} pageNum
 * @param {number|undefined} childAge
 * @param {{ width?: number, height?: number }} options
 */
export function buildIllustrationUrls(imageDescription, pageNum, childAge, { width = 512, height = 512 } = {}) {
  const style = ghibliStyle(childAge)
  const base = imageDescription
    ? `${imageDescription}, ${style}`
    : `children storybook scene ${pageNum}, ${style}`

  const encoded = encodeURIComponent(base)
  const simpleEncoded = encodeURIComponent(`children storybook scene ${pageNum}, ${style}`)
  const neg = `&negative_prompt=${NEGATIVE_PROMPT}`

  return [
    `${API_BASE}/api/images/generate?prompt=${encoded}&seed=${pageNum}&width=${width}&height=${height}${neg}`,
    `${API_BASE}/api/images/generate?prompt=${encoded}&seed=${pageNum + 50}&width=${width}&height=${height}${neg}`,
    `${API_BASE}/api/images/generate?prompt=${simpleEncoded}&seed=${pageNum}&width=${width}&height=${height}${neg}`,
  ]
}
