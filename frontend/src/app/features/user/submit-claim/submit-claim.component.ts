import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ClaimSubmitRequest } from '../../../core/models/claim.model';
import { CustomerPolicy } from '../../../core/models/policy.model';
import { ClaimService } from '../../../core/services/claim.service';
import { PolicyService } from '../../../core/services/policy.service';

type SubmitClaimFormGroup = {
  customerPolicyId: FormControl<string>;
  incidentDate: FormControl<string>;
  amount: FormControl<string>;
  description: FormControl<string>;
};

@Component({
  selector: 'app-submit-claim',
  standalone: false,
  templateUrl: './submit-claim.component.html',
  styleUrl: './submit-claim.component.scss'
})
export class SubmitClaimComponent implements OnInit {
  claimForm: FormGroup<SubmitClaimFormGroup>;
  loadingPolicies = false;
  submitting = false;
  policies: CustomerPolicy[] = [];
  policiesError: string | null = null;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private policyService: PolicyService,
    private claimService: ClaimService
  ) {
    this.claimForm = this.fb.nonNullable.group({
      customerPolicyId: ['', Validators.required],
      incidentDate: ['', Validators.required],
      amount: ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  ngOnInit(): void {
    this.loadPolicies();
  }

  get activePolicies(): CustomerPolicy[] {
    return this.policies.filter(policy => policy.status === 'ACTIVE');
  }

  get amountControl() {
    return this.claimForm.get('amount');
  }

  loadPolicies(): void {
    this.loadingPolicies = true;
    this.policiesError = null;

    this.policyService.getMyPolicies().subscribe({
      next: (policies) => {
        this.policies = policies;
        this.loadingPolicies = false;
      },
      error: (error) => {
        console.error('Error loading policies:', error);
        this.policiesError = 'We could not load your policies. Please try again later.';
        this.loadingPolicies = false;
      }
    });
  }

  onSubmit(): void {
    if (this.claimForm.invalid) {
      this.claimForm.markAllAsTouched();
      return;
    }

    const { amount, customerPolicyId, description, incidentDate } = this.claimForm.getRawValue();
    const parsedAmount = parseFloat(amount);

    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      this.amountControl?.setErrors({ invalidAmount: true });
      this.amountControl?.markAsTouched();
      return;
    }

    const request: ClaimSubmitRequest = {
      customerPolicyId,
      amount: parsedAmount,
      description: description.trim(),
      incidentDate
    };

    this.submitting = true;
    this.successMessage = null;
    this.errorMessage = null;

    this.claimService.submitClaim(request).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Claim submitted successfully.';
        this.submitting = false;
        this.claimForm.reset();
        this.claimForm.markAsPristine();
        this.claimForm.markAsUntouched();
      },
      error: (error) => {
        console.error('Error submitting claim:', error);
        this.errorMessage = 'Failed to submit claim. Please try again.';
        this.submitting = false;
      }
    });
  }

  clearForm(): void {
    this.claimForm.reset();
    this.claimForm.markAsPristine();
    this.claimForm.markAsUntouched();
    this.errorMessage = null;
  }
}
