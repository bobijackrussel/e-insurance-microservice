import { Component } from '@angular/core';

interface AdminTab {
  label: string;
  route: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent {
  readonly tabs: AdminTab[] = [
    { label: 'Overview', route: 'overview' },
    { label: 'User Management', route: 'users' },
    { label: 'Policy Templates', route: 'policies' },
    { label: 'Claims Review', route: 'claims' }
  ];
}
