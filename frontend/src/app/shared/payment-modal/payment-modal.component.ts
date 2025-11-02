import { Component, Input, Output, EventEmitter } from '@angular/core';
import { PolicyTemplate } from '../../core/models/policy.model';
import { PaymentService } from '../../core/services/payment.service';

@Component({
  selector: 'app-payment-modal',
  standalone: false,
  templateUrl: './payment-modal.component.html',
  styleUrl: './payment-modal.component.scss'
})
export class PaymentModalComponent {
  @Input() policy!: PolicyTemplate;
  @Output() close = new EventEmitter<void>();

  processing = false;
  paymentSuccess = false;

  constructor(private paymentService: PaymentService) {}

  handlePayment(): void {
    this.processing = true;

    // Create Stripe checkout session
    this.paymentService.createCheckoutSession({
      policyTemplateId: this.policy.id
    }).subscribe({
      next: (response) => {
        // Redirect to Stripe checkout
        window.location.href = response.sessionUrl;
      },
      error: (error) => {
        console.error('Payment error:', error);
        alert('Payment failed. Please try again.');
        this.processing = false;
      }
    });
  }

  closeModal(): void {
    this.close.emit();
  }
}
