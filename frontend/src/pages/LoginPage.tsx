import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Spinner } from '../components/Spinner';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as any)?.from?.pathname || '/';

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login({ username, password });
      navigate(from, { replace: true });
    } catch (err: any) {
      setError(
        err?.response?.data?.error ||
          err?.message ||
          'Invalid username or password'
      );
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{
        background: 'linear-gradient(135deg, #dbeafe 0%, #e0eaff 40%, #eef4ff 100%)',
      }}
    >
      <div className="w-full max-w-md">
        <div className="flex items-center justify-center gap-3 mb-6">
          <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center text-white font-bold text-lg shadow-lg shadow-brand-500/30">
            S
          </div>
          <div>
            <div className="text-xl font-bold text-slate-900">SmartBank</div>
            <div className="text-[10px] text-slate-500 uppercase tracking-wider">
              Dashboard
            </div>
          </div>
        </div>

        <div className="card relative overflow-hidden">
          {/* Blue overlay loader */}
          {loading && (
            <div className="absolute inset-0 z-20 flex flex-col items-center justify-center bg-blue-600/95 backdrop-blur-sm animate-fade-in">
              <div className="w-14 h-14 border-4 border-white/30 border-t-white rounded-full animate-spin" />
              <div className="mt-4 text-white font-semibold text-sm">
                Signing in…
              </div>
              <div className="mt-1 text-white/70 text-xs">
                Verifying your credentials
              </div>
            </div>
          )}

          <div className="card-body">
            <h2 className="text-2xl font-bold text-slate-900">Welcome back</h2>
            <p className="text-sm text-slate-500 mt-1 mb-6">
              Sign in to your SmartBank account
            </p>

            {error && (
              <div className="mb-4 px-3 py-2.5 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-700">
                {error}
              </div>
            )}

            <form onSubmit={submit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Username or Email
                </label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="input"
                  placeholder="your username"
                  autoComplete="username"
                  disabled={loading}
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="input"
                  placeholder="••••••••"
                  autoComplete="current-password"
                  disabled={loading}
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary w-full"
              >
                {loading ? (
                  <>
                    <Spinner size="sm" /> Signing in…
                  </>
                ) : (
                  'Sign In'
                )}
              </button>
            </form>

            <div className="text-center text-xs text-slate-500 mt-6">
              Don't have an account?{' '}
              <Link
                to="/signup"
                className="text-brand-600 hover:text-brand-700 font-semibold"
              >
                Sign up →
              </Link>
            </div>
          </div>
        </div>

        <div className="text-center text-[10px] text-slate-400 mt-6">
          SmartBank — AI-powered banking platform
        </div>
      </div>
    </div>
  );
}