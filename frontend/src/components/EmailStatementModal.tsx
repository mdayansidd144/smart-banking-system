import { useState, useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import { emailStatement } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import { Spinner } from './Spinner';

interface Props {
  open: boolean;
  accountId: string;
  accountNumber: string;
  onClose: () => void;
}

function isoDate(d: Date) {
  return d.toISOString().slice(0, 10);
}

export default function EmailStatementModal({
  open,
  accountId,
  accountNumber,
  onClose,
}: Props) {
  const { user } = useAuth();

  const today = new Date();
  const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);

  const [email, setEmail] = useState(user?.email || '');
  const [from, setFrom] = useState(isoDate(thirtyDaysAgo));
  const [to, setTo] = useState(isoDate(today));
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setSuccess(null);
      setEmail(user?.email || '');
    }
  }, [open, user]);

  const mutation = useMutation({
    mutationFn: () => emailStatement(accountId, email, from, to),
    onSuccess: (data) => {
      setSuccess(data.message || 'Statement emailed successfully');
      setTimeout(() => onClose(), 2500);
    },
  });

  if (!open) return null;

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    setSuccess(null);
    mutation.mutate();
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-fade-in"
      style={{ background: 'rgba(15, 23, 42, 0.5)', backdropFilter: 'blur(4px)' }}
      onClick={onClose}
    >
      <div
        className="w-full max-w-md bg-white dark:bg-slate-800 rounded-xl shadow-2xl border border-blue-100 dark:border-slate-700 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-blue-100 dark:border-slate-700">
          <div>
            <div className="text-sm font-semibold text-slate-800 dark:text-slate-100">
              Email Statement
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 font-mono">
              {accountNumber}
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 text-xl leading-none"
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <form onSubmit={submit} className="p-5 space-y-4">
          {success && (
            <div className="px-3 py-2.5 rounded-lg bg-emerald-50 dark:bg-emerald-900/40 border border-emerald-200 dark:border-emerald-700 text-xs text-emerald-700 dark:text-emerald-300">
              ✓ {success}
            </div>
          )}

          {mutation.isError && (
            <div className="px-3 py-2.5 rounded-lg bg-rose-50 dark:bg-rose-900/40 border border-rose-200 dark:border-rose-700 text-xs text-rose-700 dark:text-rose-300">
              {(mutation.error as any)?.response?.data?.message ||
                (mutation.error as any)?.message ||
                'Failed to send statement'}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Recipient Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="input"
              placeholder="you@example.com"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                From Date
              </label>
              <input
                type="date"
                value={from}
                onChange={(e) => setFrom(e.target.value)}
                className="input"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                To Date
              </label>
              <input
                type="date"
                value={to}
                onChange={(e) => setTo(e.target.value)}
                className="input"
                required
              />
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="btn btn-ghost flex-1"
              disabled={mutation.isPending}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={mutation.isPending || !email}
              className="btn btn-primary flex-1"
            >
              {mutation.isPending ? (
                <>
                  <Spinner size="sm" /> Sending…
                </>
              ) : (
                'Send Statement'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}