import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
function avatarUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  // If it's a relative path (/api/v1/files/...), use it as-is (proxied by Vite/nginx)
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
    <div className="p-3 border-t border-blue-200/50">
      <div className="flex items-center gap-3 mb-3">
        {picUrl ? (
          <img
            src={picUrl}
            alt={display}
            className="w-9 h-9 rounded-full object-cover border-2 border-white shadow-sm"
            onError={(e) => {
              // Fallback to initials if image fails
              (e.currentTarget as HTMLImageElement).style.display = 'none';
              e.currentTarget.nextElementSibling?.classList.remove('hidden');
            }}
          />
        ) : null}
        <div
          className={`w-9 h-9 rounded-full bg-gradient-to-br from-brand-500 to-brand-700 text-white text-xs font-bold flex items-center justify-center shadow-sm ${
            picUrl ? 'hidden' : ''
          }`}
        >
          {initials(display)}
        </div>
        <div className="min-w-0 flex-1">
          <div className="text-xs font-semibold text-slate-800 truncate">
            {display}
          </div>
          <div className="text-[10px] text-slate-500 truncate">
            {user.email}
          </div>
        </div>
      </div>
      <button
        onClick={handleLogout}
        className="w-full text-xs font-medium px-3 py-2 rounded-lg bg-white/70 hover:bg-white text-slate-700 border border-blue-200 transition-all duration-200 hover:-translate-y-0.5"
      >
        Sign Out
      </button>
    </div>
  );
}