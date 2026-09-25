import type { CategorySummary } from '../utils/aggregate';
import { CATEGORY_COLORS } from '../utils/categorize';
interface Props {
  data: CategorySummary[];
}

export default function TopCategoriesList({ data }: Props) {
  if (data.length === 0) {
    return (
      <div className="text-sm text-slate-500 dark:text-slate-400 py-6 text-center">
        No spending data
      </div>
    );
  }

  const total = data.reduce((s, d) => s + d.total, 0);
  const top = data.slice(0, 7);

  return (
    <ul className="space-y-3">
      {top.map((d) => {
        const pct = total > 0 ? (d.total / total) * 100 : 0;
        const color = CATEGORY_COLORS[d.category as keyof typeof CATEGORY_COLORS] || '#94a3b8';

        return (
          <li key={d.category}>
            <div className="flex items-center justify-between mb-1.5">
              <div className="flex items-center gap-2">
                <span
                  className="w-2.5 h-2.5 rounded-full"
                  style={{ backgroundColor: color }}
                />
                <span className="text-sm font-medium text-slate-700 dark:text-slate-200">
                  {d.category}
                </span>
                <span className="text-xs text-slate-400 dark:text-slate-500">
                  ({d.count} tx)
                </span>
              </div>
              <div className="text-right">
                <div className="text-sm font-semibold text-slate-800 dark:text-slate-100 tabular-nums">
                  ₹{d.total.toLocaleString('en-IN')}
                </div>
                <div className="text-[10px] text-slate-500 dark:text-slate-400 tabular-nums">
                  {pct.toFixed(1)}%
                </div>
              </div>
            </div>
            <div className="h-1.5 w-full rounded-full bg-blue-50 dark:bg-slate-700 overflow-hidden">
              <div
                className="h-full rounded-full transition-all duration-500"
                style={{ width: `${pct}%`, backgroundColor: color }}
              />
            </div>
          </li>
        );
      })}
    </ul>
  );
}