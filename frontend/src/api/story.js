import api from './index'

/**
 * 동화 목록 조회 (GET /api/stories) — 최근 10개
 */
export const getStories = () =>
  api.get('/api/stories').then((res) => res.data.data)

/**
 * 동화 생성 (POST /api/stories)
 * @param {{ childName: string, childAge: number, theme: string }} data
 */
export const createStory = (data) =>
  api.post('/api/stories', data).then((res) => res.data.data)

/**
 * 내 동화 목록 조회 (GET /api/stories/my) — 로그인 필요
 */
export const getMyStories = () =>
  api.get('/api/stories/my').then((res) => res.data.data)

/**
 * 동화 단건 조회 (GET /api/stories/:id)
 * @param {number} id
 */
export const getStory = (id) =>
  api.get(`/api/stories/${id}`).then((res) => res.data.data)

/**
 * 특정 페이지 텍스트 수정 (PATCH /api/stories/:id/pages/:pageNumber)
 * @param {number} storyId
 * @param {number} pageNumber  1-based
 * @param {string} text
 */
export const updatePageText = (storyId, pageNumber, text) =>
  api.patch(`/api/stories/${storyId}/pages/${pageNumber}`, { text }).then((res) => res.data.data)
