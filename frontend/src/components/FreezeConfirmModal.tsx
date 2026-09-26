import { useState } from 'react';
interface Props {
  open: boolean;
  accountNumber: string;
  loading: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
}

export default function FreezeConfirmModal({
  open,
  accountNumber,
  loading,
  onClose,
  onConfirm,
}: Props) {
  const [reason, setReason] = useState('Suspicious activity');

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fade-in">
      <div className="card w-full max-w-md relative overflow-hidden">
        <div className="card-body">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-xl bg-rose-100 flex items-center justify-center text-rose-600 text-xl font-bold">
              ❄️
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900">Freeze Account</h3>
              <p className="text-xs text-slate-500 font-mono">{accountNumber}</p>
            </div>
          </div>

          <div className="mb-4 px-3 py-2.5 rounded-lg bg-amber-50 border border-amber-200 text-xs text-amber-800">
            Freezing will block all deposits, withdrawals, and transfers on this
            account. The customer will be notified immediately.
          </div>

          <label className="block text-xs font-semibold text-slate-700 mb-1.5">
            Reason for freezing
          </label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            className="input resize-none"
            placeholder="e.g., Suspicious activity detected"
            disabled={loading}
          />

          <div className="flex gap-3 mt-6">
            <button
              onClick={onClose}
              disabled={loading}
              className="btn btn-ghost flex-1"
            >
              Cancel
            </button>
            <button
              onClick={() => onConfirm(reason)}
              disabled={loading || !reason.trim()}
              className="btn flex-1 bg-gradient-to-r from-rose-500 to-rose-600 text-white shadow-md shadow-rose-500/30 hover:shadow-lg hover:shadow-rose-500/40 disabled:opacity-50"
            >
              {loading ? 'Freezing…' : 'Freeze Account'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}