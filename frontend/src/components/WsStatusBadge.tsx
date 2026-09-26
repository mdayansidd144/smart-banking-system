import { useEffect, useState } from 'react';
import { ws, type WsStatus } from '../api/ws';
export default function WsStatusBadge() {
  const [status, setStatus] = useState<WsStatus>(ws.getStatus());

  useEffect(() => ws.onStatus(setStatus), []);

  const config = {
    connected:    { dot: 'bg-emerald-500', ring: 'ring-emerald-200', bg: 'bg-emerald-50', text: 'text-emerald-700', label: 'Live' },
    connecting:   { dot: 'bg-amber-500',   ring: 'ring-amber-200',   bg: 'bg-amber-50',   text: 'text-amber-700',   label: 'Connecting…' },
    disconnected: { dot: 'bg-rose-500',    ring: 'ring-rose-200',    bg: 'bg-rose-50',    text: 'text-rose-700',    label: 'Offline' },
  }[status];

  return (
    <div className={`hidden md:flex items-center gap-2 text-xs font-semibold ${config.text} ${config.bg} px-3 py-1.5 rounded-full ring-1 ${config.ring}`}>
      <span className="relative flex h-2 w-2">
        {status === 'connected' && (
          <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${config.dot} opacity-75`} />
        )}
        <span className={`relative inline-flex rounded-full h-2 w-2 ${config.dot}`} />
      </span>
      {config.label}
    </div>
  );
}