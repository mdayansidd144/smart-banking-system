import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getAccounts } from '../api/endpoints';
import { SkeletonList } from '../components/Skeleton';

export default function Accounts() {
  const accountsQ = useQuery({ queryKey: ['accounts'], queryFn: getAccounts });
  const [search, setSearch] = useState('');

  const accounts = accountsQ.data ?? [];
  const filtered = accounts.filter(
    (a) =>
      a.ownerName.toLowerCase().includes(search.toLowerCase()) ||
      a.accountNumber.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6 max-w-7xl mx-auto animate-fade-in">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Accounts</h2>
          <p className="text-sm text-slate-600 mt-1">
            All customer accounts across the bank
          </p>
        </div>
        <input
          type="text"
          placeholder="Search by name or account number"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="input w-72"
        />
      </div>

      <div className="card card-hover">
        <div className="card-header">
          <div className="card-title">All Accounts</div>
          <span className="text-xs text-slate-500 font-medium">
            {filtered.length} of {accounts.length}
          </span>
        </div>
        <div className="card-body">
          {accountsQ.isLoading ? (
            <SkeletonList rows={8} />
          ) : filtered.length === 0 ? (
            <div className="text-sm text-slate-500 py-8 text-center">
              No accounts match your search
            </div>
          ) : (
            <ul className="divide-y divide-blue-50">
              {filtered.map((a) => (
                <li key={a.id}>
                  <Link
                    to={`/accounts/${a.id}`}
                    className="flex items-center justify-between py-4 -mx-6 px-6 row-hover transition-all"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="text-sm font-semibold text-slate-900">
                        {a.ownerName}
                      </div>
                      <div className="text-xs text-slate-500 font-mono mt-1">
                        {a.accountNumber}
                      </div>
                    </div>
                    <div className="text-right ml-4 flex-shrink-0">
                      <div className="text-base font-bold text-slate-900 tabular-nums">
                        {a.currency} {Number(a.balance).toLocaleString('en-IN')}
                      </div>
                      <div
                        className={`text-xs font-semibold mt-1 ${
                          a.status === 'ACTIVE'
                            ? 'text-emerald-600'
                            : 'text-slate-500'
                        }`}
                      >
                        {a.status}
                      </div>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}