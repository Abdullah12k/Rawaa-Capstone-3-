import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';

@Component({selector: 'app-root', standalone: true, imports: [RouterOutlet, RouterLink], template: `
  <header class="topbar"><a routerLink="/" class="brand">Rawaa<span>.</span></a>
    <nav><a routerLink="/">Explore homes</a>
      @if (auth.session()) { <a routerLink="/dashboard">Dashboard</a><button (click)="auth.logout()">Log out</button> }
      @else { <a routerLink="/login" class="nav-button">Log in</a> }
    </nav>
  </header>
  <router-outlet />
`})
// The root component owns the shared navigation; the router fills in each page below it.
export class AppComponent { auth = inject(AuthService); }
