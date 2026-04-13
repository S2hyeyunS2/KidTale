import api from './index'

/**
 * 사용 가능한 템플릿 목록 조회 (GET /api/books/templates)
 * @param {'cover'|'content'} templateKind
 */
export const getTemplates = (templateKind) =>
  api
    .get('/api/books/templates', { params: { templateKind } })
    .then((res) => res.data.data)

/**
 * 책 생성 (POST /api/books)
 * @param {{ storyId: number, coverTemplateUid: string, contentTemplateUid: string }} data
 */
export const createBook = (data) =>
  api.post('/api/books', data).then((res) => res.data.data)
