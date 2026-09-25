import client from './client';
export interface Biller {
  id: string;
  code: string;
  name: string;
  category: string;
  defaultAmount: number;
}

export interface Bill {
  id: string;
  accountId: string;
  billerId: string;
  billerName: string;
  billerCategory: string;
  nickname: string;
  amount: number;
  cronExpression: string;
  nextDueAt: string;
  lastPaidAt: string | null;
  lastTransferId: string | null;
  status: 'ACTIVE' | 'PAUSED' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  successCount: number;
  failureCount: number;
  lastError: string | null;
  createdAt: string;
}

export interface CreateBillPayload {
  accountId: string;
  billerId: string;
  nickname?: string;
  amount: number;
  cronExpression: string;
}

export const getBillers = async (): Promise<Biller[]> => {
  const { data } = await client.get('/v1/bills/billers');
  return data;
};

export const getBills = async (): Promise<Bill[]> => {
  const { data } = await client.get('/v1/bills');
  return data;
};

export const getBillsByAccount = async (accountId: string): Promise<Bill[]> => {
  const { data } = await client.get(`/v1/bills/account/${accountId}`);
  return data;
};

export const createBill = async (payload: CreateBillPayload): Promise<Bill> => {
  const { data } = await client.post('/v1/bills', payload);
  return data;
};

export const pauseBill = async (id: string): Promise<Bill> => {
  const { data } = await client.patch(`/v1/bills/${id}/pause`);
  return data;
};

export const resumeBill = async (id: string): Promise<Bill> => {
  const { data } = await client.patch(`/v1/bills/${id}/resume`);
  return data;
};

export const cancelBill = async (id: string): Promise<void> => {
  await client.delete(`/v1/bills/${id}`);
};