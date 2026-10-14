import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export interface Session { token: string; role: 'USER' | 'OWNER'; id: number; fullName: string }

// Keeps the signed-in account for this browser tab and exposes it as an Angular signal.
// sessionStorage is convenient for the UI; authorization is always enforced by Spring Boot.
@Injectable({providedIn: 'root'})
export class AuthService {
  private http = inject(HttpClient);
  readonly session = signal<Session | null>(this.readSession());

  login(email: string, password: string) {
    return this.http.post<Session>('/api/v1/auth/login', {email, password}).pipe(tap(session => this.save(session)));
  }

  register(profile: Record<string, unknown>, password: string, role: 'user' | 'owner') {
    return this.http.post<Session>(`/api/v1/auth/register/${role}`, {profile, password}).pipe(tap(session => this.save(session)));
  }

  logout() { sessionStorage.removeItem('rawaa-session'); this.session.set(null); }

  activeSession(): Session | null {
    // JWT expiry is checked here so guards and the interceptor do not use a stale token.
    const session = this.session();
    if (!session) return null;
    try {
      const payload = JSON.parse(atob(session.token.split('.')[1]));
      if (payload.exp * 1000 > Date.now()) return session;
    } catch { /* Invalid stored token. */ }
    this.logout();
    return null;
  }

  private save(session: Session) { sessionStorage.setItem('rawaa-session', JSON.stringify(session)); this.session.set(session); }

  private readSession(): Session | null {
    try {
      const raw = sessionStorage.getItem('rawaa-session');
      if (!raw) return null;
      const session = JSON.parse(raw) as Session;
      const payload = JSON.parse(atob(session.token.split('.')[1]));
      if (payload.exp * 1000 <= Date.now()) return null;
      return session;
    } catch { return null; }
  }
}
