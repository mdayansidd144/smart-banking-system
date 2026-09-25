import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as authApi from '../api/auth';
import { Spinner } from '../components/Spinner';
export default function SettingsPage() {
  const { user } = useAuth();

  const [displayName, setDisplayName] = useState(user?.displayName || user?.username || '');
  const [email, setEmail] = useState(user?.email || '');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [profileMsg, setProfileMsg] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);
  const [passMsg, setPassMsg] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);

  const [profileLoading, setProfileLoading] = useState(false);
  const [passLoading, setPassLoading] = useState(false);

  const saveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileMsg(null);
    setProfileLoading(true);
    try {
      await authApi.updateProfile({ displayName, email });
      setProfileMsg({ type: 'ok', text: 'Profile updated. Refresh to see changes.' });
    } catch (err: any) {
      setProfileMsg({
        type: 'err',
        text: err?.response?.data?.error || err?.message || 'Update failed',
      });
    } finally {
      setProfileLoading(false);
    }
  };

  const savePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setPassMsg(null);
    if (newPassword.length < 6) {
      setPassMsg({ type: 'err', text: 'New password must be at least 6 characters' });
      return;
    }
    if (newPassword !== confirmPassword) {
      setPassMsg({ type: 'err', text: 'Passwords do not match' });
      return;
    }
    setPassLoading(true);
    try {
      await authApi.changePassword({ currentPassword, newPassword });
      setPassMsg({ type: 'ok', text: 'Password changed successfully' });
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      setPassMsg({
        type: 'err',
        text: err?.response?.data?.error || err?.message || 'Password change failed',
      });
    } finally {
      setPassLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Settings</h2>
        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
          Manage your account preferences
        </p>
      </div>

      {/* Profile section */}
      <form onSubmit={saveProfile} className="card">
        <div className="card-header">
          <div className="card-title">Profile</div>
        </div>
        <div className="card-body space-y-4">
          {profileMsg && (
            <div
              className={`px-3 py-2.5 rounded-lg border text-xs ${
                profileMsg.type === 'ok'
                  ? 'bg-emerald-50 dark:bg-emerald-900/40 border-emerald-200 dark:border-emerald-700 text-emerald-700 dark:text-emerald-300'
                  : 'bg-rose-50 dark:bg-rose-900/40 border-rose-200 dark:border-rose-700 text-rose-700 dark:text-rose-300'
              }`}
            >
              {profileMsg.text}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Username
            </label>
            <input
              type="text"
              value={user?.username || ''}
              disabled
              className="input opacity-60 cursor-not-allowed"
            />
            <div className="text-[10px] text-slate-500 dark:text-slate-400 mt-1">
              Username cannot be changed
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Display Name
            </label>
            <input
              type="text"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              className="input"
              placeholder="Your display name"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="input"
              placeholder="you@example.com"
            />
          </div>

          <button
            type="submit"
            disabled={profileLoading}
            className="btn btn-primary"
          >
            {profileLoading ? (
              <>
                <Spinner size="sm" /> Saving…
              </>
            ) : (
              'Save Profile'
            )}
          </button>
        </div>
      </form>

      {/* Password section */}
      {user?.provider === 'LOCAL' || !user?.provider ? (
        <form onSubmit={savePassword} className="card">
          <div className="card-header">
            <div className="card-title">Change Password</div>
          </div>
          <div className="card-body space-y-4">
            {passMsg && (
              <div
                className={`px-3 py-2.5 rounded-lg border text-xs ${
                  passMsg.type === 'ok'
                    ? 'bg-emerald-50 dark:bg-emerald-900/40 border-emerald-200 dark:border-emerald-700 text-emerald-700 dark:text-emerald-300'
                    : 'bg-rose-50 dark:bg-rose-900/40 border-rose-200 dark:border-rose-700 text-rose-700 dark:text-rose-300'
                }`}
              >
                {passMsg.text}
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Current Password
              </label>
              <input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="input"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                New Password
              </label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="input"
                placeholder="min 6 characters"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Confirm New Password
              </label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="input"
                required
              />
            </div>

            <button
              type="submit"
              disabled={passLoading}
              className="btn btn-primary"
            >
              {passLoading ? (
                <>
                  <Spinner size="sm" /> Changing…
                </>
              ) : (
                'Change Password'
              )}
            </button>
          </div>
        </form>
      ) : (
        <div className="card">
          <div className="card-body">
            <div className="text-sm text-slate-500 dark:text-slate-400">
              Password change is not available for Google sign-in accounts.
            </div>
          </div>
        </div>
      )}
    </div>
  );
}