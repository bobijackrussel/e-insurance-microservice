import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { ClaimService } from '../../../core/services/claim.service';
import { PolicyService } from '../../../core/services/policy.service';
import { SubmitClaimComponent } from './submit-claim.component';

describe('SubmitClaimComponent', () => {
  let component: SubmitClaimComponent;
  let fixture: ComponentFixture<SubmitClaimComponent>;

  const policyServiceMock = {
    getMyPolicies: jasmine.createSpy('getMyPolicies').and.returnValue(of([]))
  };

  const claimServiceMock = {
    submitClaim: jasmine.createSpy('submitClaim').and.returnValue(of({ success: true, message: 'ok' }))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SubmitClaimComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: PolicyService, useValue: policyServiceMock },
        { provide: ClaimService, useValue: claimServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SubmitClaimComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
