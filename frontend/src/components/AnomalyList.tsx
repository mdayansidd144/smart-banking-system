import type { Anomaly } from '../types';

interface Props {
  anomalies: Anomaly[];
  limit?: number;
}

const severityBadge = (severity: string) => {
  const cls = {
    LOW: 'badge-low',
    MEDIUM: 'badge-medium',
    HIGH: 'badge-high',
    CRITICAL: 'badge-critical',
  }[severity] || 'badge-medium';
  return `badge ${cls}`;
};

export default function AnomalyList({ anomalies, limit }: Props) {
  const list = limit ? anomalies.slice(0, limit) : anomalies;

  if (list.length === 0) {
    return (
      <div className="text-sm text-slate-500 py-6 text-center">
        No anomalies detected
      </div>
    );
  }

  return (
    <ul className="divide-y divide-blue-50">
      {list.map((a) => {
        const isCritical = a.severity === 'CRITICAL';
        return (
          <li
            key={a.id}
            className={`py-3.5 row-hover -mx-6 px-6 rounded-lg transition-all ${
              isCritical ? 'bg-rose-50/30' : ''
            }`}
          >
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2 mb-1 flex-wrap">
                  <span className={severityBadge(a.severity)}>{a.severity}</span>
                  <span className="text-xs font-mono text-slate-600 font-medium">
                    {a.ruleTriggered}
                  </span>
                  {isCritical && (
                    <span className="badge bg-rose-100 text-rose-800 text-[10px] px-1.5 py-0 border border-rose-300">
                       AUTO-FROZE
                    </span>
                  )}
                </div>
                <div className="text-xs text-slate-600 font-medium">
                  {a.eventType} · INR {Number(a.amount).toLocaleString('en-IN')}
                </div>
                <div className="text-xs text-slate-500 mt-1 line-clamp-2">
                  {a.reason}
                </div>
              </div>
              <div className="text-right flex-shrink-0">
                <div className="text-xs text-slate-600 font-medium">
                  {new Date(a.createdAt).toLocaleTimeString('en-IN', {
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </div>
                <div className="text-xs text-slate-500 mt-1">
                  Score {a.riskScore}
                </div>
              </div>
            </div>
          </li>
        );
      })}
    </ul>
  );
}