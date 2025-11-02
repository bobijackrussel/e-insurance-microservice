import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserRoutingModule } from './user-routing.module';
import { PolicyMarketplaceComponent } from './policy-marketplace/policy-marketplace.component';
import { MyPoliciesComponent } from './my-policies/my-policies.component';
import { MyClaimsComponent } from './my-claims/my-claims.component';
import { SubmitClaimComponent } from './submit-claim/submit-claim.component';
import { UserDashboardComponent } from './user-dashboard/user-dashboard.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [
    PolicyMarketplaceComponent,
    MyPoliciesComponent,
    MyClaimsComponent,
    SubmitClaimComponent,
    UserDashboardComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    UserRoutingModule
  ]
})
export class UserModule { }
