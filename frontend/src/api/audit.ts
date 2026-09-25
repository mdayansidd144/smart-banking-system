import client from './client';

export type AuditAction =
  | 'CREATE_ACCOUNT'
  | 'DEPOSIT'
  | 'WITHDRAWAL'
  | 'TRANSFER'
  | 'TRANSFER_REVERSE'
  | 'SCHEDULE_TRANSFER'
  | 'CANCEL_SCHEDULED_TRANSFER'
  | 'DOWNLOAD_STATEMENT'
  | 'UPDATE_ACCOUNT'
  | 'FREEZE_ACCOUNT';

export interface AuditLog {
  id: string;
  action: AuditAction;
  accountId: string | null;
  userId: string | null;
  username: string | null;
  ipAddress: string | null;
  userAgent: string | null;
  resourceType: string | null;
  resourceId: string | null;
  details: string | null;
  success: boolean;
  errorMessage: string | null;
  createdAt: string;
}

export interface AuditLogPage {
  content: AuditLog[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AuditFilters {
  action?: string;
  accountId?: string;
  username?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export const getAuditLogs = async (filters: AuditFilters = {}): Promise<AuditLogPage> => {
  const params = new URLSearchParams();
  if (filters.action) params.set('action', filters.action);
  if (filters.accountId) params.set('accountId', filters.accountId);
  if (filters.username) params.set('username', filters.username);
  if (filters.from) params.set('from', filters.from);
  if (filters.to) params.set('to', filters.to);
  params.set('page', String(filters.page ?? 0));
  params.set('size', String(filters.size ?? 20));

  const { data } = await client.get(`/v1/audit?${params.toString()}`);
  return data;
};