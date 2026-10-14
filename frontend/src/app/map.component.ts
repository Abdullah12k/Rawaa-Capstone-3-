import { AfterViewInit, Component, OnDestroy, effect, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';
import * as L from 'leaflet';

interface Apartment { id: number; buildingId: number; apartmentNumber: string; district: string; monthlyRent: number; bedrooms: number; bathrooms: number; area: number; status: string; furnished: boolean }
interface Building { id: number; name: string; city: string; district: string; street: string; latitude: number; longitude: number }

@Component({standalone: true, imports: [FormsModule, DecimalPipe], template: `
<main class="explore">
  <section class="intro"><p class="eyebrow">FIND YOUR PLACE</p><h1>Room to live beautifully.</h1><p>Explore rental homes across the city, right on the map.</p></section>
  <div class="workspace">
    <aside class="list-panel">
      <div class="panel-heading"><div><h2>Explore apartments</h2><p>{{ visible().length }} homes found</p></div><button class="small-button" (click)="reload()">Refresh</button></div>
      <div class="filters"><label>District<input [(ngModel)]="district" (ngModelChange)="filter()" placeholder="Any district"></label><label>Max rent (SAR)<input type="number" min="0" [(ngModel)]="maxRent" (ngModelChange)="filter()" placeholder="Any price"></label></div>
      @if (selectedApartment(); as apartment) {
        <section class="reservation-panel">
          <h3>Apartment {{ apartment.apartmentNumber }}</h3>
          <p>{{ apartment.monthlyRent | number:'1.0-0' }} SAR per month · {{ apartment.bedrooms }} bedrooms</p>
          @if (auth.session()?.role === 'USER') {
            <form (ngSubmit)="reserve(apartment)">
              <label>Move-in date<input type="date" name="startDate" [min]="today" [(ngModel)]="startDate" required></label>
              <label>Rental length (months)<input type="number" name="rentalMonths" min="1" max="120" [(ngModel)]="rentalMonths" required></label>
              <button class="primary" type="submit" [disabled]="reserving() || reservedApartmentIds.has(apartment.id)">{{ reserving() ? 'Sending request…' : reservedApartmentIds.has(apartment.id) ? 'Request sent' : 'Request to rent' }}</button>
            </form>
            <p class="reservation-note">The owner will review your request. You are not charged now.</p>
          } @else if (!auth.session()) {
            <p class="reservation-note">Log in as a renter to request this apartment.</p>
          }
          @if (reservationMessage()) { <p class="reservation-success" role="status">{{ reservationMessage() }}</p> }
          @if (reservationError()) { <p class="error" role="alert">{{ reservationError() }}</p> }
        </section>
      }
      @if (error()) { <p class="error">{{ error() }}</p> }
      @if (loading()) { <p class="empty">Loading homes…</p> }
      @for (apartment of visible(); track apartment.id) {
        <button class="listing" [class.selected]="selectedId() === apartment.id" (click)="select(apartment)">
          <span class="listing-icon">⌂</span><span class="listing-content"><span class="listing-top"><strong>{{ buildingFor(apartment)?.name || 'Apartment ' + apartment.apartmentNumber }}</strong><b>{{ apartment.monthlyRent | number:'1.0-0' }} SAR</b></span>
          <span class="muted">{{ apartment.district }} · {{ buildingFor(apartment)?.city || 'Saudi Arabia' }}</span>
          <span class="features">{{ apartment.bedrooms }} beds · {{ apartment.bathrooms }} baths · {{ apartment.area }} m² @if (apartment.furnished) { · Furnished }</span></span>
        </button>
      } @empty { @if (!loading()) { <p class="empty">No apartments match your filters yet.</p> } }
    </aside>
    <section class="map-panel" aria-label="Apartment locations"><div id="map"></div>
      @if (auth.session()) {
        <div class="location-controls"><button type="button" class="locate-button" (click)="showMyLocation()" [disabled]="locating()">{{ locating() ? 'Finding location…' : '◎ My location' }}</button>
          @if (locationStatus()) { <span class="location-status" role="status">{{ locationStatus() }}</span> }
        </div>
      }
      <span class="map-credit">Map data © OpenStreetMap contributors</span>
    </section>
  </div>
</main>`})
export class MapComponent implements AfterViewInit, OnDestroy {
  private http = inject(HttpClient); auth = inject(AuthService);
  private map?: L.Map; private markers = L.layerGroup();
  private userMarker?: L.CircleMarker; private accuracyCircle?: L.Circle;
  private userLocation?: L.LatLngExpression;
  apartments: Apartment[] = []; buildings = new Map<number, Building>();
  visible = signal<Apartment[]>([]); selectedId = signal<number | null>(null);
  loading = signal(true); error = signal(''); district = ''; maxRent: number | null = null;
  locating = signal(false); locationStatus = signal('');
  today = this.localDate(new Date()); startDate = this.today; rentalMonths = 12;
  reserving = signal(false); reservationMessage = signal(''); reservationError = signal('');
  reservedApartmentIds = new Set<number>();

  constructor() {
    effect(() => {
      if (!this.auth.session()) {
        this.userMarker?.remove(); this.accuracyCircle?.remove();
        this.userMarker = undefined; this.accuracyCircle = undefined; this.userLocation = undefined;
      }
    });
  }

  ngAfterViewInit() {
    this.map = L.map('map', {zoomControl: false}).setView([24.7136, 46.6753], 11);
    L.control.zoom({position: 'bottomright'}).addTo(this.map);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19, attribution: '© OpenStreetMap contributors'}).addTo(this.map);
    this.markers.addTo(this.map); this.reload();
    if (this.auth.activeSession()) this.showMyLocation();
  }
  ngOnDestroy() { this.map?.remove(); this.map = undefined; }
  showMyLocation() {
    if (!this.auth.activeSession()) { this.locationStatus.set('Log in to use your location.'); return; }
    if (!navigator.geolocation) { this.locationStatus.set('Location is unavailable in this browser.'); return; }
    this.locating.set(true); this.locationStatus.set('');
    navigator.geolocation.getCurrentPosition(position => {
      this.locating.set(false);
      if (!this.map) return;
      const point: L.LatLngExpression = [position.coords.latitude, position.coords.longitude];
      this.userLocation = point;
      this.userMarker?.remove(); this.accuracyCircle?.remove();
      const accuracy = Math.max(1, position.coords.accuracy);
      this.accuracyCircle = L.circle(point, {radius: accuracy, color: '#2462d1', weight: 1, fillColor: '#4983ed', fillOpacity: 0.1}).addTo(this.map);
      this.userMarker = L.circleMarker(point, {radius: 9, color: '#fff', weight: 3, fillColor: '#2462d1', fillOpacity: 1})
        .bindTooltip('Your location').addTo(this.map);
      this.map.flyTo(point, Math.max(this.map.getZoom(), 14));
      this.locationStatus.set(`Location accuracy: about ${Math.round(accuracy)} m`);
    }, error => {
      this.locating.set(false);
      this.locationStatus.set(error.code === 1 ? 'Location permission was denied.' : 'Could not determine your location. Try again.');
    }, {enableHighAccuracy: true, timeout: 12000, maximumAge: 60000});
  }
  reload() {
    this.loading.set(true); this.error.set('');
    this.http.get<Building[]>('/api/v1/building/get').subscribe({next: buildings => {
      this.buildings = new Map(buildings.map(b => [b.id, b]));
      this.http.get<Apartment[]>('/api/v1/apartment/get').subscribe({next: apartments => {
        this.apartments = apartments.filter(a => a.status === 'AVAILABLE'); this.loading.set(false); this.filter();
      }, error: () => { this.loading.set(false); this.error.set('Could not load apartments. Check that the API is running.'); }});
    }, error: () => { this.loading.set(false); this.error.set('Could not load buildings. Check that the API is running.'); }});
  }
  filter() {
    const district = this.district.trim().toLowerCase();
    const result = this.apartments.filter(a => (!district || a.district?.toLowerCase().includes(district)) && (!this.maxRent || a.monthlyRent <= this.maxRent));
    this.visible.set(result); this.drawMarkers(result);
  }
  buildingFor(apartment: Apartment) { return this.buildings.get(apartment.buildingId); }
  select(apartment: Apartment) {
    this.selectedId.set(apartment.id);
    this.reservationMessage.set(''); this.reservationError.set('');
    const building = this.buildingFor(apartment);
    if (building && Number.isFinite(building.latitude) && Number.isFinite(building.longitude)) this.map?.flyTo([building.latitude, building.longitude], 15);
  }
  selectedApartment() { return this.apartments.find(a => a.id === this.selectedId()) ?? null; }
  reserve(apartment: Apartment) {
    const session = this.auth.activeSession();
    if (session?.role !== 'USER') { this.reservationError.set('Log in as a renter to request this apartment.'); return; }
    if (!this.startDate || this.startDate < this.today || !Number.isInteger(this.rentalMonths) || this.rentalMonths < 1) {
      this.reservationError.set('Choose a date from today onward and a rental length of at least one month.'); return;
    }
    this.reserving.set(true); this.reservationError.set(''); this.reservationMessage.set('');
    this.http.post(`/api/v1/reservation/add/${session.id}/${apartment.id}`, {
      requestedStartDate: this.startDate, rentalMonths: this.rentalMonths
    }).subscribe({next: () => {
      this.reserving.set(false); this.reservedApartmentIds.add(apartment.id);
      this.reservationMessage.set('Request sent. It is pending owner approval.');
    }, error: (response: HttpErrorResponse) => {
      this.reserving.set(false);
      const body = typeof response.error === 'string' ? this.parseError(response.error) : response.error;
      this.reservationError.set(body?.message || body?.detail || (response.status === 0
        ? 'Cannot connect to the API. Check that the server is running.'
        : `Could not send the request (HTTP ${response.status}).`));
    }});
  }
  private parseError(value: string): {message?: string; detail?: string} | null {
    try { return JSON.parse(value); } catch { return null; }
  }
  private localDate(date: Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
  private drawMarkers(apartments: Apartment[]) {
    this.markers.clearLayers(); const bounds: L.LatLngExpression[] = [];
    for (const apartment of apartments) {
      const building = this.buildingFor(apartment);
      if (!building || !Number.isFinite(building.latitude) || !Number.isFinite(building.longitude)) continue;
      const point: L.LatLngExpression = [building.latitude, building.longitude]; bounds.push(point);
      L.circleMarker(point, {radius: 10, color: '#fff', weight: 3, fillColor: '#197e72', fillOpacity: 1})
        .bindTooltip(`${apartment.monthlyRent.toLocaleString()} SAR · ${apartment.bedrooms} beds`)
        .on('click', () => this.select(apartment)).addTo(this.markers);
    }
    if (bounds.length && this.map && !this.userLocation) this.map.fitBounds(L.latLngBounds(bounds).pad(0.2), {maxZoom: 13});
  }
}
