import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAuditLogs } from '../api/audit';
import type { AuditLog, AuditAction } from '../api/audit';
import { SkeletonList } from '../components/Skeleton';

const ACTION_OPTIONS: (AuditAction | 'ALL')[] = [
  'ALL',
  'CREATE_ACCOUNT',
  'DEPOSIT',
  'WITHDRAWAL',
  'TRANSFER',
  'TRANSFER_REVERSE',
  'SCHEDULE_TRANSFER',
  'CANCEL_SCHEDULED_TRANSFER',
  'DOWNLOAD_STATEMENT',
];

const actionBadge = (action: string) => {
  const colors: Record<string, string> = {
    CREATE_ACCOUNT: 'badge badge-low',
    DEPOSIT: 'badge badge-low',
    WITHDRAWAL: 'badge badge-medium',
    TRANSFER: 'badge badge-high',
    TRANSFER_REVERSE: 'badge badge-critical',
    SCHEDULE_TRANSFER: 'badge badge-medium',
    CANCEL_SCHEDULED_TRANSFER: 'badge badge-gray',
    DOWNLOAD_STATEMENT: 'badge badge-gray',
  };
  return colors[action] || 'badge badge-gray';
};

export default function AuditLogPage() {
  const [action, setAction] = useState<string>('ALL');
  const [accountId, setAccountId] = useState<string>('');
  const [username, setUsername] = useState<string>('');
  const [page, setPage] = useState(0);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const filters = {
    action: action === 'ALL' ? undefined : action,
    accountId: accountId.trim() || undefined,
    username: username.trim() || undefined,
    page,
    size: 20,
  };

  const logsQ = useQuery({
    queryKey: ['audit-logs', filters],
    queryFn: () => getAuditLogs(filters),
  });

  const logs = logsQ.data?.content ?? [];
  const totalElements = logsQ.data?.totalElements ?? 0;
  const totalPages = logsQ.data?.totalPages ?? 0;
  const isFirst = logsQ.data?.first ?? true;
  const isLast = logsQ.data?.last ?? true;

  return (
    <div className="space-y-6 max-w-7xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Audit Log</h2>
        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
          Immutable record of every action on customer accounts
        </p>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="card-body">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Action
              </label>
              <select
                value={action}
                onChange={(e) => {
                  setAction(e.target.value);
                  setPage(0);
                }}
                className="input"
              >
                {ACTION_OPTIONS.map((a) => (
                  <option key={a} value={a}>
                    {a === 'ALL' ? 'All Actions' : a}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Account ID
              </label>
              <input
                type="text"
                value={accountId}
                onChange={(e) => {
                  setAccountId(e.target.value);
                  setPage(0);
                }}
                className="input font-mono text-xs"
                placeholder="UUID (optional)"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Username
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value);
                  setPage(0);
                }}
                className="input"
                placeholder="e.g., ayan"
              />
            </div>

            <div className="flex items-end">
              <button
                onClick={() => {
                  setAction('ALL');
                  setAccountId('');
                  setUsername('');
                  setPage(0);
                }}
                className="btn btn-ghost w-full"
              >
                Clear Filters
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Results */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">Audit Records</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {totalElements} total
          </span>
        </div>
        <div className="card-body">
          {logsQ.isLoading ? (
            <SkeletonList rows={8} />
          ) : logs.length === 0 ? (
            <div className="text-sm text-slate-500 dark:text-slate-400 py-10 text-center">
              No audit records match your filters
            </div>
          ) : (
            <ul className="divide-y divide-blue-50 dark:divide-slate-700">
              {logs.map((log) => (
                <li key={log.id} className="py-3">
                  <button
                    onClick={() => setExpandedId(expandedId === log.id ? null : log.id)}
                    className="w-full text-left hover:bg-blue-50/40 dark:hover:bg-slate-700/40 -mx-3 px-3 py-2 rounded-lg transition-colors"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2 mb-1 flex-wrap">
                          <span className={actionBadge(log.action)}>{log.action}</span>
                          {log.username && (
                            <span className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                              {log.username}
                            </span>
                          )}
                          {!log.success && (
                            <span className="badge badge-critical">FAILED</span>
                          )}
                        </div>
                        {log.resourceType && (
                          <div className="text-xs text-slate-500 dark:text-slate-400 font-mono">
                            {log.resourceType}
                            {log.resourceId && ` · ${log.resourceId.slice(0, 8)}…`}
                          </div>
                        )}
                        {log.accountId && (
                          <div className="text-xs text-slate-500 dark:text-slate-400 font-mono">
                            acct: {log.accountId.slice(0, 8)}…
                          </div>
                        )}
                      </div>
                      <div className="text-right flex-shrink-0">
                        <div className="text-xs text-slate-600 dark:text-slate-300 font-medium">
                          {new Date(log.createdAt).toLocaleString('en-IN', {
                            day: '2-digit',
                            month: 'short',
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit',
                          })}
                        </div>
                        {log.ipAddress && (
                          <div className="text-[10px] text-slate-400 font-mono mt-0.5">
                            {log.ipAddress}
                          </div>
                        )}
                      </div>
                    </div>
                  </button>

                  {expandedId === log.id && <ExpandedDetail log={log} />}
                </li>
              ))}
            </ul>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-6 pt-4 border-t border-blue-100 dark:border-slate-700">
              <span className="text-xs text-slate-500 dark:text-slate-400">
                Page {page + 1} of {totalPages}
              </span>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={isFirst}
                  className="btn btn-ghost btn-sm"
                >
                  ← Previous
                </button>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={isLast}
                  className="btn btn-ghost btn-sm"
                >
                  Next →
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function ExpandedDetail({ log }: { log: AuditLog }) {
  return (
    <div className="mt-2 mx-3 p-3 rounded-lg bg-blue-50/50 dark:bg-slate-700/40 border border-blue-100 dark:border-slate-600 text-xs space-y-2">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
        <Detail label="Log ID" value={log.id} mono />
        <Detail label="Action" value={log.action} />
        {log.accountId && <Detail label="Account ID" value={log.accountId} mono />}
        {log.resourceId && <Detail label="Resource ID" value={log.resourceId} mono />}
        {log.resourceType && <Detail label="Resource Type" value={log.resourceType} />}
        {log.userId && <Detail label="User ID" value={log.userId} mono />}
        {log.username && <Detail label="Username" value={log.username} />}
        {log.ipAddress && <Detail label="IP Address" value={log.ipAddress} mono />}
        {log.userAgent && <Detail label="User Agent" value={log.userAgent} />}
        <Detail
          label="Status"
          value={log.success ? '✓ Success' : '✗ Failed'}
        />
      </div>

      {log.errorMessage && (
        <div>
          <div className="font-semibold text-rose-700 dark:text-rose-400 mb-1">
            Error Message
          </div>
          <div className="font-mono text-rose-600 dark:text-rose-300 bg-rose-50 dark:bg-rose-900/30 p-2 rounded">
            {log.errorMessage}
          </div>
        </div>
      )}

      {log.details && (
        <div>
          <div className="font-semibold text-slate-700 dark:text-slate-300 mb-1">
            Details
          </div>
          <pre className="font-mono text-slate-700 dark:text-slate-300 bg-white dark:bg-slate-800 p-2 rounded overflow-x-auto whitespace-pre-wrap break-all">
            {log.details}
          </pre>
        </div>
      )}
    </div>
  );
}

function Detail({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div>
      <div className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
        {label}
      </div>
      <div
        className={`text-xs text-slate-700 dark:text-slate-200 mt-0.5 break-all ${
          mono ? 'font-mono' : ''
        }`}
      >
        {value}
      </div>
    </div>
  );
}