import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getBudgets,
  createBudget,
  updateBudget,
  deleteBudget,
  type Budget,
} from '../api/budgets';
import { Spinner } from '../components/Spinner';

const CATEGORIES = [
  'Groceries',
  'Rent',
  'Utilities',
  'Dining',
  'Shopping',
  'Transport',
  'Subscriptions',
  'Other',
];

function statusColor(status: string) {
  if (status === 'EXCEEDED') return 'text-rose-700 dark:text-rose-400';
  if (status === 'WARNING') return 'text-amber-700 dark:text-amber-400';
  return 'text-emerald-700 dark:text-emerald-400';
}

function barColor(status: string) {
  if (status === 'EXCEEDED') return 'bg-rose-500';
  if (status === 'WARNING') return 'bg-amber-500';
  return 'bg-emerald-500';
}

export default function BudgetsPage() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [category, setCategory] = useState(CATEGORIES[0]);
  const [limit, setLimit] = useState('5000');

  const budgetsQ = useQuery({ queryKey: ['budgets'], queryFn: getBudgets });
  const budgets = budgetsQ.data ?? [];

  const createMut = useMutation({
    mutationFn: createBudget,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['budgets'] });
      resetForm();
    },
  });

  const updateMut = useMutation({
    mutationFn: ({ id, monthlyLimit }: { id: string; monthlyLimit: number }) =>
      updateBudget(id, { category, monthlyLimit }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['budgets'] });
      resetForm();
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteBudget,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });

  const resetForm = () => {
    setShowForm(false);
    setEditingId(null);
    setCategory(CATEGORIES[0]);
    setLimit('5000');
  };

  const startEdit = (b: Budget) => {
    setEditingId(b.id);
    setCategory(b.category);
    setLimit(String(b.monthlyLimit));
    setShowForm(true);
  };

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    const numLimit = Number(limit);
    if (!numLimit || numLimit <= 0) return;

    if (editingId) {
      updateMut.mutate({ id: editingId, monthlyLimit: numLimit });
    } else {
      createMut.mutate({ category, monthlyLimit: numLimit });
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto animate-fade-in">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            Budgets
          </h2>
          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
            Set monthly spending limits per category — get alerted at 80% and 100%
          </p>
        </div>
        {!showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="btn btn-primary"
          >
            + New Budget
          </button>
        )}
      </div>

      {showForm && (
        <form onSubmit={submit} className="card">
          <div className="card-body space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Category
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  disabled={!!editingId}
                  className="input disabled:opacity-60"
                >
                  {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Monthly Limit (₹)
                </label>
                <input
                  type="number"
                  min="1"
                  step="100"
                  value={limit}
                  onChange={(e) => setLimit(e.target.value)}
                  className="input"
                  required
                />
              </div>
            </div>

            <div className="flex gap-3">
              <button
                type="button"
                onClick={resetForm}
                className="btn btn-ghost flex-1"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={createMut.isPending || updateMut.isPending}
                className="btn btn-primary flex-1"
              >
                {createMut.isPending || updateMut.isPending ? (
                  <>
                    <Spinner size="sm" /> Saving…
                  </>
                ) : editingId ? (
                  'Update Budget'
                ) : (
                  'Create Budget'
                )}
              </button>
            </div>
          </div>
        </form>
      )}

      <div className="card">
        <div className="card-header">
          <div className="card-title">Your Budgets</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {budgets.length} total
          </span>
        </div>
        <div className="card-body">
          {budgetsQ.isLoading ? (
            <div className="flex justify-center py-8">
              <Spinner size="md" />
            </div>
          ) : budgets.length === 0 ? (
            <div className="text-sm text-slate-500 dark:text-slate-400 py-10 text-center">
              No budgets yet. Click "New Budget" to create one.
            </div>
          ) : (
            <ul className="space-y-5">
              {budgets.map((b) => {
                const pct = Math.min(b.percentageUsed, 100);
                return (
                  <li key={b.id}>
                    <div className="flex items-center justify-between mb-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-semibold text-slate-800 dark:text-slate-100">
                            {b.category}
                          </span>
                          <span
                            className={`badge ${
                              b.status === 'EXCEEDED'
                                ? 'badge-critical'
                                : b.status === 'WARNING'
                                ? 'badge-medium'
                                : 'badge-low'
                            }`}
                          >
                            {b.status}
                          </span>
                        </div>
                        <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                          ₹{Number(b.currentSpend).toLocaleString('en-IN')} of ₹
                          {Number(b.monthlyLimit).toLocaleString('en-IN')}
                        </div>
                      </div>
                      <div className="text-right">
                        <div
                          className={`text-lg font-bold tabular-nums ${statusColor(b.status)}`}
                        >
                          {b.percentageUsed.toFixed(1)}%
                        </div>
                        <div className="flex items-center gap-2 mt-1 justify-end">
                          <button
                            onClick={() => startEdit(b)}
                            className="text-[10px] text-brand-600 hover:text-brand-700 font-medium"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => {
                              if (confirm(`Delete budget for ${b.category}?`)) {
                                deleteMut.mutate(b.id);
                              }
                            }}
                            className="text-[10px] text-rose-600 hover:text-rose-700 font-medium"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    </div>

                    <div className="h-2 w-full rounded-full bg-blue-50 dark:bg-slate-700 overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${barColor(b.status)}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}