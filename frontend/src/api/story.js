import api from './index'

/**
 * 동화 생성 (POST /api/stories)
 * @param {{ childName: string, childAge: number, theme: string }} data
 */
export const createStory = (data) =>
  api.post('/api/stories', data).then((res) => res.data.data)

/**
 * 동화 단건 조회 (GET /api/stories/:id)
 * @param {number} id
 */
export const getStory = (id) =>
  api.get(`/api/stories/${id}`).then((res) => res.data.data)
