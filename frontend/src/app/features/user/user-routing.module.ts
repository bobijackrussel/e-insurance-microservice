import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserDashboardComponent } from './user-dashboard/user-dashboard.component';
import { PolicyMarketplaceComponent } from './policy-marketplace/policy-marketplace.component';
import { MyPoliciesComponent } from './my-policies/my-policies.component';
import { MyClaimsComponent } from './my-claims/my-claims.component';
import { SubmitClaimComponent } from './submit-claim/submit-claim.component';

const routes: Routes = [
  {
    path: '',
    component: UserDashboardComponent,
    children: [
      { path: '', redirectTo: 'marketplace', pathMatch: 'full' },
      { path: 'marketplace', component: PolicyMarketplaceComponent },
      { path: 'my-policies', component: MyPoliciesComponent },
      { path: 'my-claims', component: MyClaimsComponent },
      { path: 'submit-claim', component: SubmitClaimComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
