import { useQuery } from '@tanstack/react-query';
import { getAccounts, getAnomalies, getFraudAlerts } from '../api/endpoints';
import StatCard from '../components/StatCard';
import AccountList from '../components/AccountList';
import AnomalyList from '../components/AnomalyList';
import AnomalySeverityChart from '../components/AnomalySeverityChart';
import ActivityChart from '../components/ActivityChart';
import { SkeletonCard, SkeletonList } from '../components/Skeleton';

export default function Dashboard() {
  const accountsQ = useQuery({ queryKey: ['accounts'], queryFn: getAccounts });
  const anomaliesQ = useQuery({ queryKey: ['anomalies'], queryFn: getAnomalies });
  const fraudQ = useQuery({ queryKey: ['fraud-alerts'], queryFn: getFraudAlerts });

  const accounts = accountsQ.data ?? [];
  const anomalies = anomaliesQ.data ?? [];
  const fraudAlerts = fraudQ.data ?? [];

  const totalBalance = accounts.reduce((s, a) => s + Number(a.balance || 0), 0);
  const openAnomalies = anomalies.filter((a) => a.status === 'OPEN').length;
  const openFraud = fraudAlerts.filter((a) => a.status === 'OPEN').length;

  return (
    <div className="space-y-6 max-w-7xl mx-auto animate-fade-in">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Overview</h2>
          <p className="text-sm text-slate-600 mt-1">
            Live snapshot of your banking operations
          </p>
        </div>
        <div className="text-xs text-brand-700 font-medium bg-white px-3 py-1.5 rounded-lg border border-blue-200 shadow-sm">
          Auto-refresh every 5s
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {accountsQ.isLoading ? (
          <>
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </>
        ) : (
          <>
            <StatCard
              title="Total Accounts"
              value={accounts.length}
              subtitle={`${accounts.filter((a) => a.status === 'ACTIVE').length} active`}
              accent="blue"
            />
            <StatCard
              title="Total Balance"
              value={`₹${totalBalance.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
              subtitle="Across all accounts"
              accent="green"
            />
            <StatCard
              title="Open Anomalies"
              value={openAnomalies}
              subtitle={`${anomalies.length} total`}
              accent="orange"
            />
            <StatCard
              title="Fraud Alerts"
              value={openFraud}
              subtitle={`${fraudAlerts.length} total`}
              accent="red"
            />
          </>
        )}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card card-hover">
          <div className="card-header">
            <div className="card-title">Anomalies by Severity</div>
            <span className="text-xs text-slate-500 font-medium">
              {anomalies.length} total
            </span>
          </div>
          <div className="card-body">
            {anomaliesQ.isLoading ? (
              <div className="h-[220px] flex items-center justify-center text-sm text-slate-500">
                Loading…
              </div>
            ) : (
              <AnomalySeverityChart anomalies={anomalies} />
            )}
          </div>
        </div>

        <div className="card card-hover">
          <div className="card-header">
            <div className="card-title">Activity — Last 7 Days</div>
            <span className="text-xs text-slate-500 font-medium">
              Anomalies per day
            </span>
          </div>
          <div className="card-body">
            {anomaliesQ.isLoading ? (
              <div className="h-[220px] flex items-center justify-center text-sm text-slate-500">
                Loading…
              </div>
            ) : (
              <ActivityChart anomalies={anomalies} />
            )}
          </div>
        </div>
      </div>

      {/* Lists */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card card-hover">
          <div className="card-header">
            <div className="card-title">Recent Accounts</div>
            <span className="text-xs text-slate-500 font-medium">
              {accounts.length} total
            </span>
          </div>
          <div className="card-body">
            {accountsQ.isLoading ? (
              <SkeletonList rows={6} />
            ) : (
              <AccountList accounts={accounts} limit={6} />
            )}
          </div>
        </div>

        <div className="card card-hover">
          <div className="card-header">
            <div className="card-title">Recent Anomalies</div>
            <span className="text-xs text-slate-500 font-medium">
              {anomalies.length} detected
            </span>
          </div>
          <div className="card-body">
            {anomaliesQ.isLoading ? (
              <SkeletonList rows={4} />
            ) : (
              <AnomalyList anomalies={anomalies} limit={5} />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}