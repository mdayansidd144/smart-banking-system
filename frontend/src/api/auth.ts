import client from './client';
export interface AuthUser {
  token: string;
  type: string;
  userId: string;
  username: string;
  email: string;
  role: string;
  profilePictureUrl: string | null;
  displayName: string | null;
  provider?: string;
  kycVerified?: boolean;
  expiresIn: number;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface SignupPayload {
  username: string;
  email: string;
  password: string;
  avatar?: File | null;
}

// ---- Signup (multipart form-data because of optional avatar) ----
export const signup = async (payload: SignupPayload): Promise<AuthUser> => {
  const form = new FormData();
  form.append('username', payload.username);
  form.append('email', payload.email);
  form.append('password', payload.password);
  if (payload.avatar) {
    form.append('avatar', payload.avatar);
  }

  const { data } = await client.post('/auth/register', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  });
  return data;
};

// ---- Login ----
export const login = async (payload: LoginPayload): Promise<AuthUser> => {
  const { data } = await client.post('/auth/login', payload);
  return data;
};

// ---- Google sign-in (kept for future use) ----
export const googleSignIn = async (idToken: string): Promise<AuthUser> => {
  const { data } = await client.post('/auth/google', { idToken });
  return data;
};

// ---- Validate token ----
export const validateToken = async (token: string): Promise<boolean> => {
  try {
    const { data } = await client.get('/auth/validate', {
      headers: { Authorization: `Bearer ${token}` },
    });
    return data?.valid === true;
  } catch {
    return false;
  }
};

// ---- Get current user ----
export const getMe = async (): Promise<AuthUser> => {
  const token = getStoredToken();
  const { data } = await client.get('/auth/me', {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
};

// ---- Update profile ----
export const updateProfile = async (payload: {
  displayName?: string;
  email?: string;
}): Promise<AuthUser> => {
  const token = getStoredToken();
  const { data } = await client.patch('/auth/me', payload, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
};

// ---- Change password ----
export const changePassword = async (payload: {
  currentPassword: string;
  newPassword: string;
}): Promise<void> => {
  const token = getStoredToken();
  await client.post('/auth/change-password', payload, {
    headers: { Authorization: `Bearer ${token}` },
  });
};

// ---- Helper: get token from localStorage ----
function getStoredToken(): string {
  try {
    const raw = localStorage.getItem('smartbank.auth');
    if (raw) {
      const parsed = JSON.parse(raw);
      return parsed.token || '';
    }
  } catch {
    // ignore
  }
  return '';
}