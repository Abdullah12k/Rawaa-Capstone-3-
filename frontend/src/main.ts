import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/auth.interceptor';

// Register the router and JWT interceptor once for the whole Angular application.
bootstrapApplication(AppComponent, {providers: [provideRouter(routes), provideHttpClient(withInterceptors([authInterceptor]))]})
  .catch(error => console.error(error));
