export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface Transaction {
  id: string;
  userId: string;
  policyId?: string;
  amount: number;
  status: PaymentStatus;
  paymentMethod: string;
  stripeSessionId?: string;
  transactionDate: string;
}

export interface CheckoutSessionRequest {
  policyTemplateId: string;
}

export interface CheckoutSessionResponse {
  sessionId: string;
  sessionUrl: string;
}

export interface TransactionStatistics {
  totalTransactions: number;
  completedTransactions: number;
  failedTransactions: number;
  totalRevenue: number;
}
