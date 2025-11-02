import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ClaimService } from '../../../core/services/claim.service';
import { ClaimManagementComponent } from './claim-management.component';

describe('ClaimManagementComponent', () => {
  let component: ClaimManagementComponent;
  let fixture: ComponentFixture<ClaimManagementComponent>;

  const claimServiceMock = {
    getAllClaims: jasmine.createSpy('getAllClaims').and.returnValue(
      of({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0 })
    ),
    reviewClaim: jasmine.createSpy('reviewClaim').and.returnValue(of({ success: true }))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ClaimManagementComponent],
      imports: [CommonModule],
      providers: [{ provide: ClaimService, useValue: claimServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(ClaimManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
