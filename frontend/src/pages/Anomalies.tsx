import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAnomalies, updateAnomalyStatus } from '../api/endpoints';
import { SkeletonList } from '../components/Skeleton';

const SEVERITIES = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];

export default function Anomalies() {
  const qc = useQueryClient();
  const anomaliesQ = useQuery({ queryKey: ['anomalies'], queryFn: getAnomalies });
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const mutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      updateAnomalyStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['anomalies'] });
    },
  });

  const all = anomaliesQ.data ?? [];
  const filtered = all.filter((a) => {
    if (severityFilter !== 'ALL' && a.severity !== severityFilter) return false;
    if (statusFilter !== 'ALL' && a.status !== statusFilter) return false;
    return true;
  });

  return (
    <div className="space-y-6 max-w-7xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Anomalies</h2>
        <p className="text-sm text-slate-600 mt-1">
          Suspicious activity flagged by the anomaly detection engine
        </p>
      </div>

      <div className="flex items-center gap-3 flex-wrap">
        <div className="flex items-center gap-2 bg-white border border-blue-200 rounded-lg p-1 shadow-sm">
          {SEVERITIES.map((s) => (
            <button
              key={s}
              onClick={() => setSeverityFilter(s)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200 ${
                severityFilter === s
                  ? 'bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-sm'
                  : 'text-slate-600 hover:bg-blue-50'
              }`}
            >
              {s}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2 bg-white border border-blue-200 rounded-lg p-1 shadow-sm">
          {['ALL', 'OPEN', 'REVIEWED', 'CONFIRMED_FRAUD'].map((s) => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200 ${
                statusFilter === s
                  ? 'bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-sm'
                  : 'text-slate-600 hover:bg-blue-50'
              }`}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>

        <div className="ml-auto text-xs text-slate-500 font-medium bg-white px-3 py-1.5 rounded-lg border border-blue-200">
          {filtered.length} of {all.length}
        </div>
      </div>

      <div className="card card-hover">
        <div className="card-body">
          {anomaliesQ.isLoading ? (
            <SkeletonList rows={8} />
          ) : filtered.length === 0 ? (
            <div className="text-sm text-slate-500 py-10 text-center">
              No anomalies match your filters
            </div>
          ) : (
            <ul className="divide-y divide-blue-50">
              {filtered.map((a) => (
                <li key={a.id} className="py-4 row-hover -mx-6 px-6 rounded-lg transition-all">
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2 mb-2 flex-wrap">
                        <span className={severityClass(a.severity)}>
                          {a.severity}
                        </span>
                        <span className="text-xs font-mono text-slate-600 font-medium">
                          {a.ruleTriggered}
                        </span>
                        <span className="badge badge-gray">{a.status}</span>
                      </div>
                      <div className="text-sm text-slate-700 font-medium">
                        {a.eventType} · INR{' '}
                        {Number(a.amount).toLocaleString('en-IN')}
                      </div>
                      <div className="text-xs text-slate-500 mt-1.5">
                        {a.reason}
                      </div>
                      <div className="text-xs text-slate-400 font-mono mt-2">
                        Account: {a.accountId}
                      </div>
                    </div>
                    <div className="text-right flex-shrink-0">
                      <div className="text-sm font-bold text-slate-900 tabular-nums">
                        Score {a.riskScore}
                      </div>
                      <div className="text-xs text-slate-500 mt-1">
                        {new Date(a.createdAt).toLocaleString('en-IN', {
                          day: '2-digit',
                          month: 'short',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </div>
                      {a.status === 'OPEN' && (
                        <button
                          onClick={() =>
                            mutation.mutate({ id: a.id, status: 'REVIEWED' })
                          }
                          disabled={mutation.isPending}
                          className="btn btn-ghost btn-sm mt-2"
                        >
                          Mark Reviewed
                        </button>
                      )}
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

function severityClass(severity: string) {
  const map: Record<string, string> = {
    LOW: 'badge badge-low',
    MEDIUM: 'badge badge-medium',
    HIGH: 'badge badge-high',
    CRITICAL: 'badge badge-critical',
  };
  return map[severity] || 'badge badge-gray';
}