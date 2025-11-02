export type PolicyType = 'LIFE' | 'TRAVEL' | 'PROPERTY' | 'HEALTH';
export type PolicyStatus = 'ACTIVE' | 'PENDING' | 'CANCELLED' | 'EXPIRED';

export interface PolicyTemplate {
  id: string;
  name: string;
  type: PolicyType;
  description: string;
  coverage: string;
  price: number;
  duration: string;
  active: boolean;
}

export interface CustomerPolicy {
  id: string;
  userId: string;
  templateId: string;
  policyNumber: string;
  status: PolicyStatus;
  startDate: string;
  endDate: string;
  premium: number;
  template: PolicyTemplate;
}

export interface PolicyPurchaseRequest {
  policyTemplateId: string;
}

export interface PolicyStatistics {
  totalPolicies: number;
  activePolicies: number;
  pendingPolicies: number;
  cancelledPolicies: number;
  totalRevenue: number;
}
