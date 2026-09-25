interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  accent?: 'blue' | 'green' | 'orange' | 'red';
  tint?:
    | 'blue'
    | 'pink'
    | 'green'
    | 'hazel'
    | 'sapphire'
    | 'ruby'
    | 'amber'
    | 'violet'
    | 'teal';
}

const accentMap = {
  blue:   { bg: 'bg-blue-50',    text: 'text-blue-700',    border: 'border-blue-100',    hover: 'hover:border-blue-300' },
  green:  { bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-100', hover: 'hover:border-emerald-300' },
  orange: { bg: 'bg-amber-50',   text: 'text-amber-700',   border: 'border-amber-100',   hover: 'hover:border-amber-300' },
  red:    { bg: 'bg-rose-50',    text: 'text-rose-700',    border: 'border-rose-100',    hover: 'hover:border-rose-300' },
};

export default function StatCard({
  title,
  value,
  subtitle,
  accent = 'blue',
  tint,
}: StatCardProps) {
  const a = accentMap[accent];
  const tintClass = tint ? `card-tint-${tint}` : '';

  return (
    <div className={`card card-hover ${a.border} ${a.hover} ${tintClass} cursor-default`}>
      <div className="card-body">
        <div className="text-xs font-semibold text-slate-600 uppercase tracking-wide">
          {title}
        </div>
        <div className={`text-3xl font-bold mt-2 ${a.text} tabular-nums transition-all`}>
          {value}
        </div>
        {subtitle && (
          <div
            className={`text-xs mt-2 ${a.bg} ${a.text} inline-block px-2 py-0.5 rounded-md font-medium`}
          >
            {subtitle}
          </div>
        )}
      </div>
    </div>
  );
}