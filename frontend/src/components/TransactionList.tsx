import type { Transaction } from '../types';
interface Props {
  transactions: Transaction[];
  limit?: number;
  showAccount?: boolean;
}

export default function TransactionList({
  transactions,
  limit,
}: Props) {
  const list = limit ? transactions.slice(0, limit) : transactions;

  if (list.length === 0) {
    return (
      <div className="text-sm text-slate-500 py-4 text-center">
        No transactions yet
      </div>
    );
  }

  return (
    <ul className="divide-y divide-slate-700">
      {list.map((t) => {
        const isDeposit = t.type === 'DEPOSIT';
        return (
          <li key={t.id} className="flex items-center justify-between py-3">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <span
                  className={`text-xs font-medium ${
                    isDeposit ? 'text-green-400' : 'text-red-400'
                  }`}
                >
                  {isDeposit ? '↓' : '↑'} {t.type}
                </span>
              </div>
              <div className="text-xs text-slate-500 mt-1 truncate">
                {t.description || '—'}
              </div>
            </div>
            <div className="text-right ml-3">
              <div
                className={`text-sm font-semibold ${
                  isDeposit ? 'text-green-400' : 'text-red-400'
                }`}
              >
                {isDeposit ? '+' : '−'}₹{Number(t.amount).toLocaleString('en-IN')}
              </div>
              <div className="text-xs text-slate-500">
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
  );
}