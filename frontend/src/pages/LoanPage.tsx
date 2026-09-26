import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  applyForLoan,
  getAccounts,
  getLoans,
  type LoanResponse,
} from '../api/endpoints';
import AiProgressPanel from '../components/AiProgressPanel';

export default function LoanPage() {
  const qc = useQueryClient();
  const accountsQ = useQuery({ queryKey: ['accounts'], queryFn: getAccounts });
  const loansQ = useQuery({ queryKey: ['loans'], queryFn: getLoans });

  const [accountId, setAccountId] = useState('');
  const [amount, setAmount] = useState('100000');
  const [termMonths, setTermMonths] = useState('12');
  const [purpose, setPurpose] = useState('Home renovation');
  const [result, setResult] = useState<LoanResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: applyForLoan,
    onSuccess: (data) => {
      setResult(data);
      setError(null);
      qc.invalidateQueries({ queryKey: ['loans'] });
    },
    onError: (err: any) => {
      setError(
        err?.response?.data?.message ||
          err?.message ||
          'Loan application failed. Please try again.'
      );
      setResult(null);
    },
  });

  const accounts = accountsQ.data ?? [];

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    setResult(null);
    setError(null);
    if (!accountId) {
      setError('Please select an account');
      return;
    }
    mutation.mutate({
      accountId,
      amount: Number(amount),
      termMonths: Number(termMonths),
      purpose,
    });
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">
          AI Loan Application
        </h2>
        <p className="text-sm text-slate-600 mt-1">
          Submit a loan request — our AI credit analyst will evaluate it in real
          time
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <form onSubmit={submit} className="card card-hover">
          <div className="card-header">
            <div className="card-title">Loan Details</div>
          </div>
          <div className="card-body space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Account
              </label>
              <select
                value={accountId}
                onChange={(e) => setAccountId(e.target.value)}
                className="input"
                required
              >
                <option value="">Select an account</option>
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.ownerName} — {a.accountNumber} ({a.currency}{' '}
                    {Number(a.balance).toLocaleString('en-IN')})
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Amount (INR)
                </label>
                <input
                  type="number"
                  min="1000"
                  step="1000"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="input"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Term (months)
                </label>
                <input
                  type="number"
                  min="1"
                  max="120"
                  value={termMonths}
                  onChange={(e) => setTermMonths(e.target.value)}
                  className="input"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Purpose
              </label>
              <input
                type="text"
                value={purpose}
                onChange={(e) => setPurpose(e.target.value)}
                className="input"
                placeholder="e.g. Home renovation"
              />
            </div>

            <button
              type="submit"
              disabled={mutation.isPending}
              className="btn btn-primary w-full"
            >
              {mutation.isPending ? 'Analyzing…' : 'Submit Application'}
            </button>
          </div>
        </form>

        <div className="space-y-6">
          {mutation.isPending && <AiProgressPanel active />}

          {error && !mutation.isPending && (
            <div className="card border-red-200 bg-gradient-to-br from-red-50/80 to-white animate-fade-in">
              <div className="card-body">
                <div className="text-sm font-semibold text-rose-700 mb-1">
                  Application failed
                </div>
                <div className="text-xs text-rose-600">{error}</div>
              </div>
            </div>
          )}

          {result && !mutation.isPending && <ResultCard result={result} />}

          {!result && !mutation.isPending && !error && (
            <div className="card">
              <div className="card-body">
                <div className="text-sm text-slate-500">
                  Submit an application to see the AI decision here.
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="card card-hover">
        <div className="card-header">
          <div className="card-title">Recent Applications</div>
          <span className="text-xs text-slate-500 font-medium">
            {(loansQ.data ?? []).length} total
          </span>
        </div>
        <div className="card-body">
          {loansQ.isLoading ? (
            <div className="text-sm text-slate-500">Loading…</div>
          ) : (loansQ.data ?? []).length === 0 ? (
            <div className="text-sm text-slate-500 py-4 text-center">
              No applications yet
            </div>
          ) : (
            <ul className="divide-y divide-blue-50">
              {(loansQ.data ?? []).slice(0, 8).map((l) => (
                <li
                  key={l.id}
                  className="py-3 flex items-center justify-between row-hover -mx-6 px-6 rounded-lg transition-all"
                >
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <span className={decisionBadge(l.decision)}>
                        {l.decision}
                      </span>
                      <span className="text-xs font-mono text-slate-500">
                        Score {l.riskScore}
                      </span>
                    </div>
                    <div className="text-xs text-slate-600 mt-1">
                      {l.purpose} · ₹
                      {Number(l.requestedAmount).toLocaleString('en-IN')} ·{' '}
                      {l.termMonths} months
                    </div>
                  </div>
                  <div className="text-right flex-shrink-0 ml-3">
                    <div className="text-sm font-bold text-slate-900">
                      {l.interestRate}%
                    </div>
                    <div className="text-xs text-slate-500">
                      {new Date(l.createdAt).toLocaleDateString('en-IN', {
                        day: '2-digit',
                        month: 'short',
                      })}
                    </div>
                    {l.decision === 'APPROVED' && (
                      <Link
                        to={`/loans/${l.id}/amortization`}
                        className="text-xs text-brand-600 hover:text-brand-700 font-semibold mt-1 inline-block"
                      >
                         View Schedule →
                      </Link>
                    )}
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

function decisionBadge(decision: string) {
  const cls =
    {
      APPROVED: 'badge-low',
      MANUAL_REVIEW: 'badge-medium',
      REJECTED: 'badge-critical',
    }[decision] || 'badge-gray';
  return `badge ${cls}`;
}

function ResultCard({ result }: { result: LoanResponse }) {
  const accent =
    {
      APPROVED: 'emerald',
      MANUAL_REVIEW: 'amber',
      REJECTED: 'rose',
    }[result.decision] || 'blue';

  const accentBorder =
    {
      emerald: 'border-emerald-200 bg-gradient-to-br from-emerald-50/60 to-white',
      amber: 'border-amber-200 bg-gradient-to-br from-amber-50/60 to-white',
      rose: 'border-rose-200 bg-gradient-to-br from-rose-50/60 to-white',
      blue: 'border-blue-200',
    }[accent];

  return (
    <div className={`card card-hover ${accentBorder} animate-fade-in`}>
      <div className="card-body space-y-5">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
              Decision
            </div>
            <div className="text-xl font-bold text-slate-900 mt-1">
              {result.decision.replace('_', ' ')}
            </div>
          </div>
          <div className="text-right">
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
              Risk Score
            </div>
            <div className="text-2xl font-bold text-slate-900 mt-1 tabular-nums">
              {result.riskScore}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 pt-4 border-t border-blue-100">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
              Approved Amount
            </div>
            <div className="text-lg font-bold text-slate-900 mt-1 tabular-nums">
              ₹{Number(result.approvedAmount).toLocaleString('en-IN')}
            </div>
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
              Interest Rate
            </div>
            <div className="text-lg font-bold text-slate-900 mt-1 tabular-nums">
              {result.interestRate}%
            </div>
          </div>
        </div>

        <div className="pt-4 border-t border-blue-100">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">
            AI Reasoning
          </div>
          <div className="text-sm text-slate-700 leading-relaxed">
            {result.reasoning}
          </div>
        </div>

        {result.decision === 'APPROVED' && (
          <div className="pt-4 border-t border-blue-100">
            <Link
              to={`/loans/${result.id}/amortization`}
              className="btn btn-primary w-full"
            >
               View Amortization Schedule
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}