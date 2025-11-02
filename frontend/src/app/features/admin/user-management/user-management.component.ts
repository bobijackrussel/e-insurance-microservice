import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { User, UserRole, UserRegistrationRequest } from '../../../core/models/user.model';
import { UserService } from '../../../core/services/user.service';

type CreateUserFormGroup = {
  keycloakId: FormControl<string>;
  email: FormControl<string>;
  firstName: FormControl<string>;
  lastName: FormControl<string>;
  phone: FormControl<string>;
};

@Component({
  selector: 'app-user-management',
  standalone: false,
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];

  loading = false;
  error: string | null = null;
  feedbackMessage: string | null = null;

  searchTerm = '';
  filterRole: 'ALL' | UserRole = 'ALL';

  showCreateModal = false;
  showDeactivateDialog = false;
  submitting = false;
  selectedUser: User | null = null;

  createForm: FormGroup<CreateUserFormGroup>;

  constructor(
    private fb: FormBuilder,
    private userService: UserService
  ) {
    this.createForm = this.fb.nonNullable.group({
      keycloakId: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: ['']
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;

    this.userService.getAllUsers(0, 50).subscribe({
      next: (response) => {
        this.users = response.content ?? [];
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.error = 'Unable to load users.';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredUsers = this.users.filter(user => {
      const matchesRole = this.filterRole === 'ALL' || user.role === this.filterRole;
      const matchesSearch = !term || [user.email, user.firstName, user.lastName].some(value => {
        if (!value) {
          return false;
        }
        return value.toLowerCase().includes(term);
      });
      return matchesRole && matchesSearch;
    });
  }

  onSearchChange(value: string): void {
    this.searchTerm = value;
    this.applyFilters();
  }

  setFilter(role: 'ALL' | UserRole): void {
    this.filterRole = role;
    this.applyFilters();
  }

  openCreateModal(): void {
    this.createForm.reset({
      keycloakId: '',
      email: '',
      firstName: '',
      lastName: '',
      phone: ''
    });
    this.showCreateModal = true;
    this.feedbackMessage = null;
  }

  openDeactivateDialog(user: User): void {
    this.selectedUser = user;
    this.showDeactivateDialog = true;
    this.feedbackMessage = null;
  }

  closeModals(): void {
    this.showCreateModal = false;
    this.showDeactivateDialog = false;
    this.selectedUser = null;
  }

  submitCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const form = this.createForm.getRawValue();
    const payload: UserRegistrationRequest = {
      keycloakId: form.keycloakId.trim(),
      email: form.email.trim(),
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      phone: form.phone.trim() || undefined
    };

    this.submitting = true;
    this.userService.registerUser(payload)
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: () => {
          this.feedbackMessage = 'User created successfully.';
          this.closeModals();
          this.loadUsers();
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.feedbackMessage = 'Failed to create user.';
        }
      });
  }

  confirmDeactivate(): void {
    if (!this.selectedUser) {
      return;
    }

    this.submitting = true;
    this.userService.deactivateUser(this.selectedUser.id)
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: () => {
          this.feedbackMessage = `User ${this.selectedUser?.email} deactivated.`;
          this.closeModals();
          this.loadUsers();
        },
        error: (error) => {
          console.error('Error deactivating user:', error);
          this.feedbackMessage = 'Failed to deactivate user.';
        }
      });
  }

  getStatusBadgeClass(user: User): string {
    return user.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800';
  }
}
