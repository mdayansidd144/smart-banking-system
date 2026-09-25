import { useState, useEffect } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import type { Anomaly } from '../types';

interface Props {
  anomalies: Anomaly[];
}

const COLORS = {
  LOW: { top: '#34d399', bottom: '#059669' },
  MEDIUM: { top: '#fbbf24', bottom: '#d97706' },
  HIGH: { top: '#fb923c', bottom: '#ea580c' },
  CRITICAL: { top: '#f87171', bottom: '#dc2626' },
};

export default function Anomaly3DChart({ anomalies }: Props) {
  const [angle, setAngle] = useState(0);

  useEffect(() => {
    const id = setInterval(() => setAngle((a) => (a + 0.4) % 360), 50);
    return () => clearInterval(id);
  }, []);

  const counts = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
  anomalies.forEach((a) => {
    if (a.severity in counts) counts[a.severity as keyof typeof counts]++;
  });

  const data = Object.entries(counts)
    .filter(([, v]) => v > 0)
    .map(([name, value]) => ({ name, value }));

  if (data.length === 0) {
    return (
      <div className="flex items-center justify-center h-[320px] text-sm text-slate-500 dark:text-slate-400">
        No anomalies detected
      </div>
    );
  }

  const rotateY = Math.sin((angle * Math.PI) / 180) * 5;
  const rotateX = 4 + Math.cos((angle * Math.PI) / 180) * 2;

  return (
    <div className="relative" style={{ perspective: '1200px' }}>
      <div
        style={{
          transform: `rotateY(${rotateY}deg) rotateX(${rotateX}deg)`,
          transformStyle: 'preserve-3d',
          transition: 'transform 100ms linear',
        }}
      >
        <ResponsiveContainer width="100%" height={320}>
          <PieChart>
            <defs>
              {Object.entries(COLORS).map(([key, c]) => (
                <radialGradient
                  key={key}
                  id={`pieGradient-${key}`}
                  cx="50%"
                  cy="30%"
                  r="80%"
                  fx="50%"
                  fy="30%"
                >
                  <stop offset="0%" stopColor={c.top} stopOpacity={1} />
                  <stop offset="100%" stopColor={c.bottom} stopOpacity={1} />
                </radialGradient>
              ))}
              <filter id="pieShadow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="4" />
                <feOffset dx="0" dy="6" result="offsetblur" />
                <feComponentTransfer>
                  <feFuncA type="linear" slope="0.3" />
                </feComponentTransfer>
                <feMerge>
                  <feMergeNode />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
            </defs>

            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={65}
              outerRadius={105}
              paddingAngle={3}
              stroke="white"
              strokeWidth={2}
              filter="url(#pieShadow)"
              animationDuration={600}
              animationBegin={100}
            >
              {data.map((entry) => (
                <Cell
                  key={entry.name}
                  fill={`url(#pieGradient-${entry.name})`}
                />
              ))}
            </Pie>

            <Tooltip
              contentStyle={{
                backgroundColor: 'white',
                border: '1px solid #dbeafe',
                borderRadius: '8px',
                fontSize: '12px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
              }}
              formatter={(value: any, name: any) => [
                `${value} anomal${value === 1 ? 'y' : 'ies'}`,
                name,
              ]}
            />

            <Legend
              verticalAlign="bottom"
              height={36}
              iconType="circle"
              wrapperStyle={{ fontSize: '12px', fontWeight: 500 }}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div className="absolute top-2 right-2 text-[10px] text-slate-400 dark:text-slate-500 bg-white/70 dark:bg-slate-800/70 px-2 py-1 rounded pointer-events-none">
        Ambient 3D perspective
      </div>
    </div>
  );
}