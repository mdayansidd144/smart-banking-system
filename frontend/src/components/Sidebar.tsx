import { NavLink } from 'react-router-dom';
import UserMenu from './UserMenu';
import { useAuth } from '../context/AuthContext';

const baseLinks = [
  { to: '/',          label: 'Dashboard', end: true },
  { to: '/accounts',  label: 'Accounts',  end: false },
  { to: '/analytics', label: 'Analytics', end: false },
  { to: '/budgets',   label: 'Budgets',   end: false },
  { to: '/bills',     label: 'Bills',     end: false },
  { to: '/fx',        label: 'FX Rates',  end: false },
  { to: '/anomalies', label: 'Anomalies', end: false },
  { to: '/loan',      label: 'AI Loan',   end: false },
  { to: '/agent',     label: 'Ask AI',    end: false },
  { to: '/audit',     label: 'Audit Log', end: false },
  { to: '/kyc',       label: 'KYC',       end: false },
];

const adminLinks = [
  { to: '/admin/kyc', label: 'KYC Admin', end: false },
];

export default function Sidebar() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const links = isAdmin ? [...baseLinks, ...adminLinks] : baseLinks;

  return (
    <aside
      className="w-60 flex flex-col shadow-sm dark:bg-slate-800 relative"
      style={{
        background: 'linear-gradient(180deg, #c7d5e8 0%, #d3ddec 40%, #e0e8f3 100%)',
      }}
    >
      {/* Header logo band — with the vertical silver-grey divider ONLY here */}
      <div
        className="h-16 flex items-center px-5 border-b border-blue-300/60 dark:border-slate-700 relative"
        style={{ background: 'linear-gradient(180deg, #c8dcf5 0%, #d9e7f7 100%)' }}
      >
        {/* Vertical divider — only spans this 64px header band */}
        <div
          className="absolute top-0 right-0 w-[3px] h-full pointer-events-none z-20"
          style={{
            background: 'linear-gradient(180deg, #94a3b8 0%, #a8b6c8 60%, #b8c4d4 100%)',
            borderTopLeftRadius: '10px',
            borderBottomLeftRadius: '4px',
            boxShadow: '0 0 6px rgba(100, 116, 139, 0.25)',
          }}
        />

        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center text-white font-bold shadow-md shadow-brand-500/30 transition-transform hover:scale-105">
            S
          </div>
          <div>
            <div className="text-sm font-bold text-slate-900 dark:text-slate-100 leading-tight">
              SmartBank
            </div>
            <div className="text-[10px] text-slate-700 dark:text-slate-400 uppercase tracking-wider">
              Dashboard
            </div>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.end}
            className={({ isActive }) =>
              `flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-md shadow-brand-500/30'
                  : 'text-slate-700 dark:text-slate-300 hover:bg-white/70 dark:hover:bg-slate-700/60 hover:text-brand-700 dark:hover:text-brand-300 hover:translate-x-0.5'
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>

      <UserMenu />
    </aside>
  );
}