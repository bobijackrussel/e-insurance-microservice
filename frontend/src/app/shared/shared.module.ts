import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { PaymentModalComponent } from './payment-modal/payment-modal.component';

@NgModule({
  declarations: [
    PaymentModalComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  exports: [
    CommonModule,
    ReactiveFormsModule,
    PaymentModalComponent
  ]
})
export class SharedModule { }
