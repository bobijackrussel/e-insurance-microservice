import { Component, OnInit } from '@angular/core';
import { CustomerPolicy } from '../../../core/models/policy.model';
import { PolicyService } from '../../../core/services/policy.service';

@Component({
  selector: 'app-my-policies',
  standalone: false,
  templateUrl: './my-policies.component.html',
  styleUrl: './my-policies.component.scss'
})
export class MyPoliciesComponent implements OnInit {
  policies: CustomerPolicy[] = [];
  loading = false;
  showCancelDialog = false;
  selectedPolicyId: string | null = null;

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.loading = true;
    this.policyService.getMyPolicies().subscribe({
      next: (policies) => {
        this.policies = policies;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading policies:', error);
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      case 'EXPIRED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  downloadPDF(policy: CustomerPolicy): void {
    alert(`Downloading PDF for policy ${policy.policyNumber}\n\nIn a real application, this would generate and download a PDF document with policy details.`);
  }

  handleCancelPolicy(policyId: string): void {
    this.selectedPolicyId = policyId;
    this.showCancelDialog = true;
  }

  confirmCancelPolicy(): void {
    if (this.selectedPolicyId) {
      this.policyService.cancelPolicy(this.selectedPolicyId).subscribe({
        next: () => {
          alert('Policy cancelled successfully');
          this.showCancelDialog = false;
          this.selectedPolicyId = null;
          this.loadPolicies(); // Reload policies
        },
        error: (error) => {
          console.error('Error cancelling policy:', error);
          alert('Failed to cancel policy');
        }
      });
    }
  }

  closeCancelDialog(): void {
    this.showCancelDialog = false;
    this.selectedPolicyId = null;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
