import { DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { AuthService, Session } from './auth.service';

interface Apartment { id: number; apartmentNumber: string; buildingId: number }
interface Building { id: number; name: string }
interface Reservation {
  id: number; apartmentId: number; userId: number; status: string;
  requestedStartDate: string; rentalMonths: number;
}
interface Contract {
  id: number; reservationId: number; apartmentId: number; userId: number;
  contractNumber: string; contractStatus: string; monthlyRent: number;
  securityDeposit: number | null; startDate: string; endDate: string;
}

@Component({standalone: true, imports: [RouterLink, FormsModule, DecimalPipe], template: `
<main class="page dashboard">
  <p class="eyebrow">YOUR DASHBOARD</p><h1>Hello, {{ auth.session()?.fullName }}</h1>
  <p class="muted">{{ auth.session()?.role === 'OWNER' ? 'Manage rental requests and contracts.' : 'Track your rental requests and contracts.' }}</p>
  <div class="dashboard-actions"><a class="primary link-button" routerLink="/">Explore apartments</a><button class="small-button" type="button" (click)="load()">Refresh</button></div>
  @if (loading()) { <p class="empty">Loading your rentals…</p> }
  @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
  @if (message()) { <p class="reservation-success" role="status">{{ message() }}</p> }

  @if (!loading() && auth.session()?.role === 'OWNER') {
    <section class="dashboard-card"><h2>Rental requests</h2>
      @for (item of reservations(); track item.id) {
        <article class="rental-item">
          <div><h3>{{ apartmentName(item.apartmentId) }}</h3><p class="muted">Renter #{{ item.userId }} · Start {{ item.requestedStartDate }} · {{ item.rentalMonths }} months</p></div>
          <span class="status">{{ item.status }}</span>
          @if (item.status === 'PENDING') {
            <div class="rental-actions"><button class="primary" type="button" [disabled]="busy()" (click)="approve(item)">Approve request</button><button class="small-button" type="button" [disabled]="busy()" (click)="rejectReservation(item)">Reject</button></div>
          }
          @if (item.status === 'APPROVED' && !contractFor(item.id)) {
            <form class="contract-form" (ngSubmit)="createContract(item)">
              <label>Security deposit (SAR, optional)<input type="number" min="0.01" step="0.01" name="deposit-{{ item.id }}" [(ngModel)]="deposits[item.id]" placeholder="No deposit"></label>
              <button class="primary" type="submit" [disabled]="busy()">Create contract</button>
            </form>
          }
        </article>
      } @empty { <p class="muted">No rental requests yet.</p> }
    </section>
    <section class="dashboard-card"><h2>Contracts</h2>
      @for (item of contracts(); track item.id) {
        <article class="rental-item"><div><h3>{{ apartmentName(item.apartmentId) }}</h3><p class="muted">{{ item.contractNumber }} · Renter #{{ item.userId }}</p><p>{{ item.startDate }} to {{ item.endDate }} · {{ item.monthlyRent | number:'1.0-0' }} SAR/month</p></div><span class="status">{{ item.contractStatus }}</span></article>
      } @empty { <p class="muted">No contracts yet.</p> }
    </section>
  }

  @if (!loading() && auth.session()?.role === 'USER') {
    <section class="dashboard-card"><h2>Your requests</h2>
      @for (item of reservations(); track item.id) {
        <article class="rental-item"><div><h3>{{ apartmentName(item.apartmentId) }}</h3><p class="muted">Start {{ item.requestedStartDate }} · {{ item.rentalMonths }} months</p></div><span class="status">{{ item.status }}</span>
          @if (item.status === 'PENDING' || item.status === 'APPROVED') { <div class="rental-actions"><button class="small-button" type="button" [disabled]="busy()" (click)="cancelReservation(item)">Cancel request</button></div> }
        </article>
      } @empty { <p class="muted">You have not requested an apartment yet.</p> }
    </section>
    <section class="dashboard-card"><h2>Your contracts</h2>
      @for (item of contracts(); track item.id) {
        <article class="rental-item"><div><h3>{{ apartmentName(item.apartmentId) }}</h3><p class="muted">{{ item.contractNumber }}</p><p>{{ item.startDate }} to {{ item.endDate }} · {{ item.monthlyRent | number:'1.0-0' }} SAR/month</p><p>Security deposit: {{ item.securityDeposit ? (item.securityDeposit | number:'1.0-0') + ' SAR' : 'None' }}</p></div><span class="status">{{ item.contractStatus }}</span>
          @if (item.contractStatus === 'PENDING') {
            <div class="rental-actions"><button class="primary" type="button" [disabled]="busy() || today < item.startDate" (click)="acceptContract(item)">Accept contract</button><button class="small-button" type="button" [disabled]="busy()" (click)="rejectContract(item)">Reject</button>
              @if (today < item.startDate) { <span class="muted">Acceptance opens on {{ item.startDate }}.</span> }
            </div>
          }
        </article>
      } @empty { <p class="muted">No contracts yet. The owner creates one after approving a request.</p> }
    </section>
  }
</main>`})
export class DashboardComponent {
  private http = inject(HttpClient);
  auth = inject(AuthService);
  apartments = signal<Apartment[]>([]); buildings = signal<Building[]>([]);
  reservations = signal<Reservation[]>([]); contracts = signal<Contract[]>([]);
  loading = signal(true); busy = signal(false); error = signal(''); message = signal('');
  deposits: Record<number, number | null> = {};
  today = this.localDate(new Date());

  // The API returns only this account's records; backend authorization verifies the same scope.
  load() {
    const session = this.auth.activeSession();
    if (!session) return;
    this.loading.set(true); this.error.set('');
    const reservationUrl = session.role === 'OWNER'
      ? `/api/v1/reservation/get/owner/${session.id}` : `/api/v1/reservation/get/user/${session.id}`;
    const contractUrl = session.role === 'OWNER'
      ? `/api/v1/contract/owner/${session.id}` : `/api/v1/contract/user/${session.id}`;
    forkJoin({
      apartments: this.http.get<Apartment[]>('/api/v1/apartment/get'),
      buildings: this.http.get<Building[]>('/api/v1/building/get'),
      reservations: this.http.get<Reservation[]>(reservationUrl),
      contracts: this.http.get<Contract[]>(contractUrl)
    }).subscribe({next: data => {
      this.apartments.set(data.apartments); this.buildings.set(data.buildings);
      this.reservations.set(data.reservations); this.contracts.set(data.contracts);
      this.loading.set(false);
    }, error: (response: HttpErrorResponse) => {
      this.loading.set(false); this.error.set(this.errorMessage(response));
    }});
  }

  apartmentName(id: number) {
    const apartment = this.apartments().find(item => item.id === id);
    const building = this.buildings().find(item => item.id === apartment?.buildingId);
    return apartment ? `${building?.name ?? 'Apartment'} · ${apartment.apartmentNumber}` : `Apartment #${id}`;
  }
  contractFor(reservationId: number) { return this.contracts().find(item => item.reservationId === reservationId); }
  approve(item: Reservation) { this.act(this.http.put(`/api/v1/reservation/accept/${this.accountId('OWNER')}/${item.id}`, {}), 'Request approved. You can now create the contract.'); }
  rejectReservation(item: Reservation) { this.act(this.http.put(`/api/v1/reservation/reject/${this.accountId('OWNER')}/${item.id}`, {}), 'Request rejected.'); }
  createContract(item: Reservation) {
    const deposit = this.deposits[item.id];
    if (deposit != null && deposit <= 0) { this.error.set('Security deposit must be positive, or leave it empty.'); return; }
    this.act(this.http.post(`/api/v1/contract/add/${this.accountId('OWNER')}/${item.id}`, {securityDeposit: deposit ?? null}), 'Contract created. The renter can review it in their dashboard.');
  }
  cancelReservation(item: Reservation) { this.act(this.http.put(`/api/v1/reservation/end/${this.accountId('USER')}/${item.id}`, {}), 'Request cancelled.'); }
  acceptContract(item: Contract) {
    if (!window.confirm(`Accept contract ${item.contractNumber} for ${item.monthlyRent} SAR per month?`)) return;
    this.act(this.http.put(`/api/v1/contract/accept/${item.id}/${this.accountId('USER')}`, {}), 'Contract accepted. The apartment is now rented.');
  }
  rejectContract(item: Contract) { this.act(this.http.put(`/api/v1/contract/reject/${item.id}/${this.accountId('USER')}`, {}), 'Contract rejected.'); }

  private accountId(role: Session['role']) {
    const session = this.auth.activeSession();
    if (session?.role !== role) throw new Error('Wrong account type');
    return session.id;
  }
  private act(request: Observable<unknown>, success: string) {
    this.busy.set(true); this.error.set(''); this.message.set('');
    request.subscribe({next: () => { this.busy.set(false); this.message.set(success); this.load(); },
      error: (response: HttpErrorResponse) => { this.busy.set(false); this.error.set(this.errorMessage(response)); }});
  }
  private errorMessage(response: HttpErrorResponse) {
    let body = response.error;
    if (typeof body === 'string') { try { body = JSON.parse(body); } catch { body = null; } }
    return body?.message || body?.detail || (response.status === 0
      ? 'The service is unavailable right now. Please try again shortly.'
      : `We could not complete your request. Please try again. (Error ${response.status})`);
  }
  private localDate(date: Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}
