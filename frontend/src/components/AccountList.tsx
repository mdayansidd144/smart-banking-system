import { Link } from 'react-router-dom';
import type { Account } from '../types';

interface Props {
  accounts: Account[];
  limit?: number;
}

export default function AccountList({ accounts, limit }: Props) {
  const list = limit ? accounts.slice(0, limit) : accounts;

  if (list.length === 0) {
    return (
      <div className="text-sm text-slate-500 py-6 text-center">
        No accounts yet
      </div>
    );
  }

  return (
    <ul className="divide-y divide-blue-50">
      {list.map((a) => {
        const isFrozen = a.status === 'FROZEN';
        return (
          <li key={a.id}>
            <Link
              to={`/accounts/${a.id}`}
              className="flex items-center justify-between py-3.5 -mx-6 px-6 row-hover transition-all"
            >
              <div className="min-w-0 flex-1">
                <div className="text-sm font-semibold text-slate-900 truncate flex items-center gap-2">
                  {a.ownerName}
                  {isFrozen && (
                    <span className="badge badge-critical text-[10px] px-1.5 py-0">
                      ❄️ FROZEN
                    </span>
                  )}
                </div>
                <div className="text-xs text-slate-500 font-mono truncate mt-0.5">
                  {a.accountNumber}
                </div>
              </div>
              <div className="ml-3 text-right flex-shrink-0">
                <div className="text-sm font-bold text-slate-900 tabular-nums">
                  {a.currency} {Number(a.balance).toLocaleString('en-IN')}
                </div>
                <div
                  className={`text-xs font-medium mt-0.5 transition-colors ${
                    a.status === 'ACTIVE'
                      ? 'text-emerald-600'
                      : a.status === 'FROZEN'
                      ? 'text-rose-600'
                      : 'text-slate-500'
                  }`}
                >
                  {a.status}
                </div>
              </div>
            </Link>
          </li>
        );
      })}
    </ul>
  );
}