import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import ThemeToggle from './ThemeToggle';
import GlobalSearch from './GlobalSearch';
import NotificationsBell from './NotificationsBell';

export default function Layout() {
  const [searchOpen, setSearchOpen] = useState(false);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setSearchOpen(true);
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);

  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <header
          className="h-16 backdrop-blur-md border-b border-blue-300/60 dark:border-slate-700 flex items-center px-6 justify-between shadow-sm dark:bg-slate-800/70 relative z-50"
          style={{
            background: 'linear-gradient(180deg, #c8dcf5 0%, #d9e7f7 100%)',
          }}
        >
          <div>
            <h1 className="text-base font-semibold text-slate-900 dark:text-slate-100">
              Real-Time Banking Dashboard
            </h1>
            <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
              Live account, transaction & fraud monitoring
            </p>
          </div>
          <div className="flex items-center gap-3">
            <LiveIndicator />

            <button
              onClick={() => setSearchOpen(true)}
              title="Search accounts (Ctrl+K)"
              className="hidden sm:flex items-center gap-2 px-3 py-2 rounded-lg border border-blue-300 dark:border-slate-600 bg-white/90 dark:bg-slate-800 hover:bg-white dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs transition-all duration-200"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="w-4 h-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.3-4.3" />
              </svg>
              <span>Search</span>
              <kbd className="ml-2 px-1.5 py-0.5 text-[10px] font-mono bg-white dark:bg-slate-700 rounded border border-slate-200 dark:border-slate-600">
                ⌘K
              </kbd>
            </button>

            <NotificationsBell />

            <div className="h-8 w-px bg-blue-300/70 dark:bg-slate-700" />
            <span className="hidden md:inline text-xs text-slate-700 dark:text-slate-300 font-medium">
              {new Date().toLocaleString('en-IN', {
                day: '2-digit',
                month: 'short',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </span>
            <ThemeToggle />
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          <Outlet />
        </main>
      </div>

      <GlobalSearch open={searchOpen} onClose={() => setSearchOpen(false)} />
    </div>
  );
}

function LiveIndicator() {
  return (
    <div className="hidden md:flex items-center gap-2 text-xs font-semibold text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-900/40 px-3 py-1.5 rounded-full border border-emerald-200 dark:border-emerald-700 transition-all hover:bg-emerald-100 dark:hover:bg-emerald-900/60">
      <span className="relative flex h-2 w-2">
        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-500 opacity-75" />
        <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
      </span>
      Live
    </div>
  );
}