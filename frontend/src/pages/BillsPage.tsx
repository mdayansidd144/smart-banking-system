import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getBillers,
  getBills,
  createBill,
  pauseBill,
  resumeBill,
  cancelBill,
  type Bill,
} from '../api/bills';
import { getAccounts } from '../api/endpoints';
import { Spinner } from '../components/Spinner';

const CRON_PRESETS = [
  { label: '1st of month', value: '0 0 10 1 * *' },
  { label: 'Every Monday', value: '0 0 9 * * MON' },
  { label: 'Every 15 days', value: '0 0 10 */15 * *' },
];

function statusBadge(status: string) {
  const map: Record<string, string> = {
    ACTIVE: 'badge-low',
    PAUSED: 'badge-gray',
    PAID: 'badge-medium',
    OVERDUE: 'badge-critical',
    CANCELLED: 'badge-gray',
  };
  return `badge ${map[status] || 'badge-gray'}`;
}

function formatDate(iso: string | null) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function daysUntil(iso: string): string {
  const diff = new Date(iso).getTime() - Date.now();
  const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
  if (days < 0) return `${Math.abs(days)}d overdue`;
  if (days === 0) return 'Due today';
  if (days === 1) return 'Due tomorrow';
  return `Due in ${days}d`;
}

export default function BillsPage() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);

  const [accountId, setAccountId] = useState('');
  const [billerId, setBillerId] = useState('');
  const [nickname, setNickname] = useState('');
  const [amount, setAmount] = useState('');
  const [cron, setCron] = useState(CRON_PRESETS[0].value);

  const billsQ = useQuery({ queryKey: ['bills'], queryFn: getBills });
  const billersQ = useQuery({ queryKey: ['billers'], queryFn: getBillers });
  const accountsQ = useQuery({ queryKey: ['accounts'], queryFn: getAccounts });

  const bills = billsQ.data ?? [];
  const billers = billersQ.data ?? [];
  const accounts = accountsQ.data ?? [];

  const createMut = useMutation({
    mutationFn: createBill,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['bills'] });
      resetForm();
    },
  });

  const pauseMut = useMutation({
    mutationFn: pauseBill,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bills'] }),
  });

  const resumeMut = useMutation({
    mutationFn: resumeBill,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bills'] }),
  });

  const cancelMut = useMutation({
    mutationFn: cancelBill,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bills'] }),
  });

  const resetForm = () => {
    setShowForm(false);
    setAccountId('');
    setBillerId('');
    setNickname('');
    setAmount('');
    setCron(CRON_PRESETS[0].value);
  };

  const handleBillerChange = (id: string) => {
    setBillerId(id);
    const biller = billers.find((b) => b.id === id);
    if (biller && !amount) {
      setAmount(String(biller.defaultAmount));
    }
  };

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!accountId || !billerId) return;
    createMut.mutate({
      accountId,
      billerId,
      nickname: nickname || undefined,
      amount: Number(amount),
      cronExpression: cron,
    });
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto animate-fade-in">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Bills</h2>
          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
            Schedule and pay recurring bills automatically
          </p>
        </div>
        {!showForm && (
          <button onClick={() => setShowForm(true)} className="btn btn-primary">
            + New Bill
          </button>
        )}
      </div>

      {showForm && (
        <form onSubmit={submit} className="card">
          <div className="card-body space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Account
                </label>
                <select
                  value={accountId}
                  onChange={(e) => setAccountId(e.target.value)}
                  className="input"
                  required
                >
                  <option value="">Select account</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.ownerName} — {a.accountNumber} (₹
                      {Number(a.balance).toLocaleString('en-IN')})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Biller
                </label>
                <select
                  value={billerId}
                  onChange={(e) => handleBillerChange(e.target.value)}
                  className="input"
                  required
                >
                  <option value="">Select biller</option>
                  {billers.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.name} ({b.category})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Nickname (optional)
                </label>
                <input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  className="input"
                  placeholder="e.g., Home Electricity"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Amount (₹)
                </label>
                <input
                  type="number"
                  min="1"
                  step="1"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="input"
                  required
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Schedule
                </label>
                <div className="flex gap-2 flex-wrap">
                  {CRON_PRESETS.map((p) => (
                    <button
                      key={p.value}
                      type="button"
                      onClick={() => setCron(p.value)}
                      className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
                        cron === p.value
                          ? 'bg-brand-600 text-white border-brand-600'
                          : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border-blue-200 dark:border-slate-600 hover:bg-blue-50 dark:hover:bg-slate-700'
                      }`}
                    >
                      {p.label}
                    </button>
                  ))}
                </div>
                <div className="text-[10px] text-slate-400 mt-2 font-mono">{cron}</div>
              </div>
            </div>

            <div className="flex gap-3">
              <button type="button" onClick={resetForm} className="btn btn-ghost flex-1">
                Cancel
              </button>
              <button
                type="submit"
                disabled={createMut.isPending}
                className="btn btn-primary flex-1"
              >
                {createMut.isPending ? (
                  <>
                    <Spinner size="sm" /> Creating…
                  </>
                ) : (
                  'Create Bill'
                )}
              </button>
            </div>
          </div>
        </form>
      )}

      <div className="card">
        <div className="card-header">
          <div className="card-title">Scheduled Bills</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {bills.length} total
          </span>
        </div>
        <div className="card-body">
          {billsQ.isLoading ? (
            <div className="flex justify-center py-8">
              <Spinner size="md" />
            </div>
          ) : bills.length === 0 ? (
            <div className="text-sm text-slate-500 dark:text-slate-400 py-10 text-center">
              No bills yet. Click "New Bill" to schedule one.
            </div>
          ) : (
            <ul className="divide-y divide-blue-50 dark:divide-slate-700">
              {bills.map((b) => (
                <li key={b.id} className="py-4">
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                          {b.nickname || b.billerName}
                        </span>
                        <span className={statusBadge(b.status)}>{b.status}</span>
                        <span className="badge badge-gray">{b.billerCategory}</span>
                      </div>
                      <div className="text-xs text-slate-500 dark:text-slate-400">
                        {b.billerName}
                      </div>
                      <div className="text-xs text-slate-600 dark:text-slate-300 mt-1 font-mono">
                        ₹{Number(b.amount).toLocaleString('en-IN')} · {b.cronExpression}
                      </div>
                    </div>
                    <div className="text-right flex-shrink-0">
                      <div className="text-sm font-bold text-slate-900 dark:text-slate-100">
                        {daysUntil(b.nextDueAt)}
                      </div>
                      <div className="text-[10px] text-slate-500 dark:text-slate-400">
                        {formatDate(b.nextDueAt)}
                      </div>
                      <div className="flex items-center gap-2 mt-2 justify-end">
                        {b.status === 'ACTIVE' && (
                          <button
                            onClick={() => pauseMut.mutate(b.id)}
                            className="text-[10px] text-amber-600 hover:text-amber-700 font-medium"
                          >
                            Pause
                          </button>
                        )}
                        {b.status === 'PAUSED' && (
                          <button
                            onClick={() => resumeMut.mutate(b.id)}
                            className="text-[10px] text-emerald-600 hover:text-emerald-700 font-medium"
                          >
                            Resume
                          </button>
                        )}
                        <button
                          onClick={() => {
                            if (confirm(`Cancel bill "${b.nickname || b.billerName}"?`)) {
                              cancelMut.mutate(b.id);
                            }
                          }}
                          className="text-[10px] text-rose-600 hover:text-rose-700 font-medium"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}