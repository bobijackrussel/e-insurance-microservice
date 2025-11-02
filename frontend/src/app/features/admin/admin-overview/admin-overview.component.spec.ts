import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ClaimService } from '../../../core/services/claim.service';
import { PaymentService } from '../../../core/services/payment.service';
import { PolicyService } from '../../../core/services/policy.service';
import { UserService } from '../../../core/services/user.service';
import { AdminOverviewComponent } from './admin-overview.component';

describe('AdminOverviewComponent', () => {
  let component: AdminOverviewComponent;
  let fixture: ComponentFixture<AdminOverviewComponent>;

  const userServiceMock = {
    getUserStatistics: jasmine.createSpy('getUserStatistics').and.returnValue(
      of({ totalUsers: 0, activeUsers: 0, inactiveUsers: 0, newUsersThisMonth: 0 })
    )
  };

  const policyServiceMock = {
    getPolicyStatistics: jasmine.createSpy('getPolicyStatistics').and.returnValue(
      of({ totalPolicies: 0, activePolicies: 0, pendingPolicies: 0, cancelledPolicies: 0, totalRevenue: 0 })
    )
  };

  const claimServiceMock = {
    getClaimStatistics: jasmine.createSpy('getClaimStatistics').and.returnValue(
      of({ totalClaims: 0, pendingClaims: 0, approvedClaims: 0, rejectedClaims: 0, totalClaimAmount: 0 })
    ),
    getAllClaims: jasmine.createSpy('getAllClaims').and.returnValue(
      of({ content: [], totalElements: 0, totalPages: 0, size: 5, number: 0 })
    )
  };

  const paymentServiceMock = {
    getTransactionStatistics: jasmine.createSpy('getTransactionStatistics').and.returnValue(
      of({ totalTransactions: 0, completedTransactions: 0, failedTransactions: 0, totalRevenue: 0 })
    )
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminOverviewComponent],
      imports: [CommonModule],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: PolicyService, useValue: policyServiceMock },
        { provide: ClaimService, useValue: claimServiceMock },
        { provide: PaymentService, useValue: paymentServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
