import { Component } from '@angular/core';

@Component({
  selector: 'app-user-dashboard',
  standalone: false,
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.scss'
})
export class UserDashboardComponent {
  readonly tabs = [
    { label: 'Browse Policies', route: 'marketplace' },
    { label: 'My Policies', route: 'my-policies' },
    { label: 'My Claims', route: 'my-claims' },
    { label: 'Submit Claim', route: 'submit-claim' }
  ];
}
