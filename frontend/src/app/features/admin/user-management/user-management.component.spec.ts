import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { UserService } from '../../../core/services/user.service';
import { UserManagementComponent } from './user-management.component';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;

  const userServiceMock = {
    getAllUsers: jasmine.createSpy('getAllUsers').and.returnValue(
      of({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0 })
    ),
    registerUser: jasmine.createSpy('registerUser').and.returnValue(of({ success: true })),
    deactivateUser: jasmine.createSpy('deactivateUser').and.returnValue(of({ success: true }))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserManagementComponent],
      imports: [CommonModule, ReactiveFormsModule],
      providers: [{ provide: UserService, useValue: userServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
