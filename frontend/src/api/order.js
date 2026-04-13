import api from './index'

/**
 * 주문 생성 (POST /api/orders)
 * @param {{ storyId, recipientName, recipientPhone, postalCode, address1, address2, memo, quantity }} data
 */
export const createOrder = (data) =>
  api.post('/api/orders', data).then((res) => res.data.data)

/**
 * 주문 단건 조회 (GET /api/orders/:id)
 * @param {number} id
 */
export const getOrder = (id) =>
  api.get(`/api/orders/${id}`).then((res) => res.data.data)
