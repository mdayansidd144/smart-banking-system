import type { Transaction } from '../types';
import { categorize } from './categorize';
import type { Category } from './categorize';
export interface CategorySummary {
  category: Category;
  total: number;
  count: number;
}

export interface DayBalance {
  date: string;
  balance: number;
  incoming: number;
  outgoing: number;
}

export interface AnalyticsSummary {
  totalIn: number;
  totalOut: number;
  net: number;
  transactionCount: number;
  avgTransaction: number;
  byCategory: CategorySummary[];
  dailyBalances: DayBalance[];
}

export function computeAnalytics(transactions: Transaction[]): AnalyticsSummary {
  if (transactions.length === 0) {
    return {
      totalIn: 0,
      totalOut: 0,
      net: 0,
      transactionCount: 0,
      avgTransaction: 0,
      byCategory: [],
      dailyBalances: [],
    };
  }

  // Sort oldest first for time-based math
  const sorted = [...transactions].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );

  let totalIn = 0;
  let totalOut = 0;
  const categoryMap = new Map<Category, { total: number; count: number }>();
  const dayMap = new Map<string, { incoming: number; outgoing: number; lastBalance: number }>();

  sorted.forEach((t) => {
    const amount = Number(t.amount);
    const day = t.createdAt.slice(0, 10);
    const isDeposit = t.type === 'DEPOSIT';

    if (isDeposit) {
      totalIn += amount;
    } else {
      totalOut += amount;
      // Only count outgoing for category breakdown (spending)
      const cat = categorize(t.description);
      const existing = categoryMap.get(cat) || { total: 0, count: 0 };
      categoryMap.set(cat, { total: existing.total + amount, count: existing.count + 1 });
    }

    const dayEntry = dayMap.get(day) || { incoming: 0, outgoing: 0, lastBalance: 0 };
    if (isDeposit) dayEntry.incoming += amount;
    else dayEntry.outgoing += amount;
    dayEntry.lastBalance = Number(t.balanceAfter);
    dayMap.set(day, dayEntry);
  });

  const byCategory: CategorySummary[] = Array.from(categoryMap.entries())
    .map(([category, v]) => ({ category, total: v.total, count: v.count }))
    .sort((a, b) => b.total - a.total);

  const dailyBalances: DayBalance[] = Array.from(dayMap.entries())
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([date, v]) => ({
      date,
      balance: v.lastBalance,
      incoming: v.incoming,
      outgoing: v.outgoing,
    }));

  return {
    totalIn,
    totalOut,
    net: totalIn - totalOut,
    transactionCount: sorted.length,
    avgTransaction: sorted.length ? (totalIn + totalOut) / sorted.length : 0,
    byCategory,
    dailyBalances,
  };
}

export function filterByDateRange(
  transactions: Transaction[],
  days: number | 'all'
): Transaction[] {
  if (days === 'all') return transactions;
  const cutoff = Date.now() - days * 24 * 60 * 60 * 1000;
  return transactions.filter(
    (t) => new Date(t.createdAt).getTime() >= cutoff
  );
}