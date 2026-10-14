import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';

@Component({standalone: true, imports: [FormsModule], template: `
<main class="auth-shell"><section class="auth-card">
  <p class="eyebrow">WELCOME TO RAWAA</p><h1>{{ registerMode ? 'Create your account' : 'Welcome back' }}</h1>
  <p class="muted">{{ registerMode ? 'Start finding your next place.' : 'Sign in to manage your rentals.' }}</p>
  <form (ngSubmit)="submit()">
    @if (registerMode) {
      <label>Account type<select [(ngModel)]="role" name="role"><option value="user">Renter</option><option value="owner">Property owner</option></select></label>
      <label>Full name<input [(ngModel)]="fullName" name="fullName" required></label>
      <label>Phone number<input [(ngModel)]="phoneNumber" name="phoneNumber" required></label>
      @if (role === 'user') {
        <label>Gender<select [(ngModel)]="gender" name="gender"><option value="MALE">Male</option><option value="FEMALE">Female</option></select></label>
        <label>Children count<input type="number" min="0" [(ngModel)]="childrenCount" name="childrenCount" required></label>
      } @else {
        <label>Commercial registration number<input [(ngModel)]="commercialRegistrationNumber" name="commercialRegistrationNumber" pattern="[0-9]{10}" required></label>
      }
    }
    <label>Email<input type="email" [(ngModel)]="email" name="email" required></label>
    <label>Password<input type="password" [(ngModel)]="password" name="password" required minlength="8"></label>
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
    <button type="submit" class="primary" [disabled]="busy()">{{ busy() ? 'Please wait…' : registerMode ? 'Create account' : 'Log in' }}</button>
  </form>
  <p class="switch">{{ registerMode ? 'Already have an account?' : 'New to Rawaa?' }}
    <button type="button" (click)="registerMode = !registerMode; error.set('')">{{ registerMode ? 'Log in' : 'Create account' }}</button>
  </p>
</section></main>
`})
export class LoginComponent {
  private auth = inject(AuthService); private router = inject(Router);
  registerMode = false; role: 'user' | 'owner' = 'user';
  fullName = ''; phoneNumber = ''; gender = 'MALE'; childrenCount = 0; commercialRegistrationNumber = '';
  email = ''; password = ''; error = signal(''); busy = signal(false);
  // Registration sends role-specific profile fields; login only sends email and password.
  submit() {
    this.error.set(''); this.busy.set(true);
    const profile = this.role === 'user'
      ? {fullName: this.fullName, email: this.email, phoneNumber: this.phoneNumber, gender: this.gender, childrenCount: this.childrenCount}
      : {fullName: this.fullName, email: this.email, phoneNumber: this.phoneNumber, commercialRegistrationNumber: this.commercialRegistrationNumber};
    const request = this.registerMode ? this.auth.register(profile, this.password, this.role) : this.auth.login(this.email, this.password);
    request.subscribe({next: () => this.router.navigateByUrl('/dashboard'), error: (response: HttpErrorResponse) => {
      this.busy.set(false);
      // Prefer the backend's validation message, then provide a useful fallback by status.
      const body = typeof response.error === 'string' ? this.parseError(response.error) : response.error;
      const serverMessage = typeof body?.message === 'string' && body.message.trim() ? body.message.trim()
        : typeof body?.detail === 'string' && body.detail.trim() ? body.detail.trim() : '';
      if (response.status === 0) {
        this.error.set('The service is unavailable right now. Please try again shortly.');
      } else if (!this.registerMode && response.status === 401) {
        this.error.set('Email or password is incorrect. Please try again.');
      } else if (response.status === 400) {
        this.error.set(serverMessage || 'Some details are invalid. Check each required field, email format, password length, and registration number.');
      } else if (response.status === 409) {
        this.error.set(serverMessage || 'An account already uses this email, phone number, or commercial registration number.');
      } else if (response.status === 403) {
        this.error.set('Your request was blocked. Refresh the page and try again.');
      } else if (response.status === 404) {
        this.error.set('The account service is temporarily unavailable. Please try again later.');
      } else if (response.status === 429) {
        this.error.set('Too many attempts. Please wait a moment and try again.');
      } else if (response.status >= 500) {
        this.error.set('We could not complete your request right now. Please try again later.');
      } else {
        this.error.set(serverMessage || `We could not complete your request. Please try again. (Error ${response.status})`);
      }
    }});
  }

  private parseError(text: string): {message?: string; detail?: string} | null {
    try { return JSON.parse(text); } catch { return null; }
  }
}
