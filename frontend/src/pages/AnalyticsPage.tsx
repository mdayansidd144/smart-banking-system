import { useMemo, useState } from 'react';
import { useQueries, useQuery } from '@tanstack/react-query';
import { getAccounts, getTransactions } from '../api/endpoints';
import type { Transaction } from '../types';
import CategoryPieChart from '../components/CategoryPieChart';
import BalanceTrendChart from '../components/BalanceTrendChart';
import TopCategoriesList from '../components/TopCategoriesList';
import { computeAnalytics, filterByDateRange } from '../utils/aggregate';
import { Spinner } from '../components/Spinner';

const RANGES = [
  { label: '7 days', value: 7 },
  { label: '30 days', value: 30 },
  { label: '90 days', value: 90 },
  { label: 'All time', value: 'all' as const },
];

export default function AnalyticsPage() {
  const [range, setRange] = useState<number | 'all'>(30);
  const [selectedAccountId, setSelectedAccountId] = useState<string>('ALL');

  const accountsQ = useQuery({
    queryKey: ['accounts'],
    queryFn: getAccounts,
  });
  const accounts = accountsQ.data ?? [];

  // Fetch transactions for all accounts (or just the selected one)
  const txQueries = useQueries({
    queries: accounts.map((a) => ({
      queryKey: ['transactions', a.id],
      queryFn: () => getTransactions(a.id),
      enabled: selectedAccountId === 'ALL' || selectedAccountId === a.id,
    })),
  });

  const allTx = useMemo<Transaction[]>(() => {
    const out: Transaction[] = [];
    txQueries.forEach((q) => {
      if (q.data) out.push(...q.data);
    });
    return out;
  }, [txQueries]);

  const filtered = useMemo(() => filterByDateRange(allTx, range), [allTx, range]);
  const analytics = useMemo(() => computeAnalytics(filtered), [filtered]);

  const isLoading =
    accountsQ.isLoading ||
    (selectedAccountId === 'ALL'
      ? txQueries.some((q) => q.isLoading)
      : txQueries.find(
          (_, i) => accounts[i]?.id === selectedAccountId
        )?.isLoading ?? false);

  return (
    <div className="space-y-6 max-w-7xl mx-auto animate-fade-in">
      {/* Header */}
      <div className="flex items-start justify-between flex-wrap gap-3">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            Analytics
          </h2>
          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
            Understand where money flows — spending patterns, categories, and trends
          </p>
        </div>

        {/* Filters */}
        <div className="flex items-center gap-3 flex-wrap">
          {/* Account filter */}
          <select
            value={selectedAccountId}
            onChange={(e) => setSelectedAccountId(e.target.value)}
            className="input text-sm !py-2"
            style={{ minWidth: 220 }}
          >
            <option value="ALL">All Accounts</option>
            {accounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.ownerName} — {a.accountNumber}
              </option>
            ))}
          </select>

          {/* Range filter */}
          <div className="flex items-center gap-1 bg-white dark:bg-slate-800 border border-blue-200 dark:border-slate-600 rounded-lg p-1">
            {RANGES.map((r) => (
              <button
                key={String(r.value)}
                onClick={() => setRange(r.value)}
                className={`px-3 py-1.5 text-xs font-semibold rounded-md transition-all ${
                  range === r.value
                    ? 'bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-blue-50 dark:hover:bg-slate-700'
                }`}
              >
                {r.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-20">
          <Spinner size="lg" />
        </div>
      ) : (
        <>
          {/* Summary cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <SummaryCard
              label="Total In"
              value={`₹${analytics.totalIn.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
              color="text-emerald-700 dark:text-emerald-400"
            />
            <SummaryCard
              label="Total Out"
              value={`₹${analytics.totalOut.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
              color="text-rose-700 dark:text-rose-400"
            />
            <SummaryCard
              label="Net"
              value={`${analytics.net >= 0 ? '+' : ''}₹${analytics.net.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
              color={
                analytics.net >= 0
                  ? 'text-emerald-700 dark:text-emerald-400'
                  : 'text-rose-700 dark:text-rose-400'
              }
            />
            <SummaryCard
              label="Avg Transaction"
              value={`₹${analytics.avgTransaction.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
              color="text-brand-700 dark:text-brand-300"
            />
          </div>

          {/* Charts row 1: Pie + Trend */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="card card-hover">
              <div className="card-header">
                <div className="card-title">Spending by Category</div>
                <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                  {analytics.byCategory.length} categories
                </span>
              </div>
              <div className="card-body">
                <CategoryPieChart data={analytics.byCategory} />
              </div>
            </div>

            <div className="card card-hover">
              <div className="card-header">
                <div className="card-title">Balance Trend</div>
                <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                  {analytics.dailyBalances.length} days
                </span>
              </div>
              <div className="card-body">
                <BalanceTrendChart data={analytics.dailyBalances} />
              </div>
            </div>
          </div>

          {/* Charts row 2: Top categories full-width */}
          <div className="card card-hover">
            <div className="card-header">
              <div className="card-title">Top Spending Categories</div>
              <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                {analytics.transactionCount} transactions analyzed
              </span>
            </div>
            <div className="card-body">
              <TopCategoriesList data={analytics.byCategory} />
            </div>
          </div>
        </>
      )}
    </div>
  );
}

function SummaryCard({
  label,
  value,
  color,
}: {
  label: string;
  value: string;
  color: string;
}) {
  return (
    <div className="card card-hover">
      <div className="card-body">
        <div className="text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wide">
          {label}
        </div>
        <div className={`text-2xl font-bold mt-2 ${color} tabular-nums`}>{value}</div>
      </div>
    </div>
  );
}