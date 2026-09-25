import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function Layout() {
  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-16 bg-white/70 backdrop-blur-md border-b border-blue-100 flex items-center px-6 justify-between shadow-sm">
          <div>
            <h1 className="text-base font-semibold text-slate-900">
              Real-Time Banking Dashboard
            </h1>
            <p className="text-xs text-slate-500 mt-0.5">
              Live account, transaction & fraud monitoring
            </p>
          </div>
          <div className="flex items-center gap-4">
            <LiveIndicator />
            <div className="h-8 w-px bg-blue-100" />
            <span className="text-xs text-slate-600 font-medium">
              {new Date().toLocaleString('en-IN', {
                day: '2-digit',
                month: 'short',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </span>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function LiveIndicator() {
  return (
    <div className="flex items-center gap-2 text-xs font-semibold text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-full border border-emerald-200 transition-all hover:bg-emerald-100">
      <span className="relative flex h-2 w-2">
        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-500 opacity-75" />
        <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
      </span>
      Live
    </div>
  );
}