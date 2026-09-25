import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
function avatarUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  return url;
}

function initials(name: string) {
  return name
    .split(' ')
    .map((s) => s[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();
}

export default function UserMenu() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const display = user.displayName || user.username;
  const picUrl = avatarUrl(user.profilePictureUrl);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="p-3 border-t border-blue-200/50 dark:border-slate-700">
      {/* Profile row */}
      <div className="flex items-center gap-3 mb-3">
        {picUrl ? (
          <img
            src={picUrl}
            alt={display}
            className="w-9 h-9 rounded-full object-cover border-2 border-white dark:border-slate-700 shadow-sm flex-shrink-0"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).style.display = 'none';
              e.currentTarget.nextElementSibling?.classList.remove('hidden');
            }}
          />
        ) : null}
        <div
          className={`w-9 h-9 rounded-full bg-gradient-to-br from-brand-500 to-brand-700 text-white text-xs font-bold flex items-center justify-center shadow-sm flex-shrink-0 ${
            picUrl ? 'hidden' : ''
          }`}
        >
          {initials(display)}
        </div>
        <div className="min-w-0 flex-1 text-left">
          <div className="text-xs font-semibold text-slate-800 dark:text-slate-100 truncate">
            {display}
          </div>
          <div className="text-[10px] text-slate-500 dark:text-slate-400 truncate">
            {user.email}
          </div>
        </div>
      </div>

      {/* Buttons with icons on the right */}
      <div className="space-y-1.5">
        <button
          onClick={() => navigate('/settings')}
          className="w-full flex items-center justify-between text-xs font-medium px-3 py-2 rounded-lg bg-white/70 dark:bg-slate-700/60 hover:bg-white dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 border border-blue-200 dark:border-slate-600 transition-all duration-200 hover:-translate-y-0.5"
        >
          <span>Settings</span>
          {/* Gear icon */}
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="w-3.5 h-3.5 text-slate-500 dark:text-slate-400"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        </button>
        <button
          onClick={handleLogout}
          className="w-full flex items-center justify-between text-xs font-medium px-3 py-2 rounded-lg bg-white/70 dark:bg-slate-700/60 hover:bg-white dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 border border-blue-200 dark:border-slate-600 transition-all duration-200 hover:-translate-y-0.5"
        >
          <span>Log Out</span>
          {/* Log out icon (arrow exiting) */}
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="w-3.5 h-3.5 text-slate-500 dark:text-slate-400"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
        </button>
      </div>
    </div>
  );
}