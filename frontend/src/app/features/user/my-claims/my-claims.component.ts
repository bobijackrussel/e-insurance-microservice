import { Component, OnInit } from '@angular/core';
import { Claim } from '../../../core/models/claim.model';
import { ClaimService } from '../../../core/services/claim.service';

@Component({
  selector: 'app-my-claims',
  standalone: false,
  templateUrl: './my-claims.component.html',
  styleUrl: './my-claims.component.scss'
})
export class MyClaimsComponent implements OnInit {
  claims: Claim[] = [];
  loading = false;

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.loading = true;
    this.claimService.getMyClaims().subscribe({
      next: (claims) => {
        this.claims = claims;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading claims:', error);
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'PAID':
        return 'bg-blue-100 text-blue-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'UNDER_REVIEW':
        return 'bg-orange-100 text-orange-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  formatStatus(status: string): string {
    return status.replace('_', ' ');
  }
}
