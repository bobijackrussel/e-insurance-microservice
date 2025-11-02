import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PolicyMarketplaceComponent } from './policy-marketplace.component';

describe('PolicyMarketplaceComponent', () => {
  let component: PolicyMarketplaceComponent;
  let fixture: ComponentFixture<PolicyMarketplaceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PolicyMarketplaceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PolicyMarketplaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
