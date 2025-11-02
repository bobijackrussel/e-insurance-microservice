import { Component, OnInit } from '@angular/core';
import { Claim, ClaimStatistics } from '../../../core/models/claim.model';
import { PolicyStatistics } from '../../../core/models/policy.model';
import { TransactionStatistics } from '../../../core/models/payment.model';
import { UserStatistics } from '../../../core/models/user.model';
import { ClaimService } from '../../../core/services/claim.service';
import { PaymentService } from '../../../core/services/payment.service';
import { PolicyService } from '../../../core/services/policy.service';
import { UserService } from '../../../core/services/user.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-overview',
  standalone: false,
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.scss'
})
export class AdminOverviewComponent implements OnInit {
  statsLoading = false;
  statsError: string | null = null;
  claimsLoading = false;
  claimsError: string | null = null;

  userStats: UserStatistics | null = null;
  policyStats: PolicyStatistics | null = null;
  claimStats: ClaimStatistics | null = null;
  paymentStats: TransactionStatistics | null = null;

  recentClaims: Claim[] = [];

  constructor(
    private userService: UserService,
    private policyService: PolicyService,
    private claimService: ClaimService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
    this.loadRecentClaims();
  }

  loadStatistics(): void {
    this.statsLoading = true;
    this.statsError = null;

    forkJoin({
      users: this.userService.getUserStatistics(),
      policies: this.policyService.getPolicyStatistics(),
      claims: this.claimService.getClaimStatistics(),
      payments: this.paymentService.getTransactionStatistics()
    }).subscribe({
      next: ({ users, policies, claims, payments }) => {
        this.userStats = users;
        this.policyStats = policies;
        this.claimStats = claims;
        this.paymentStats = payments;
        this.statsLoading = false;
      },
      error: (error) => {
        console.error('Error loading statistics:', error);
        this.statsError = 'Unable to load dashboard statistics right now.';
        this.statsLoading = false;
      }
    });
  }

  loadRecentClaims(): void {
    this.claimsLoading = true;
    this.claimsError = null;

    this.claimService.getAllClaims(0, 5).subscribe({
      next: (response) => {
        this.recentClaims = response.content || [];
        this.claimsLoading = false;
      },
      error: (error) => {
        console.error('Error loading recent claims:', error);
        this.claimsError = 'Unable to load recent claims.';
        this.claimsLoading = false;
      }
    });
  }

  getStatusBadgeClass(status: Claim['status']): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'UNDER_REVIEW':
        return 'bg-orange-100 text-orange-800';
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PAID':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatCurrency(amount: number | undefined | null): string {
    if (amount === undefined || amount === null) {
      return '$0.00';
    }
    return `$${amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  formatDate(date: string | undefined): string {
    if (!date) {
      return 'N/A';
    }
    return new Date(date).toLocaleDateString();
  }
}
