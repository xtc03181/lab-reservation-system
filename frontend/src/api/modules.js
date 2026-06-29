import request from './request'

export const login = data => request.post('/auth/login', data)
export const sendResetCode = data => request.post('/auth/send-reset-code', data)
export const resetPassword = data => request.post('/auth/reset-password', data)

export const listUsers = () => request.get('/users')
export const getMe = () => request.get('/users/me')
export const updateMe = data => request.put('/users/me', data)
export const changePassword = data => request.put('/users/me/password', data)
export const createUser = data => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = id => request.delete(`/users/${id}`)

export const listLabs = () => request.get('/labs')
export const createLab = data => request.post('/labs', data)
export const updateLab = (id, data) => request.put(`/labs/${id}`, data)
export const deleteLab = id => request.delete(`/labs/${id}`)

export const listEquipment = () => request.get('/equipment')
export const createEquipment = data => request.post('/equipment', data)
export const updateEquipment = (id, data) => request.put(`/equipment/${id}`, data)
export const deleteEquipment = id => request.delete(`/equipment/${id}`)

export const listLabReservations = () => request.get('/lab-reservations')
export const createLabReservation = data => request.post('/lab-reservations', data)
export const updateLabReservation = (id, data) => request.put(`/lab-reservations/${id}`, data)
export const cancelLabReservation = id => request.put(`/lab-reservations/${id}/cancel`)
export const reviewLabReservation = (id, data) => request.put(`/lab-reservations/${id}/review`, data)

export const listEquipmentBorrows = () => request.get('/equipment-borrows')
export const createEquipmentBorrow = data => request.post('/equipment-borrows', data)
export const reviewEquipmentBorrow = (id, data) => request.put(`/equipment-borrows/${id}/review`, data)

export const listNotices = () => request.get('/notices')
export const createNotice = data => request.post('/notices', data)
export const updateNotice = (id, data) => request.put(`/notices/${id}`, data)
export const pinNotice = id => request.put(`/notices/${id}/pin`)
export const normalNotice = id => request.put(`/notices/${id}/normal`)
export const offlineNotice = id => request.put(`/notices/${id}/offline`)
export const deleteNotice = id => request.delete(`/notices/${id}`)

export const listOperationLogs = () => request.get('/operation-logs')

export const listMessages = () => request.get('/messages')
export const unreadMessageCount = () => request.get('/messages/unread-count')
export const markMessageRead = id => request.put(`/messages/${id}/read`)
export const markAllMessagesRead = () => request.put('/messages/read-all')

export const getActiveReservationRule = () => request.get('/reservation-rules/active')
export const updateActiveReservationRule = data => request.put('/reservation-rules/active', data)
