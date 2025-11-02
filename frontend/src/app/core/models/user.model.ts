export type UserRole = 'USER' | 'ADMIN';

export interface User {
  id: string;
  keycloakId?: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  dateOfBirth?: string;
  role: UserRole;
  active: boolean;
  registeredDate: string;
}

export interface UserRegistrationRequest {
  keycloakId: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  dateOfBirth?: string;
}

export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  newUsersThisMonth: number;
}
