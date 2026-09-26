import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import {
  getAccount,
  getTransactions,
  freezeAccount,
  unfreezeAccount,
} from '../api/endpoints';
import { SkeletonList, SkeletonText } from '../components/Skeleton';
import { toCsv, downloadCsv } from '../utils/csv';
import EmailStatementModal from '../components/EmailStatementModal';
import FreezeConfirmModal from '../components/FreezeConfirmModal';
import { useAuth } from '../context/AuthContext';

export default function AccountDetail() {
  const { id = '' } = useParams<{ id: string }>();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const queryClient = useQueryClient();

  const [emailModalOpen, setEmailModalOpen] = useState(false);
  const [freezeModalOpen, setFreezeModalOpen] = useState(false);
  const [freezing, setFreezing] = useState(false);
  const [toast, setToast] = useState<{ type: 'ok' | 'err'; msg: string } | null>(null);

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
  const isFrozen = account?.status === 'FROZEN';

  const showToast = (type: 'ok' | 'err', msg: string) => {
    setToast({ type, msg });
    setTimeout(() => setToast(null), 3500);
  };

  const handleFreeze = async (reason: string) => {
    setFreezing(true);
    try {
      await freezeAccount(id, reason);
      await queryClient.invalidateQueries({ queryKey: ['account', id] });
      await queryClient.invalidateQueries({ queryKey: ['accounts'] });
      setFreezeModalOpen(false);
      showToast('ok', 'Account frozen successfully');
    } catch (e: any) {
      showToast('err', e?.response?.data?.message || 'Failed to freeze account');
    } finally {
      setFreezing(false);
    }
  };

  const handleUnfreeze = async () => {
    if (!confirm('Unfreeze this account? Customer will be able to transact again.')) return;
    setFreezing(true);
    try {
      await unfreezeAccount(id);
      await queryClient.invalidateQueries({ queryKey: ['account', id] });
      await queryClient.invalidateQueries({ queryKey: ['accounts'] });
      showToast('ok', 'Account unfrozen — customer notified');
    } catch (e: any) {
      showToast('err', e?.response?.data?.message || 'Failed to unfreeze account');
    } finally {
      setFreezing(false);
    }
  };

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
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-20 right-6 z-50 px-4 py-3 rounded-xl shadow-lg text-sm font-medium animate-fade-in ${
            toast.type === 'ok'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          {toast.msg}
        </div>
      )}

      <div>
        <Link
          to="/accounts"
          className="text-xs text-brand-600 hover:text-brand-700 font-medium"
        >
          ← Back to Accounts
        </Link>
        <div className="flex items-center justify-between flex-wrap gap-3 mt-2">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            Account Details
          </h2>
          {account && isAdmin && !isFrozen && (
            <button
              onClick={() => setFreezeModalOpen(true)}
              className="btn bg-gradient-to-r from-rose-500 to-rose-600 text-white text-xs shadow-md shadow-rose-500/25 hover:shadow-lg hover:shadow-rose-500/35"
            >
               Freeze Account
            </button>
          )}
          {account && isAdmin && isFrozen && (
            <button
              onClick={handleUnfreeze}
              disabled={freezing}
              className="btn bg-gradient-to-r from-emerald-500 to-emerald-600 text-white text-xs shadow-md shadow-emerald-500/25 hover:shadow-lg hover:shadow-emerald-500/35 disabled:opacity-50"
            >
              {freezing ? 'Unfreezing…' : ' Unfreeze Account'}
            </button>
          )}
        </div>
      </div>

      {/* Frozen banner */}
      {account && isFrozen && (
        <div className="rounded-xl border-l-4 border-rose-500 bg-gradient-to-r from-rose-50 to-rose-100/50 p-4 shadow-sm">
          <div className="flex items-start gap-3">
            <div className="text-2xl">Freeze</div>
            <div className="flex-1">
              <div className="font-bold text-rose-900 text-sm">
                This account is frozen
              </div>
              <div className="text-xs text-rose-800 mt-1">
                All deposits, withdrawals, and transfers are blocked. The customer
                has been notified by email. Contact support to resolve.
              </div>
            </div>
          </div>
        </div>
      )}

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
                <div className="text-base font-bold text-slate-900 dark:text-slate-100 mt-1 tabular-nums">
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
                    className={`badge ${
                      account.status === 'ACTIVE'
                        ? 'badge-low'
                        : account.status === 'FROZEN'
                        ? 'badge-critical'
                        : 'badge-gray'
                    }`}
                  >
                    {account.status === 'FROZEN' && 'Freeze Now '}
                    {account.status}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Actions */}
      {account && (
        <div className="card">
          <div className="card-header">
            <div className="card-title">Actions</div>
          </div>
          <div className="card-body flex flex-wrap gap-3">
            <button
              onClick={handleDownloadStatement}
              disabled={isFrozen}
              className="btn btn-primary"
            >
              Download Statement (PDF)
            </button>
            <button
              onClick={() => setEmailModalOpen(true)}
              disabled={isFrozen}
              className="btn btn-ghost"
            >
              Email Statement
            </button>
            <button
              onClick={handleExportCsv}
              disabled={transactions.length === 0}
              className="btn btn-ghost"
            >
              Export CSV
            </button>
          </div>
        </div>
      )}

      {/* Transactions */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">Recent Transactions</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {transactions.length} total
          </span>
        </div>
        <div className="card-body">
          {txQ.isLoading ? (
            <SkeletonList rows={5} />
          ) : transactions.length === 0 ? (
            <div className="text-sm text-slate-500 py-6 text-center">
              No transactions yet
            </div>
          ) : (
            <ul className="divide-y divide-blue-50">
              {transactions.slice(0, 20).map((t) => (
                <li
                  key={t.id}
                  className="py-3 flex items-center justify-between row-hover -mx-6 px-6"
                >
                  <div className="min-w-0 flex-1">
                    <div className="text-sm font-semibold text-slate-900">
                      {t.type}
                    </div>
                    <div className="text-xs text-slate-500 truncate mt-0.5">
                      {t.description || '—'}
                    </div>
                  </div>
                  <div className="text-right ml-3">
                    <div className="text-sm font-bold tabular-nums text-slate-900">
                      {t.type === 'WITHDRAWAL' ? '-' : '+'}₹
                      {Number(t.amount).toLocaleString('en-IN')}
                    </div>
                    <div className="text-xs text-slate-500 tabular-nums">
                      Bal: ₹{Number(t.balanceAfter).toLocaleString('en-IN')}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <EmailStatementModal
        open={emailModalOpen}
        accountId={id}
        onClose={() => setEmailModalOpen(false)}
      />

      <FreezeConfirmModal
        open={freezeModalOpen}
        accountNumber={account?.accountNumber || ''}
        loading={freezing}
        onClose={() => setFreezeModalOpen(false)}
        onConfirm={handleFreeze}
      />
    </div>
  );
}