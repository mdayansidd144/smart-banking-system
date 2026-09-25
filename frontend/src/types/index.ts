export interface Account {
  id: string;
  ownerName: string;
  accountNumber: string;
  balance: number;
  currency: string;
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
  createdAt: string;
}

export interface Transaction {
  id: string;
  accountId: string;
  type: 'DEPOSIT' | 'WITHDRAWAL';
  amount: number;
  balanceAfter: number;
  description: string;
  createdAt: string;
}

export interface Transfer {
  transferId: string;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  status: 'COMPLETED' | 'FAILED' | 'REVERSED';
  createdAt: string;
}

export interface Anomaly {
  id: string;
  accountId: string;
  relatedAccountId: string | null;
  eventType: string;
  ruleTriggered: string;
  amount: number;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  riskScore: number;
  reason: string;
  aiExplanation: string | null;
  status: 'OPEN' | 'REVIEWED' | 'DISMISSED' | 'CONFIRMED_FRAUD';
  createdAt: string;
}

export interface FraudAlert {
  id: string;
  accountId: string;
  relatedAccountId: string | null;
  eventType: string;
  amount: number;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  riskScore: number;
  reason: string;
  status: 'OPEN' | 'REVIEWED' | 'DISMISSED' | 'CONFIRMED_FRAUD';
  createdAt: string;
}