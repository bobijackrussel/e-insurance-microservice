import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../shared/shared.module';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminOverviewComponent } from './admin-overview/admin-overview.component';
import { PolicyManagementComponent } from './policy-management/policy-management.component';
import { ClaimManagementComponent } from './claim-management/claim-management.component';
import { UserManagementComponent } from './user-management/user-management.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminOverviewComponent,
    PolicyManagementComponent,
    ClaimManagementComponent,
    UserManagementComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
