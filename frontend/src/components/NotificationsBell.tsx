import { useState, useRef, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getNotifications } from '../api/notifications';
import type { Notification } from '../api/notifications';
import { Spinner } from './Spinner';

function statusBadge(status: string) {
  const map: Record<string, string> = {
    SENT: 'badge badge-low',
    PENDING: 'badge badge-medium',
    FAILED: 'badge badge-critical',
  };
  return map[status] || 'badge badge-gray';
}

function relativeTime(iso: string) {
  const t = new Date(iso).getTime();
  const diff = Date.now() - t;
  const m = Math.floor(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  const d = Math.floor(h / 24);
  if (d < 7) return `${d}d ago`;
  return new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
}

export default function NotificationsBell() {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  const notifQ = useQuery({
    queryKey: ['notifications', 'recent'],
    queryFn: () => getNotifications(0, 10),
    refetchInterval: 30000,
  });

  const notifs = notifQ.data?.content ?? [];
  const total = notifQ.data?.totalElements ?? 0;

  // Close on outside click
  useEffect(() => {
    if (!open) return;
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    window.addEventListener('mousedown', handleClick);
    return () => window.removeEventListener('mousedown', handleClick);
  }, [open]);

  // Close on Escape
  useEffect(() => {
    if (!open) return;
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [open]);

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        title="Notifications"
        aria-label="Notifications"
        className="relative w-9 h-9 rounded-lg border border-blue-200 dark:border-slate-600 bg-white dark:bg-slate-800 hover:bg-blue-50 dark:hover:bg-slate-700 flex items-center justify-center transition-all duration-200 hover:scale-105 text-slate-600 dark:text-slate-300"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="w-5 h-5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
          <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
        </svg>
        {total > 0 && (
          <span className="absolute -top-1 -right-1 min-w-[18px] h-[18px] rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center px-1 shadow-sm">
            {total > 99 ? '99+' : total}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-96 max-w-[calc(100vw-2rem)] bg-white dark:bg-slate-800 rounded-xl shadow-2xl border border-blue-100 dark:border-slate-700 overflow-hidden z-50 animate-fade-in">
          <div className="flex items-center justify-between px-4 py-3 border-b border-blue-100 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/50">
            <div className="text-sm font-semibold text-slate-800 dark:text-slate-200">
              Notifications
            </div>
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
              {total} total
            </span>
          </div>

          <div className="max-h-96 overflow-y-auto">
            {notifQ.isLoading ? (
              <div className="flex items-center justify-center py-10">
                <Spinner size="md" />
              </div>
            ) : notifs.length === 0 ? (
              <div className="text-center py-10 text-sm text-slate-500 dark:text-slate-400">
                No notifications yet
              </div>
            ) : (
              <ul className="divide-y divide-blue-50 dark:divide-slate-700">
                {notifs.map((n) => (
                  <NotificationRow key={n.id} notification={n} />
                ))}
              </ul>
            )}
          </div>

          <div className="px-4 py-2.5 border-t border-blue-100 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/50 text-[10px] text-slate-500 dark:text-slate-400 text-center">
            Auto-refreshes every 30s
          </div>
        </div>
      )}
    </div>
  );
}

function NotificationRow({ notification: n }: { notification: Notification }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <li>
      <button
        onClick={() => setExpanded((e) => !e)}
        className="w-full text-left px-4 py-3 hover:bg-blue-50/60 dark:hover:bg-slate-700/60 transition-colors"
      >
        <div className="flex items-start gap-3">
          <div className="flex-shrink-0 mt-0.5">
            <span className={statusBadge(n.status)}>{n.status}</span>
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-sm font-medium text-slate-800 dark:text-slate-200 truncate">
              {n.subject}
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 truncate">
              {n.eventType} · {n.channel}
            </div>
            {expanded && (
              <div className="mt-2 space-y-1">
                <div className="text-xs text-slate-600 dark:text-slate-400 font-mono break-all">
                  {n.recipient}
                </div>
                {n.failureReason && (
                  <div className="text-xs text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-900/30 rounded p-2 border border-rose-100 dark:border-rose-800/50">
                    {n.failureReason}
                  </div>
                )}
              </div>
            )}
          </div>
          <div className="text-[10px] text-slate-400 flex-shrink-0 font-medium">
            {relativeTime(n.createdAt)}
          </div>
        </div>
      </button>
    </li>
  );
}