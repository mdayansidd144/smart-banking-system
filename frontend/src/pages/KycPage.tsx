import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getKycStatus,
  submitKyc,
  type KycSubmitPayload,
  type KycStatus,
} from '../api/kyc';
import { Spinner } from '../components/Spinner';

function statusColor(status: KycStatus) {
  return {
    NOT_STARTED: 'text-slate-600 dark:text-slate-400',
    PENDING: 'text-amber-700 dark:text-amber-400',
    APPROVED: 'text-emerald-700 dark:text-emerald-400',
    REJECTED: 'text-rose-700 dark:text-rose-400',
  }[status];
}

function statusBadge(status: KycStatus) {
  return {
    NOT_STARTED: 'badge badge-gray',
    PENDING: 'badge badge-medium',
    APPROVED: 'badge badge-low',
    REJECTED: 'badge badge-critical',
  }[status];
}

export default function KycPage() {
  const qc = useQueryClient();
  const statusQ = useQuery({ queryKey: ['kyc-status'], queryFn: getKycStatus });

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<KycSubmitPayload>({
    fullName: '',
    dateOfBirth: '',
    address: '',
    city: '',
    postalCode: '',
    panNumber: '',
    aadhaarNumber: '',
  });
  const [error, setError] = useState<string | null>(null);

  const submission = statusQ.data;
  const status: KycStatus = submission?.status ?? 'NOT_STARTED';

  // Show form automatically if NOT_STARTED or REJECTED
  useEffect(() => {
    if (status === 'NOT_STARTED' || status === 'REJECTED') {
      setShowForm(true);
    } else {
      setShowForm(false);
    }
  }, [status]);

  const submitMut = useMutation({
    mutationFn: submitKyc,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['kyc-status'] });
      setError(null);
      setShowForm(false);
    },
    onError: (err: any) => {
      setError(err?.response?.data?.error || err?.message || 'Submission failed');
    },
  });

  const handleChange = (field: keyof KycSubmitPayload) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setForm((f) => ({ ...f, [field]: e.target.value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Client-side validation
    if (form.panNumber.length !== 10) {
      setError('PAN must be exactly 10 characters');
      return;
    }
    if (form.aadhaarNumber.length !== 12) {
      setError('Aadhaar must be exactly 12 digits');
      return;
    }

    submitMut.mutate(form);
  };

  if (statusQ.isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-3xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
          KYC Verification
        </h2>
        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
          Verify your identity to unlock the full SmartBank experience
        </p>
      </div>

      {/* Status card */}
      <div className="card">
        <div className="card-body">
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div>
              <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                Verification Status
              </div>
              <div className={`text-2xl font-bold mt-1 ${statusColor(status)}`}>
                {status.replace('_', ' ')}
              </div>
              {submission?.submittedAt && (
                <div className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                  Submitted on{' '}
                  {new Date(submission.submittedAt).toLocaleString('en-IN', {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </div>
              )}
            </div>
            <span className={statusBadge(status)}>{status.replace('_', ' ')}</span>
          </div>

          {status === 'PENDING' && (
            <div className="mt-4 px-3 py-2.5 rounded-lg bg-amber-50 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-700 text-xs text-amber-800 dark:text-amber-300">
               Your documents are under review. This typically takes 1–2 business days.
            </div>
          )}

          {status === 'APPROVED' && (
            <div className="mt-4 px-3 py-2.5 rounded-lg bg-emerald-50 dark:bg-emerald-900/30 border border-emerald-200 dark:border-emerald-700 text-xs text-emerald-800 dark:text-emerald-300">
               Your KYC is verified. You have full access to SmartBank.
            </div>
          )}

          {status === 'REJECTED' && submission?.rejectionReason && (
            <div className="mt-4 px-3 py-2.5 rounded-lg bg-rose-50 dark:bg-rose-900/30 border border-rose-200 dark:border-rose-700 text-xs text-rose-800 dark:text-rose-300">
               Rejected: {submission.rejectionReason}
              <br />
              <span className="font-medium">You can re-submit with corrected information below.</span>
            </div>
          )}
        </div>
      </div>

      {/* Submitted documents preview */}
      {submission?.documents && submission.documents.length > 0 && (
        <div className="card">
          <div className="card-header">
            <div className="card-title">Submitted Documents</div>
          </div>
          <div className="card-body">
            <ul className="divide-y divide-blue-50 dark:divide-slate-700">
              {submission.documents.map((doc) => (
                <li key={doc.id} className="py-3 flex items-center justify-between">
                  <div>
                    <div className="text-sm font-semibold text-slate-800 dark:text-slate-100">
                      {doc.docType}
                    </div>
                    <div className="text-xs text-slate-500 dark:text-slate-400 font-mono">
                      {doc.documentNumber}
                    </div>
                  </div>
                  <span className="badge badge-gray">Received</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {/* Submission form */}
      {showForm && (
        <form onSubmit={handleSubmit} className="card">
          <div className="card-header">
            <div className="card-title">
              {status === 'REJECTED' ? 'Resubmit Documents' : 'Submit Documents'}
            </div>
          </div>
          <div className="card-body space-y-4">
            {error && (
              <div className="px-3 py-2.5 rounded-lg bg-rose-50 dark:bg-rose-900/30 border border-rose-200 dark:border-rose-700 text-xs text-rose-700 dark:text-rose-300">
                {error}
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                value={form.fullName}
                onChange={handleChange('fullName')}
                className="input"
                placeholder="As on your PAN card"
                required
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Date of Birth
                </label>
                <input
                  type="date"
                  value={form.dateOfBirth}
                  onChange={handleChange('dateOfBirth')}
                  className="input"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Postal Code
                </label>
                <input
                  type="text"
                  value={form.postalCode}
                  onChange={handleChange('postalCode')}
                  className="input"
                  placeholder="560001"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Address
              </label>
              <input
                type="text"
                value={form.address}
                onChange={handleChange('address')}
                className="input"
                placeholder="123 Main Street"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                City
              </label>
              <input
                type="text"
                value={form.city}
                onChange={handleChange('city')}
                className="input"
                placeholder="Bangalore"
                required
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-blue-100 dark:border-slate-700">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  PAN Number
                </label>
                <input
                  type="text"
                  value={form.panNumber}
                  onChange={handleChange('panNumber')}
                  className="input font-mono uppercase"
                  placeholder="ABCDE1234F"
                  maxLength={10}
                  required
                />
                <div className="text-[10px] text-slate-500 dark:text-slate-400 mt-1">
                  Format: 5 letters + 4 digits + 1 letter
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  Aadhaar Number
                </label>
                <input
                  type="text"
                  value={form.aadhaarNumber}
                  onChange={handleChange('aadhaarNumber')}
                  className="input font-mono"
                  placeholder="123456789012"
                  maxLength={12}
                  required
                />
                <div className="text-[10px] text-slate-500 dark:text-slate-400 mt-1">
                  12-digit number
                </div>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitMut.isPending}
              className="btn btn-primary w-full"
            >
              {submitMut.isPending ? (
                <>
                  <Spinner size="sm" /> Submitting…
                </>
              ) : status === 'REJECTED' ? (
                'Resubmit for Verification'
              ) : (
                'Submit for Verification'
              )}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}