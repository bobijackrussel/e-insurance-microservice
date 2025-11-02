import { Component, OnInit } from '@angular/core';
import { PolicyTemplate, PolicyType } from '../../../core/models/policy.model';
import { PolicyService } from '../../../core/services/policy.service';

@Component({
  selector: 'app-policy-marketplace',
  standalone: false,
  templateUrl: './policy-marketplace.component.html',
  styleUrl: './policy-marketplace.component.scss'
})
export class PolicyMarketplaceComponent implements OnInit {
  policies: PolicyTemplate[] = [];
  filteredPolicies: PolicyTemplate[] = [];
  selectedPolicy: PolicyTemplate | null = null;
  filterType: string = 'ALL';
  loading: boolean = false;

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.loading = true;
    this.policyService.getActivePolicyTemplates().subscribe({
      next: (policies) => {
        this.policies = policies;
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading policies:', error);
        this.loading = false;
      }
    });
  }

  setFilter(type: string): void {
    this.filterType = type;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.filterType === 'ALL') {
      this.filteredPolicies = this.policies;
    } else {
      this.filteredPolicies = this.policies.filter(p => p.type === this.filterType);
    }
  }

  handlePurchase(policy: PolicyTemplate): void {
    this.selectedPolicy = policy;
  }

  closeModal(): void {
    this.selectedPolicy = null;
    this.loadPolicies(); // Reload policies after purchase
  }
}
