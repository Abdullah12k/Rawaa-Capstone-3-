import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { MapComponent } from './map.component';
import { LoginComponent } from './login.component';
import { DashboardComponent } from './dashboard.component';

const authGuard = () => inject(AuthService).activeSession() ? true : inject(Router).createUrlTree(['/login']);

export const routes: Routes = [
  {path: '', component: MapComponent},
  {path: 'login', component: LoginComponent},
  {path: 'dashboard', component: DashboardComponent, canActivate: [authGuard]},
  {path: '**', redirectTo: ''}
];
