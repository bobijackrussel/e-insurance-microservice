import { CustomerPolicy } from './policy.model';

export type ClaimStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'PAID';

export interface Claim {
  id: string;
  userId: string;
  policyId: string;
  claimNumber: string;
  description: string;
  amount: number;
  status: ClaimStatus;
  submittedDate: string;
  reviewedDate?: string;
  reviewNotes?: string;
  incidentDate: string;
  policy: CustomerPolicy;
}

export interface ClaimSubmitRequest {
  customerPolicyId: string;
  amount: number;
  description: string;
  incidentDate: string;
}

export interface ClaimReviewRequest {
  status: 'APPROVED' | 'REJECTED';
  adminNotes: string;
}

export interface ClaimStatistics {
  totalClaims: number;
  pendingClaims: number;
  approvedClaims: number;
  rejectedClaims: number;
  totalClaimAmount: number;
}
