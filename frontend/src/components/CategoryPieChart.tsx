import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import type { CategorySummary } from '../utils/aggregate';
import { CATEGORY_COLORS } from '../utils/categorize';
interface Props {
  data: CategorySummary[];
}

export default function CategoryPieChart({ data }: Props) {
  if (data.length === 0) {
    return (
      <div className="flex items-center justify-center h-[280px] text-sm text-slate-500 dark:text-slate-400">
        No spending data yet
      </div>
    );
  }

  const chartData = data.map((d) => ({
    name: d.category,
    value: d.total,
  }));

  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          innerRadius={60}
          outerRadius={95}
          paddingAngle={3}
          dataKey="value"
        >
          {chartData.map((entry, index) => (
            <Cell
              key={`cell-${index}`}
              fill={CATEGORY_COLORS[entry.name as keyof typeof CATEGORY_COLORS] || '#94a3b8'}
            />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{
            backgroundColor: 'white',
            border: '1px solid #dbeafe',
            borderRadius: '8px',
            fontSize: '12px',
          }}
          formatter={(value: any) => `₹${Number(value).toLocaleString('en-IN')}`}
        />
        <Legend
          verticalAlign="bottom"
          height={40}
          iconType="circle"
          wrapperStyle={{ fontSize: '11px' }}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}