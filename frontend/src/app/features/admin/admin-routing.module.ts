import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminOverviewComponent } from './admin-overview/admin-overview.component';
import { PolicyManagementComponent } from './policy-management/policy-management.component';
import { ClaimManagementComponent } from './claim-management/claim-management.component';
import { UserManagementComponent } from './user-management/user-management.component';

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      { path: 'overview', component: AdminOverviewComponent },
      { path: 'users', component: UserManagementComponent },
      { path: 'policies', component: PolicyManagementComponent },
      { path: 'claims', component: ClaimManagementComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
