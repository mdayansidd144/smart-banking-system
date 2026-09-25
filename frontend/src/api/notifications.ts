import client from './client';

export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED';
export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH';

export interface Notification {
  id: string;
  eventType: string;
  channel: NotificationChannel;
  recipient: string;
  subject: string;
  body: string | null;
  status: NotificationStatus;
  failureReason: string | null;
  retryCount: number;
  sentAt: string | null;
  createdAt: string;
}

export interface NotificationPage {
  content: Notification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export const getNotifications = async (page = 0, size = 10): Promise<NotificationPage> => {
  const { data } = await client.get(
    `/v1/notifications?page=${page}&size=${size}`
  );
  return data;
};

export const getUnreadCount = async (): Promise<number> => {
  const { data } = await client.get('/v1/notifications/unread-count');
  return data?.count ?? 0;
};