import client from './client';
export type KycStatus = 'NOT_STARTED' | 'PENDING' | 'APPROVED' | 'REJECTED';
export interface KycDocument {
  id: string;
  docType: string;
  documentNumber: string;
  documentUrl: string | null;
  uploadedAt: string;
}

export interface KycSubmission {
  id: string | null;
  userId: string;
  username: string | null;
  email: string | null;
  fullName: string | null;
  dateOfBirth: string | null;
  address: string | null;
  city: string | null;
  postalCode: string | null;
  status: KycStatus;
  submittedAt: string | null;
  reviewedAt: string | null;
  rejectionReason: string | null;
  documents: KycDocument[];
}

export interface KycSubmitPayload {
  fullName: string;
  dateOfBirth: string;     // YYYY-MM-DD
  address: string;
  city: string;
  postalCode: string;
  panNumber: string;
  aadhaarNumber: string;
}

export const getKycStatus = async (): Promise<KycSubmission> => {
  const { data } = await client.get('/kyc/status');
  return data;
};

export const submitKyc = async (payload: KycSubmitPayload): Promise<KycSubmission> => {
  const { data } = await client.post('/kyc/submit', payload);
  return data;
};

export const listPendingKyc = async (): Promise<KycSubmission[]> => {
  const { data } = await client.get('/kyc/admin/pending');
  return data;
};

export const listAllKyc = async (): Promise<KycSubmission[]> => {
  const { data } = await client.get('/kyc/admin/all');
  return data;
};

export const approveKyc = async (id: string): Promise<KycSubmission> => {
  const { data } = await client.post(`/kyc/admin/${id}/approve`);
  return data;
};

export const rejectKyc = async (id: string, reason: string): Promise<KycSubmission> => {
  const { data } = await client.post(`/kyc/admin/${id}/reject`, { reason });
  return data;
};