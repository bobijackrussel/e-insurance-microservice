import { Component, OnInit } from '@angular/core';
import { Claim, ClaimStatus } from '../../../core/models/claim.model';
import { ClaimService } from '../../../core/services/claim.service';

type FilterOption = 'ALL' | ClaimStatus;

@Component({
  selector: 'app-claim-management',
  standalone: false,
  templateUrl: './claim-management.component.html',
  styleUrl: './claim-management.component.scss'
})
export class ClaimManagementComponent implements OnInit {
  claims: Claim[] = [];
  filteredClaims: Claim[] = [];
  loading = false;
  error: string | null = null;
  feedbackMessage: string | null = null;

  filterStatus: FilterOption = 'ALL';
  reviewNotes = '';
  selectedClaimId: string | null = null;
  submittingReview = false;

  readonly statusFilters: Array<{ label: string; value: FilterOption }> = [
    { label: 'All Claims', value: 'ALL' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Under Review', value: 'UNDER_REVIEW' },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'Rejected', value: 'REJECTED' },
    { label: 'Paid', value: 'PAID' }
  ];

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.loading = true;
    this.error = null;

    this.claimService.getAllClaims(0, 50).subscribe({
      next: (response) => {
        this.claims = response.content ?? [];
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading claims:', error);
        this.error = 'Unable to load claims.';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.filterStatus === 'ALL') {
      this.filteredClaims = [...this.claims];
    } else {
      this.filteredClaims = this.claims.filter(claim => claim.status === this.filterStatus);
    }
  }

  setFilter(status: FilterOption): void {
    this.filterStatus = status;
    this.applyFilter();
  }

  startReview(claimId: string): void {
    this.selectedClaimId = claimId;
    this.reviewNotes = '';
    this.feedbackMessage = null;
  }

  cancelReview(): void {
    this.selectedClaimId = null;
    this.reviewNotes = '';
  }

  submitReview(targetStatus: 'APPROVED' | 'REJECTED', claim: Claim): void {
    if (!this.reviewNotes.trim()) {
      this.feedbackMessage = targetStatus === 'APPROVED'
        ? 'Please add review notes before approving the claim.'
        : 'Please explain why the claim is being rejected.';
      return;
    }

    this.submittingReview = true;
    this.claimService.reviewClaim(claim.id, {
      status: targetStatus,
      adminNotes: this.reviewNotes.trim()
    }).subscribe({
      next: () => {
        this.feedbackMessage = `Claim ${claim.claimNumber} ${targetStatus === 'APPROVED' ? 'approved' : 'rejected'} successfully.`;
        this.selectedClaimId = null;
        this.reviewNotes = '';
        this.submittingReview = false;
        this.loadClaims();
      },
      error: (error) => {
        console.error('Error reviewing claim:', error);
        this.feedbackMessage = 'Failed to update claim. Please try again.';
        this.submittingReview = false;
      }
    });
  }

  getStatusBadgeClass(status: ClaimStatus): string {
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

  formatCurrency(amount: number): string {
    return `$${amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  formatDate(date: string | undefined): string {
    if (!date) {
      return 'N/A';
    }
    return new Date(date).toLocaleDateString();
  }
}
