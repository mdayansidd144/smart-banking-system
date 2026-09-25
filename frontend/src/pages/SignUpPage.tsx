import { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Spinner } from '../components/Spinner';

export default function SignupPage() {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const fileRef = useRef<HTMLInputElement>(null);

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [avatar, setAvatar] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const onPickFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0] || null;
    setAvatar(f);
    if (f) {
      const reader = new FileReader();
      reader.onload = (ev) => setPreview(ev.target?.result as string);
      reader.readAsDataURL(f);
    } else {
      setPreview(null);
    }
  };

  const clearAvatar = () => {
    setAvatar(null);
    setPreview(null);
    if (fileRef.current) fileRef.current.value = '';
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }
    if (avatar && avatar.size > 2 * 1024 * 1024) {
      setError('Profile picture must be under 2 MB');
      return;
    }

    setLoading(true);
    try {
      await signup({ username, email, password, avatar });
      navigate('/', { replace: true });
    } catch (err: any) {
      setError(
        err?.response?.data?.error ||
          err?.message ||
          'Signup failed. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4 py-8"
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

        <div className="card">
          <div className="card-body">
            <h2 className="text-2xl font-bold text-slate-900">Create account</h2>
            <p className="text-sm text-slate-500 mt-1 mb-6">
              Start banking smarter in seconds
            </p>

            {error && (
              <div className="mb-4 px-3 py-2.5 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-700">
                {error}
              </div>
            )}

            <form onSubmit={submit} className="space-y-4">
              <div className="flex flex-col items-center mb-2">
                <div className="relative">
                  {preview ? (
                    <img
                      src={preview}
                      alt="preview"
                      className="w-20 h-20 rounded-full object-cover border-4 border-white shadow-md"
                    />
                  ) : (
                    <div className="w-20 h-20 rounded-full bg-gradient-to-br from-brand-100 to-brand-200 flex items-center justify-center text-2xl text-brand-700 font-bold shadow-sm">
                      ?
                    </div>
                  )}
                  <button
                    type="button"
                    onClick={() => fileRef.current?.click()}
                    className="absolute -bottom-1 -right-1 w-8 h-8 rounded-full bg-brand-600 hover:bg-brand-700 text-white text-sm shadow-md transition-all duration-200 hover:scale-105"
                    title="Upload photo"
                  >
                    +
                  </button>
                  {preview && (
                    <button
                      type="button"
                      onClick={clearAvatar}
                      className="absolute -top-1 -right-1 w-6 h-6 rounded-full bg-white border border-blue-200 text-slate-500 hover:text-rose-600 hover:border-rose-300 text-xs shadow-sm transition-colors"
                      title="Remove"
                    >
                      ×
                    </button>
                  )}
                </div>
                <input
                  ref={fileRef}
                  type="file"
                  accept="image/*"
                  onChange={onPickFile}
                  className="hidden"
                />
                <div className="text-[10px] text-slate-500 mt-2">
                  Profile picture (optional, max 2 MB)
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Username
                </label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="input"
                  placeholder="johndoe"
                  autoComplete="username"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Email
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="input"
                  placeholder="you@example.com"
                  autoComplete="email"
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
                  placeholder="min 6 characters"
                  autoComplete="new-password"
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
                    <Spinner size="sm" /> Creating account…
                  </>
                ) : (
                  'Create Account'
                )}
              </button>
            </form>

            {/*
             * Google Sign-In — temporarily disabled
             * Same reason as LoginPage — to re-enable, uncomment.
             */}

            <div className="text-center text-xs text-slate-500 mt-6">
              Already have an account?{' '}
              <Link
                to="/login"
                className="text-brand-600 hover:text-brand-700 font-semibold"
              >
                Sign in →
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}