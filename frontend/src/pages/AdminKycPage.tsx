import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  listAllKyc,
  approveKyc,
  rejectKyc,
  type KycSubmission,
} from '../api/kyc';
import { Spinner } from '../components/Spinner';

function statusBadge(status: string) {
  return {
    PENDING: 'badge badge-medium',
    APPROVED: 'badge badge-low',
    REJECTED: 'badge badge-critical',
    NOT_STARTED: 'badge badge-gray',
  }[status] || 'badge badge-gray';
}

export default function AdminKycPage() {
  const qc = useQueryClient();
  const kycQ = useQuery({ queryKey: ['admin-kyc'], queryFn: listAllKyc });

  const [rejecting, setRejecting] = useState<string | null>(null);
  const [reason, setReason] = useState('');

  const approveMut = useMutation({
    mutationFn: approveKyc,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-kyc'] }),
  });

  const rejectMut = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      rejectKyc(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-kyc'] });
      setRejecting(null);
      setReason('');
    },
  });

  const submissions = kycQ.data ?? [];
  const pending = submissions.filter((s) => s.status === 'PENDING');

  return (
    <div className="space-y-6 max-w-6xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
          KYC Admin Panel
        </h2>
        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
          Review and approve customer verification requests
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="card">
          <div className="card-body">
            <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">
              Total
            </div>
            <div className="text-2xl font-bold text-slate-900 dark:text-slate-100 mt-1">
              {submissions.length}
            </div>
          </div>
        </div>
        <div className="card">
          <div className="card-body">
            <div className="text-xs font-semibold text-amber-600 dark:text-amber-400 uppercase">
              Pending
            </div>
            <div className="text-2xl font-bold text-amber-700 dark:text-amber-400 mt-1">
              {pending.length}
            </div>
          </div>
        </div>
        <div className="card">
          <div className="card-body">
            <div className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 uppercase">
              Approved
            </div>
            <div className="text-2xl font-bold text-emerald-700 dark:text-emerald-400 mt-1">
              {submissions.filter((s) => s.status === 'APPROVED').length}
            </div>
          </div>
        </div>
        <div className="card">
          <div className="card-body">
            <div className="text-xs font-semibold text-rose-600 dark:text-rose-400 uppercase">
              Rejected
            </div>
            <div className="text-2xl font-bold text-rose-700 dark:text-rose-400 mt-1">
              {submissions.filter((s) => s.status === 'REJECTED').length}
            </div>
          </div>
        </div>
      </div>

      {/* Submissions list */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">All Submissions</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {submissions.length} total
          </span>
        </div>
        <div className="card-body">
          {kycQ.isLoading ? (
            <div className="flex justify-center py-8">
              <Spinner size="md" />
            </div>
          ) : submissions.length === 0 ? (
            <div className="text-sm text-slate-500 dark:text-slate-400 py-10 text-center">
              No KYC submissions yet
            </div>
          ) : (
            <ul className="divide-y divide-blue-50 dark:divide-slate-700">
              {submissions.map((s) => (
                <SubmissionRow
                  key={s.id}
                  submission={s}
                  onApprove={() => s.id && approveMut.mutate(s.id)}
                  onReject={() => s.id && setRejecting(s.id)}
                  rejecting={rejecting === s.id}
                  reason={reason}
                  setReason={setReason}
                  onRejectSubmit={() =>
                    s.id && rejectMut.mutate({ id: s.id, reason })
                  }
                  onRejectCancel={() => {
                    setRejecting(null);
                    setReason('');
                  }}
                  approvePending={approveMut.isPending}
                  rejectPending={rejectMut.isPending}
                />
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

interface RowProps {
  submission: KycSubmission;
  onApprove: () => void;
  onReject: () => void;
  rejecting: boolean;
  reason: string;
  setReason: (r: string) => void;
  onRejectSubmit: () => void;
  onRejectCancel: () => void;
  approvePending: boolean;
  rejectPending: boolean;
}

function SubmissionRow({
  submission: s,
  onApprove,
  onReject,
  rejecting,
  reason,
  setReason,
  onRejectSubmit,
  onRejectCancel,
  approvePending,
  rejectPending,
}: RowProps) {
  const [expanded, setExpanded] = useState(false);

  return (
    <li className="py-4">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
              {s.fullName || s.username}
            </span>
            <span className={statusBadge(s.status)}>{s.status}</span>
          </div>
          <div className="text-xs text-slate-500 dark:text-slate-400 font-mono">
            {s.username} · {s.email}
          </div>
          <button
            onClick={() => setExpanded((e) => !e)}
            className="text-xs text-brand-600 hover:text-brand-700 font-medium mt-2"
          >
            {expanded ? '▼ Hide details' : '▶ Show details'}
          </button>
        </div>

        <div className="flex gap-2 flex-shrink-0">
          {s.status === 'PENDING' && !rejecting && (
            <>
              <button
                onClick={onApprove}
                disabled={approvePending}
                className="btn btn-primary btn-sm"
              >
                Approve
              </button>
              <button
                onClick={onReject}
                disabled={rejectPending}
                className="btn btn-ghost btn-sm text-rose-600 hover:text-rose-700 border-rose-200"
              >
                Reject
              </button>
            </>
          )}
        </div>
      </div>

      {rejecting && (
        <div className="mt-3 p-3 rounded-lg bg-rose-50 dark:bg-rose-900/30 border border-rose-200 dark:border-rose-700 space-y-2">
          <input
            type="text"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Reason for rejection (e.g., PAN doesn't match name)"
            className="input text-sm"
          />
          <div className="flex gap-2">
            <button
              onClick={onRejectSubmit}
              disabled={rejectPending || !reason.trim()}
              className="btn btn-primary btn-sm"
            >
              {rejectPending ? <Spinner size="sm" /> : 'Confirm Reject'}
            </button>
            <button onClick={onRejectCancel} className="btn btn-ghost btn-sm">
              Cancel
            </button>
          </div>
        </div>
      )}

      {expanded && (
        <div className="mt-3 p-4 rounded-lg bg-blue-50/50 dark:bg-slate-700/40 border border-blue-100 dark:border-slate-600 grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
          <Detail label="Date of Birth" value={s.dateOfBirth || '—'} />
          <Detail label="Postal Code" value={s.postalCode || '—'} />
          <Detail label="Address" value={s.address || '—'} />
          <Detail label="City" value={s.city || '—'} />
          <Detail
            label="Submitted At"
            value={s.submittedAt ? new Date(s.submittedAt).toLocaleString('en-IN') : '—'}
          />
          <Detail
            label="Reviewed At"
            value={s.reviewedAt ? new Date(s.reviewedAt).toLocaleString('en-IN') : '—'}
          />
          {s.rejectionReason && (
            <div className="md:col-span-2">
              <Detail label="Rejection Reason" value={s.rejectionReason} />
            </div>
          )}
          {s.documents.length > 0 && (
            <div className="md:col-span-2">
              <div className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase mb-1">
                Documents
              </div>
              <ul className="space-y-1">
                {s.documents.map((d) => (
                  <li
                    key={d.id}
                    className="flex items-center justify-between px-2 py-1 bg-white dark:bg-slate-800 rounded border border-blue-100 dark:border-slate-600"
                  >
                    <span className="font-medium text-slate-700 dark:text-slate-300">
                      {d.docType}
                    </span>
                    <span className="font-mono text-slate-600 dark:text-slate-400">
                      {d.documentNumber}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </li>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <div className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase">
        {label}
      </div>
      <div className="text-slate-700 dark:text-slate-200 mt-0.5">{value}</div>
    </div>
  );
}