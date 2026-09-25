import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { getAccount, getTransactions } from '../api/endpoints';
import { SkeletonList, SkeletonText } from '../components/Skeleton';
import { toCsv, downloadCsv } from '../utils/csv';
import EmailStatementModal from '../components/EmailStatementModal';

export default function AccountDetail() {
  const { id = '' } = useParams<{ id: string }>();
  const [emailModalOpen, setEmailModalOpen] = useState(false);

  const accountQ = useQuery({
    queryKey: ['account', id],
    queryFn: () => getAccount(id),
    enabled: !!id,
  });

  const txQ = useQuery({
    queryKey: ['transactions', id],
    queryFn: () => getTransactions(id),
    enabled: !!id,
  });

  const account = accountQ.data;
  const transactions = txQ.data ?? [];

  const handleDownloadStatement = () => {
    const to = new Date().toISOString().slice(0, 10);
    const from = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
      .toISOString()
      .slice(0, 10);
    window.open(
      `http://localhost:9000/api/v1/accounts/${id}/statement?from=${from}&to=${to}`,
      '_blank'
    );
  };

  const handleExportCsv = () => {
    if (!account || transactions.length === 0) return;
    const rows = transactions.map((t) => ({
      date: new Date(t.createdAt).toISOString(),
      type: t.type,
      amount: t.amount,
      balanceAfter: t.balanceAfter,
      description: t.description || '',
    }));
    const csv = toCsv(rows, [
      { key: 'date', label: 'Date' },
      { key: 'type', label: 'Type' },
      { key: 'amount', label: 'Amount' },
      { key: 'balanceAfter', label: 'Balance After' },
      { key: 'description', label: 'Description' },
    ]);
    downloadCsv(`${account.accountNumber}-transactions.csv`, csv);
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div>
        <Link
          to="/accounts"
          className="text-xs text-brand-600 hover:text-brand-700 font-medium"
        >
          ← Back to Accounts
        </Link>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mt-2">
          Account Details
        </h2>
      </div>

      <div className="card">
        <div className="card-body">
          {accountQ.isLoading ? (
            <SkeletonText lines={3} />
          ) : !account ? (
            <div className="text-sm text-slate-500 dark:text-slate-400">
              Account not found
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div>
                <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                  Owner
                </div>
                <div className="text-base font-bold text-slate-900 dark:text-slate-100 mt-1">
                  {account.ownerName}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                  Account Number
                </div>
                <div className="text-base font-mono text-slate-900 dark:text-slate-100 mt-1">
                  {account.accountNumber}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                  Balance
                </div>
                <div className="text-base font-bold text-emerald-700 dark:text-emerald-400 mt-1 tabular-nums">
                  {account.currency}{' '}
                  {Number(account.balance).toLocaleString('en-IN')}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                  Status
                </div>
                <div className="mt-1">
                  <span
                    className={
                      account.status === 'ACTIVE'
                        ? 'badge badge-low'
                        : 'badge badge-gray'
                    }
                  >
                    {account.status}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Action buttons */}
      <div className="flex gap-3 flex-wrap">
        <button onClick={handleDownloadStatement} className="btn btn-ghost">
          Download PDF Statement
        </button>
        <button
          onClick={handleExportCsv}
          disabled={transactions.length === 0}
          className="btn btn-ghost"
        >
          Export CSV
        </button>
        <button
          onClick={() => setEmailModalOpen(true)}
          disabled={!account}
          className="btn btn-primary"
        >
          Email Statement
        </button>
      </div>

      {/* Transactions */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">Transaction History</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {transactions.length} total
          </span>
        </div>
        <div className="card-body">
          {txQ.isLoading ? (
            <SkeletonList rows={6} />
          ) : transactions.length === 0 ? (
            <div className="text-sm text-slate-500 dark:text-slate-400 py-6 text-center">
              No transactions yet
            </div>
          ) : (
            <ul className="divide-y divide-blue-50 dark:divide-slate-700">
              {transactions.map((t) => {
                const isDeposit = t.type === 'DEPOSIT';
                return (
                  <li
                    key={t.id}
                    className="flex items-center justify-between py-3.5"
                  >
                    <div className="min-w-0 flex-1">
                      <div
                        className={`text-xs font-semibold ${
                          isDeposit
                            ? 'text-emerald-700 dark:text-emerald-400'
                            : 'text-rose-700 dark:text-rose-400'
                        }`}
                      >
                        {t.type}
                      </div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 truncate">
                        {t.description || '—'}
                      </div>
                    </div>
                    <div className="text-right ml-3">
                      <div
                        className={`text-sm font-bold ${
                          isDeposit
                            ? 'text-emerald-700 dark:text-emerald-400'
                            : 'text-rose-700 dark:text-rose-400'
                        } tabular-nums`}
                      >
                        {isDeposit ? '+' : '−'}₹
                        {Number(t.amount).toLocaleString('en-IN')}
                      </div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                        {new Date(t.createdAt).toLocaleString('en-IN', {
                          day: '2-digit',
                          month: 'short',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </div>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
      {account && (
        <EmailStatementModal
          open={emailModalOpen}
          accountId={account.id}
          accountNumber={account.accountNumber}
          onClose={() => setEmailModalOpen(false)}
        />
      )}
    </div>
  );
}