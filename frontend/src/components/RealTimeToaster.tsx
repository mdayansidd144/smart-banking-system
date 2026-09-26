import { useEffect, useState } from 'react';
import { ws, type WsEvent } from '../api/ws';
interface Toast { id: string; title: string; body: string; accent: string; }
const META: Record<string, { title: string; accent: string }> = {
  'anomaly.detected':  { title: ' New anomaly',     accent: 'border-rose-300 bg-rose-50 text-rose-900' },
  'account.frozen':    { title: ' Account frozen',  accent: 'border-rose-300 bg-rose-50 text-rose-900' },
  'money.deposited':   { title: ' Deposit received', accent: 'border-emerald-300 bg-emerald-50 text-emerald-900' },
  'money.withdrawn':   { title: ' Withdrawal',       accent: 'border-amber-300 bg-amber-50 text-amber-900' },
  'money.transferred': { title: ' Transfer',         accent: 'border-blue-300 bg-blue-50 text-blue-900' },
  'account.created':   { title: ' New account',      accent: 'border-blue-300 bg-blue-50 text-blue-900' },
  'budget.alert':      { title: ' Budget alert',     accent: 'border-amber-300 bg-amber-50 text-amber-900' },
  'bill.alert':        { title: ' Bill alert',       accent: 'border-violet-300 bg-violet-50 text-violet-900' },
};

export default function RealtimeToaster() {
  const [toasts, setToasts] = useState<Toast[]>([]);

  useEffect(() => {
    return ws.onEvent((event: WsEvent) => {
      const meta = META[event.type];
      if (!meta) return;
      const id = Math.random().toString(36).slice(2);
      setToasts((p) => [...p, { id, title: meta.title, body: summarize(event), accent: meta.accent }]);
      setTimeout(() => setToasts((p) => p.filter((t) => t.id !== id)), 5000);
    });
  }, []);

  if (!toasts.length) return null;

  return (
    <div className="fixed top-20 right-6 z-50 space-y-2 max-w-sm">
      {toasts.map((t) => (
        <div key={t.id} className={`rounded-xl border px-4 py-3 shadow-lg animate-fade-in backdrop-blur-sm ${t.accent}`}>
          <div className="text-xs font-bold">{t.title}</div>
          <div className="text-[11px] mt-0.5 opacity-90">{t.body}</div>
        </div>
      ))}
    </div>
  );
}

function summarize(event: WsEvent): string {
  const d = event.data || {};
  const amount = d.amount ?? d.approvedAmount ?? null;
  const acc = event.accountId ? event.accountId.slice(0, 8) + '…' : '—';
  switch (event.type) {
    case 'money.deposited':   return `₹${Number(amount ?? 0).toLocaleString('en-IN')} deposited to ${acc}`;
    case 'money.withdrawn':   return `₹${Number(amount ?? 0).toLocaleString('en-IN')} withdrawn from ${acc}`;
    case 'money.transferred': return `₹${Number(amount ?? 0).toLocaleString('en-IN')} transferred`;
    case 'anomaly.detected':  return `${d.ruleTriggered ?? 'Anomaly'} · ${d.severity ?? ''} · ${acc}`;
    case 'account.frozen':    return `Account ${acc} frozen — ${d.reason ?? 'no reason'}`;
    case 'account.created':   return `Account ${d.accountNumber ?? acc} opened`;
    case 'budget.alert':      return `${d.category ?? 'Budget'} — ${d.percentageUsed ?? 0}% used`;
    case 'bill.alert':        return `${d.billerName ?? 'Bill'} — ${d.alertType ?? ''}`;
    default: return event.type;
  }
}