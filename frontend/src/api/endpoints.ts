import client from './client';
import type { Account, Transaction, Anomaly, FraudAlert } from '../types';
export const getAccounts = async (): Promise<Account[]> => {
  const { data } = await client.get('/v1/accounts');
  return data;
};
export const getAccount = async (id: string): Promise<Account> => {
  const { data } = await client.get(`/v1/accounts/${id}`);
  return data;
};
export const getTransactions = async (accountId: string): Promise<Transaction[]> => {
  const { data } = await client.get(`/v1/accounts/${accountId}/transactions`);
  return data;
};

// ---- Anomalies ----
export const getAnomalies = async (): Promise<Anomaly[]> => {
  const { data } = await client.get('/v1/anomalies');
  return data;
};
export const getAnomaliesByAccount = async (accountId: string): Promise<Anomaly[]> => {
  const { data } = await client.get(`/v1/anomalies/account/${accountId}`);
  return data;
};
export const getAnomaliesBySeverity = async (severity: string): Promise<Anomaly[]> => {
  const { data } = await client.get(`/v1/anomalies/severity/${severity}`);
  return data;
};
export const updateAnomalyStatus = async (id: string, status: string): Promise<Anomaly> => {
  const { data } = await client.patch(`/v1/anomalies/${id}/status?status=${status}`);
  return data;
};

// ---- Fraud ----
export const getFraudAlerts = async (): Promise<FraudAlert[]> => {
  const { data } = await client.get('/v1/fraud/alerts');
  return data;
};

// ---- Loans ----
export interface LoanRequest {
  accountId: string;
  amount: number;
  termMonths: number;
  purpose: string;
}
export interface LoanResponse {
  id: string;
  accountId: string;
  requestedAmount: number;
  termMonths: number;
  purpose: string;
  decision: 'APPROVED' | 'REJECTED' | 'MANUAL_REVIEW';
  approvedAmount: number;
  interestRate: number;
  riskScore: number;
  reasoning: string;
  createdAt: string;
}
export const applyForLoan = async (payload: LoanRequest): Promise<LoanResponse> => {
  const { data } = await client.post('/v1/loans/apply', payload, { timeout: 120000 });
  return data;
};
export const getLoans = async (): Promise<LoanResponse[]> => {
  const { data } = await client.get('/v1/loans');
  return data;
};

// ---- AI Agent Chat (with memory) ----
export interface AgentChatResponse {
  reply: string;
  sessionId: string;
  repliedAt: string;
}
export const chatWithAgent = async (
  message: string,
  sessionId?: string
): Promise<AgentChatResponse> => {
  const { data } = await client.post(
    '/v1/agent/chat',
    { message, sessionId },
    { timeout: 120000 }
  );
  return data;
};
export const emailStatement = async (
  accountId: string,
  email: string,
  from: string,
  to: string
): Promise<{ status: string; message: string; requestId: string }> => {
  const { data } = await client.post(
    `/v1/accounts/${accountId}/email-statement?from=${from}&to=${to}`,
    { email },
    { timeout: 30000 }
  );
  return data;
};
export const freezeAccount = async (
  id: string,
  reason: string
): Promise<Account> => {
  const { data } = await client.post(`/v1/accounts/${id}/freeze`, { reason });
  return data;
};

export const unfreezeAccount = async (id: string): Promise<Account> => {
  const { data } = await client.post(`/v1/accounts/${id}/unfreeze`);
  return data;
};
export interface AmortizationEntry {
  month: number;
  emi: number;
  principalComponent: number;
  interestComponent: number;
  remainingBalance: number;
  cumulativeInterest: number;
}

export interface AmortizationSchedule {
  loanId: string;
  accountId: string;
  principal: number;
  annualInterestRate: number;
  termMonths: number;
  monthlyEmi: number;
  totalInterest: number;
  totalPayment: number;
  entries: AmortizationEntry[];
}

export const getAmortization = async (
  loanId: string
): Promise<AmortizationSchedule> => {
  const { data } = await client.get(`/v1/loans/${loanId}/amortization`);
  return data;
};