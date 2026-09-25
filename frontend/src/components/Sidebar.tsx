import { NavLink } from 'react-router-dom';
import UserMenu from './UserMenu';
const links = [
  { to: '/',          label: 'Dashboard', end: true },
  { to: '/accounts',  label: 'Accounts',  end: false },
  { to: '/anomalies', label: 'Anomalies', end: false },
  { to: '/loan',      label: 'AI Loan',   end: false },
  { to: '/agent',     label: 'Ask AI',    end: false },
];

export default function Sidebar() {
  return (
    <aside
      className="w-60 border-r border-blue-100 flex flex-col shadow-sm"
      style={{
        background: 'linear-gradient(180deg, #dbeafe 0%, #eff6ff 40%, #f8fbff 100%)',
      }}
    >
      <div
        className="h-16 flex items-center px-5 border-b border-blue-200/50"
        style={{ background: 'linear-gradient(180deg, #c7dcff 0%, #dbeafe 100%)' }}
      >
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center text-white font-bold shadow-md shadow-brand-500/30 transition-transform hover:scale-105">
            S
          </div>
          <div>
            <div className="text-sm font-bold text-slate-900 leading-tight">SmartBank</div>
            <div className="text-[10px] text-slate-600 uppercase tracking-wider">Dashboard</div>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-3 space-y-1">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.end}
            className={({ isActive }) =>
              `flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-md shadow-brand-500/30'
                  : 'text-slate-700 hover:bg-white/70 hover:text-brand-700 hover:translate-x-0.5'
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