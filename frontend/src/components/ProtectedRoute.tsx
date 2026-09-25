import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { PageLoader } from './Spinner';
import { useQuery } from '@tanstack/react-query';
import { getKycStatus } from '../api/kyc';
interface Props {
  children: React.ReactNode;
}

const KYC_REQUIRED_PATHS = [
  '/accounts',
  '/loan',
];

export default function ProtectedRoute({ children }: Props) {
  const { user, loading } = useAuth();
  const location = useLocation();

  const kycQ = useQuery({
    queryKey: ['kyc-status'],
    queryFn: getKycStatus,
    enabled: !!user,
    staleTime: 30_000,
  });

  if (loading) return <PageLoader message="Checking session…" />;

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const isAdmin = user.role === 'ADMIN';
  if (isAdmin) return <>{children}</>;
  if (kycQ.isLoading) return <PageLoader message="Checking verification…" />;

  const kycStatus = kycQ.data?.status ?? 'NOT_STARTED';
  const kycVerified = kycStatus === 'APPROVED';
  const needsKyc = KYC_REQUIRED_PATHS.some((p) =>
    location.pathname.startsWith(p)
  );

  if (!kycVerified && needsKyc) {
    return <Navigate to="/kyc" replace />;
  }

  return <>{children}</>;
}