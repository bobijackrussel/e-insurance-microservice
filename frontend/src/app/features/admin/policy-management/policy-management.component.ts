import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { PolicyTemplate, PolicyType } from '../../../core/models/policy.model';
import { PolicyService } from '../../../core/services/policy.service';

type PolicyFormGroup = {
  name: FormControl<string>;
  type: FormControl<PolicyType>;
  description: FormControl<string>;
  coverage: FormControl<string>;
  price: FormControl<string>;
  duration: FormControl<string>;
};

@Component({
  selector: 'app-policy-management',
  standalone: false,
  templateUrl: './policy-management.component.html',
  styleUrl: './policy-management.component.scss'
})
export class PolicyManagementComponent implements OnInit {
  policies: PolicyTemplate[] = [];
  filteredPolicies: PolicyTemplate[] = [];
  loading = false;
  error: string | null = null;

  filterType: 'ALL' | PolicyType = 'ALL';

  showCreateModal = false;
  showEditModal = false;
  showDeleteDialog = false;

  selectedPolicy: PolicyTemplate | null = null;
  submitting = false;
  feedbackMessage: string | null = null;

  policyForm: FormGroup<PolicyFormGroup>;

  readonly policyTypes: Array<{ label: string; value: 'ALL' | PolicyType }> = [
    { label: 'All', value: 'ALL' },
    { label: 'Life', value: 'LIFE' },
    { label: 'Travel', value: 'TRAVEL' },
    { label: 'Property', value: 'PROPERTY' },
    { label: 'Health', value: 'HEALTH' }
  ];

  constructor(
    private fb: FormBuilder,
    private policyService: PolicyService
  ) {
    this.policyForm = this.fb.nonNullable.group({
      name: this.fb.nonNullable.control('', Validators.required),
      type: this.fb.nonNullable.control<PolicyType>('LIFE', Validators.required),
      description: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(10)]),
      coverage: this.fb.nonNullable.control('', Validators.required),
      price: this.fb.nonNullable.control('', Validators.required),
      duration: this.fb.nonNullable.control('', Validators.required)
    });
  }

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.loading = true;
    this.error = null;

    this.policyService.getAllPolicyTemplates(0, 100).subscribe({
      next: (response) => {
        this.policies = response.content ?? [];
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading policies:', error);
        this.error = 'Unable to load policy templates.';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.filterType === 'ALL') {
      this.filteredPolicies = [...this.policies];
    } else {
      this.filteredPolicies = this.policies.filter(policy => policy.type === this.filterType);
    }
  }

  setFilter(type: 'ALL' | PolicyType): void {
    this.filterType = type;
    this.applyFilter();
  }

  openCreateModal(): void {
    this.policyForm.reset({
      name: '',
      type: 'LIFE' as PolicyType,
      description: '',
      coverage: '',
      price: '',
      duration: ''
    });
    this.showCreateModal = true;
    this.feedbackMessage = null;
  }

  openEditModal(policy: PolicyTemplate): void {
    this.selectedPolicy = policy;
    this.policyForm.setValue({
      name: policy.name,
      type: policy.type,
      description: policy.description,
      coverage: policy.coverage,
      price: policy.price.toString(),
      duration: policy.duration
    });
    this.showEditModal = true;
    this.feedbackMessage = null;
  }

  openDeleteDialog(policy: PolicyTemplate): void {
    this.selectedPolicy = policy;
    this.showDeleteDialog = true;
  }

  closeModals(): void {
    this.showCreateModal = false;
    this.showEditModal = false;
    this.showDeleteDialog = false;
    this.selectedPolicy = null;
    this.policyForm.reset({
      name: '',
      type: 'LIFE' as PolicyType,
      description: '',
      coverage: '',
      price: '',
      duration: ''
    });
  }

  toggleActive(policy: PolicyTemplate): void {
    const updated = { ...policy, active: !policy.active };
    this.policyService.updatePolicyTemplate(policy.id, { active: updated.active }).subscribe({
      next: () => {
        policy.active = updated.active;
        this.feedbackMessage = `Policy template ${updated.active ? 'activated' : 'deactivated'} successfully.`;
      },
      error: (error) => {
        console.error('Error updating policy:', error);
        this.feedbackMessage = 'Failed to update policy template status.';
      }
    });
  }

  submitCreate(): void {
    if (this.policyForm.invalid) {
      this.policyForm.markAllAsTouched();
      return;
    }

    const form = this.policyForm.getRawValue();
    const price = parseFloat(form.price);
    if (isNaN(price) || price <= 0) {
      this.policyForm.get('price')?.setErrors({ invalidPrice: true });
      return;
    }

    const payload: Partial<PolicyTemplate> = {
      name: form.name,
      type: form.type,
      description: form.description,
      coverage: form.coverage,
      price,
      duration: form.duration,
      active: true
    };

    this.submitting = true;
    this.policyService.createPolicyTemplate(payload)
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: () => {
          this.feedbackMessage = 'Policy template created successfully.';
          this.closeModals();
          this.loadPolicies();
        },
        error: (error) => {
          console.error('Error creating policy:', error);
          this.feedbackMessage = 'Failed to create policy template.';
        }
      });
  }

  submitEdit(): void {
    if (!this.selectedPolicy) {
      return;
    }

    if (this.policyForm.invalid) {
      this.policyForm.markAllAsTouched();
      return;
    }

    const form = this.policyForm.getRawValue();
    const price = parseFloat(form.price);
    if (isNaN(price) || price <= 0) {
      this.policyForm.get('price')?.setErrors({ invalidPrice: true });
      return;
    }

    const payload: Partial<PolicyTemplate> = {
      name: form.name,
      type: form.type,
      description: form.description,
      coverage: form.coverage,
      price,
      duration: form.duration
    };

    this.submitting = true;
    this.policyService.updatePolicyTemplate(this.selectedPolicy.id, payload)
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: () => {
          this.feedbackMessage = 'Policy template updated successfully.';
          this.closeModals();
          this.loadPolicies();
        },
        error: (error) => {
          console.error('Error updating policy:', error);
          this.feedbackMessage = 'Failed to update policy template.';
        }
      });
  }

  confirmDelete(): void {
    if (!this.selectedPolicy) {
      return;
    }

    this.submitting = true;
    this.policyService.deletePolicyTemplate(this.selectedPolicy.id)
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: () => {
          this.feedbackMessage = 'Policy template deleted successfully.';
          this.closeModals();
          this.loadPolicies();
        },
        error: (error) => {
          console.error('Error deleting policy:', error);
          this.feedbackMessage = 'Failed to delete policy template.';
        }
      });
  }

  getStatusBadgeClass(policy: PolicyTemplate): string {
    return policy.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800';
  }
}
