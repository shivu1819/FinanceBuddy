import api from './api'
export const getNotifications = async () => (await api.get('/notifications')).data
export const getUnreadCount = async () => (await api.get('/notifications/unread-count')).data.count
export const markNotificationRead = async (id) => (await api.patch(`/notifications/${id}/read`)).data
export const markAllNotificationsRead = async () => api.patch('/notifications/read-all')
