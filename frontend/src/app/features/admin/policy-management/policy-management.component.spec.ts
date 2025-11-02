import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { PolicyService } from '../../../core/services/policy.service';
import { PolicyManagementComponent } from './policy-management.component';

describe('PolicyManagementComponent', () => {
  let component: PolicyManagementComponent;
  let fixture: ComponentFixture<PolicyManagementComponent>;

  const policyServiceMock = {
    getAllPolicyTemplates: jasmine.createSpy('getAllPolicyTemplates').and.returnValue(
      of({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0 })
    ),
    createPolicyTemplate: jasmine.createSpy('createPolicyTemplate').and.returnValue(of({ success: true })),
    updatePolicyTemplate: jasmine.createSpy('updatePolicyTemplate').and.returnValue(of({ success: true })),
    deletePolicyTemplate: jasmine.createSpy('deletePolicyTemplate').and.returnValue(of({ success: true }))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PolicyManagementComponent],
      imports: [CommonModule, ReactiveFormsModule],
      providers: [
        { provide: PolicyService, useValue: policyServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PolicyManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
