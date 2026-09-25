import client from './client';
export interface Budget {
  id: string;
  category: string;
  monthlyLimit: number;
  currentSpend: number;
  percentageUsed: number;
  status: 'OK' | 'WARNING' | 'EXCEEDED';
  createdAt: string;
}

export interface BudgetRequest {
  category: string;
  monthlyLimit: number;
}

export const getBudgets = async (): Promise<Budget[]> => {
  const { data } = await client.get('/v1/budgets');
  return data;
};

export const createBudget = async (payload: BudgetRequest): Promise<Budget> => {
  const { data } = await client.post('/v1/budgets', payload);
  return data;
};

export const updateBudget = async (
  id: string,
  payload: BudgetRequest
): Promise<Budget> => {
  const { data } = await client.patch(`/v1/budgets/${id}`, payload);
  return data;
};

export const deleteBudget = async (id: string): Promise<void> => {
  await client.delete(`/v1/budgets/${id}`);
};

// The client interceptor should auto-attach the JWT. If not, this ensures it.
client.interceptors.request.use((config) => {
  try {
    const raw =
      sessionStorage.getItem('smartbank.auth') ||
      localStorage.getItem('smartbank.auth');
    if (raw) {
      const parsed = JSON.parse(raw);
      if (parsed.token) {
        config.headers = config.headers || {};
        (config.headers as any).Authorization = `Bearer ${parsed.token}`;
      }
    }
  } catch {
    // ignore
  }
  return config;
});