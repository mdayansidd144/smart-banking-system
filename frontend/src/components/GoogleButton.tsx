import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext';
declare global {
  interface Window {
    google?: any;
  }
}

interface Props {
  onError?: (msg: string) => void;
}

// Module-level flag prevents double-init in React StrictMode
let googleInitialized = false;

export default function GoogleButton({ onError }: Props) {
  const { googleLogin } = useAuth();
  const btnRef = useRef<HTMLDivElement>(null);
  const [ready, setReady] = useState(false);
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

  useEffect(() => {
    if (!clientId) return;

    const init = () => {
      try {
        if (!googleInitialized) {
          window.google.accounts.id.initialize({
            client_id: clientId,
            callback: handleCredential,
            auto_select: false,
            cancel_on_tap_outside: true,
          });
          googleInitialized = true;
        }

        if (btnRef.current) {
          window.google.accounts.id.renderButton(btnRef.current, {
            theme: 'outline',
            size: 'large',
            width: 320,
            text: 'continue_with',
            shape: 'rectangular',
            logo_alignment: 'left',
          });
        }
        setReady(true);
      } catch (e) {
        console.error('Google init failed:', e);
      }
    };

    async function handleCredential(response: any) {
      try {
        const idToken = response.credential;
        await googleLogin(idToken);
      } catch (e: any) {
        console.error('Google sign-in failed:', e);
        onError?.(e?.message || 'Google sign-in failed');
      }
    }

    if (window.google?.accounts?.id) {
      init();
    } else {
      const t = setInterval(() => {
        if (window.google?.accounts?.id) {
          clearInterval(t);
          init();
        }
      }, 200);
      return () => clearInterval(t);
    }
  }, [clientId, googleLogin, onError]);

  if (!clientId) {
    return (
      <div className="text-xs text-slate-500 text-center py-3 border border-dashed border-blue-200 rounded-lg">
        Google sign-in not configured
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center">
      <div ref={btnRef} />
      {!ready && (
        <div className="text-xs text-slate-400 mt-2">Loading Google…</div>
      )}
    </div>
  );
}