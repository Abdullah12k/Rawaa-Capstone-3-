import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({standalone: true, imports: [RouterLink], template: `
<main class="page dashboard"><p class="eyebrow">YOUR DASHBOARD</p><h1>Hello, {{ auth.session()?.fullName }}</h1>
<p class="muted">Signed in as {{ auth.session()?.role === 'OWNER' ? 'a property owner' : 'a renter' }}.</p>
<div class="dashboard-card"><h2>Find your next place</h2><p>Explore available apartments and view their buildings on the map.</p><a class="primary link-button" routerLink="/">Explore apartments</a></div>
</main>`})
// The protected landing page shown after login or registration.
export class DashboardComponent { auth = inject(AuthService); }
