import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import type { Anomaly } from '../types';
interface Props {
  anomalies: Anomaly[];
}

export default function ActivityChart({ anomalies }: Props) {
  const days: { label: string; count: number; key: string }[] = [];
  const now = new Date();
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(now.getDate() - i);
    const key = d.toISOString().slice(0, 10); // YYYY-MM-DD
    const label = d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
    days.push({ key, label, count: 0 });
  }

  // Count anomalies per day
  anomalies.forEach((a) => {
    const dayKey = a.createdAt.slice(0, 10);
    const day = days.find((d) => d.key === dayKey);
    if (day) day.count++;
  });

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={days} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e0e7ff" vertical={false} />
        <XAxis
          dataKey="label"
          tick={{ fontSize: 11, fill: '#64748b' }}
          axisLine={false}
          tickLine={false}
        />
        <YAxis
          tick={{ fontSize: 11, fill: '#64748b' }}
          axisLine={false}
          tickLine={false}
          allowDecimals={false}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: 'white',
            border: '1px solid #dbeafe',
            borderRadius: '8px',
            fontSize: '12px',
          }}
          cursor={{ fill: 'rgba(59, 130, 246, 0.06)' }}
        />
        <Bar dataKey="count" fill="url(#barGradient)" radius={[6, 6, 0, 0]} />
        <defs>
          <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#3b82f6" />
            <stop offset="100%" stopColor="#93c5fd" />
          </linearGradient>
        </defs>
      </BarChart>
    </ResponsiveContainer>
  );
}