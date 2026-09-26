import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import {
  getAmortization,
  type AmortizationEntry,
} from '../api/endpoints';
import { toCsv, downloadCsv } from '../utils/csv';

export default function LoanAmortizationPage() {
  const { id = '' } = useParams<{ id: string }>();

  const q = useQuery({
    queryKey: ['amortization', id],
    queryFn: () => getAmortization(id),
    enabled: !!id,
  });

  const schedule = q.data;

  const handleExportCsv = () => {
    if (!schedule) return;
    const csv = toCsv(schedule.entries, [
      { key: 'month', label: 'Month' },
      { key: 'emi', label: 'EMI' },
      { key: 'principalComponent', label: 'Principal' },
      { key: 'interestComponent', label: 'Interest' },
      { key: 'remainingBalance', label: 'Balance' },
      { key: 'cumulativeInterest', label: 'Cumulative Interest' },
    ]);
    downloadCsv(`amortization-${schedule.loanId.slice(0, 8)}.csv`, csv);
  };

  const handlePrint = () => window.print();

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div>
        <Link
          to="/loan"
          className="text-xs text-brand-600 hover:text-brand-700 font-medium"
        >
          ← Back to Loans
        </Link>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mt-2">
          Loan Amortization Schedule
        </h2>
        {schedule && (
          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
            Loan #{schedule.loanId.slice(0, 8)} · ₹
            {Number(schedule.principal).toLocaleString('en-IN')} ·{' '}
            {schedule.termMonths} months @ {schedule.annualInterestRate}% p.a.
          </p>
        )}
      </div>

      {q.isLoading && (
        <div className="card">
          <div className="card-body text-sm text-slate-500 text-center py-12">
            Loading schedule…
          </div>
        </div>
      )}

      {q.isError && (
        <div className="card border-rose-200 bg-rose-50/60">
          <div className="card-body">
            <div className="text-sm font-semibold text-rose-800 mb-1">
              Could not load amortization
            </div>
            <div className="text-xs text-rose-700">
              {(q.error as any)?.response?.data?.message ||
                (q.error as any)?.message ||
                'Unknown error'}
            </div>
          </div>
        </div>
      )}

      {schedule && (
        <>
          {/* Summary cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <SummaryCard
              label="Monthly EMI"
              value={`₹${Number(schedule.monthlyEmi).toLocaleString('en-IN')}`}
              tint="card-tint-sapphire"
            />
            <SummaryCard
              label="Total Interest"
              value={`₹${Number(schedule.totalInterest).toLocaleString('en-IN')}`}
              tint="card-tint-amber"
            />
            <SummaryCard
              label="Total Payment"
              value={`₹${Number(schedule.totalPayment).toLocaleString('en-IN')}`}
              tint="card-tint-blue"
            />
            <SummaryCard
              label="Principal"
              value={`₹${Number(schedule.principal).toLocaleString('en-IN')}`}
              tint="card-tint-green"
            />
          </div>

          {/* Actions */}
          <div className="flex flex-wrap gap-3">
            <button onClick={handleExportCsv} className="btn btn-primary">
              Export CSV
            </button>
            <button onClick={handlePrint} className="btn btn-ghost">
              Print / Save as PDF
            </button>
          </div>

          {/* Table */}
          <div className="card">
            <div className="card-header">
              <div className="card-title">Month-by-month breakdown</div>
              <span className="text-xs text-slate-500 font-medium">
                {schedule.entries.length} payments
              </span>
            </div>
            <div className="card-body p-0">
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-slate-50 dark:bg-slate-800/60">
                    <tr className="text-left text-xs uppercase tracking-wide text-slate-600 dark:text-slate-300">
                      <th className="px-4 py-3 font-semibold">#</th>
                      <th className="px-4 py-3 font-semibold text-right">EMI</th>
                      <th className="px-4 py-3 font-semibold text-right">
                        Principal
                      </th>
                      <th className="px-4 py-3 font-semibold text-right">
                        Interest
                      </th>
                      <th className="px-4 py-3 font-semibold text-right">
                        Balance
                      </th>
                      <th className="px-4 py-3 font-semibold text-right">
                        Cum. Interest
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
                    {schedule.entries.map((e) => (
                      <Row key={e.month} entry={e} />
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

function SummaryCard({
  label,
  value,
  tint,
}: {
  label: string;
  value: string;
  tint: string;
}) {
  return (
    <div className={`card card-hover ${tint}`}>
      <div className="card-body">
        <div className="text-xs font-semibold text-slate-600 uppercase tracking-wide">
          {label}
        </div>
        <div className="text-xl font-bold text-slate-900 mt-2 tabular-nums">
          {value}
        </div>
      </div>
    </div>
  );
}

function Row({ entry }: { entry: AmortizationEntry }) {
  return (
    <tr className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors">
      <td className="px-4 py-2.5 font-mono text-xs text-slate-600 dark:text-slate-400">
        {entry.month}
      </td>
      <td className="px-4 py-2.5 text-right tabular-nums">
        ₹{Number(entry.emi).toLocaleString('en-IN')}
      </td>
      <td className="px-4 py-2.5 text-right tabular-nums text-emerald-700 dark:text-emerald-400">
        ₹{Number(entry.principalComponent).toLocaleString('en-IN')}
      </td>
      <td className="px-4 py-2.5 text-right tabular-nums text-amber-700 dark:text-amber-400">
        ₹{Number(entry.interestComponent).toLocaleString('en-IN')}
      </td>
      <td className="px-4 py-2.5 text-right tabular-nums text-slate-700 dark:text-slate-300">
        ₹{Number(entry.remainingBalance).toLocaleString('en-IN')}
      </td>
      <td className="px-4 py-2.5 text-right tabular-nums text-slate-500 dark:text-slate-400">
        ₹{Number(entry.cumulativeInterest).toLocaleString('en-IN')}
      </td>
    </tr>
  );
}