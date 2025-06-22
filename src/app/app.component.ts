import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { EventMessage, EventType, InteractionStatus } from '@azure/msal-browser';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'my-angular-project';
  isIframe = false;
  loginDisplay = false;
  private readonly _destroying$ = new Subject<void>();
  accessToken: string | null = null;
  userInfo: any = null;

  constructor(
    private broadcastService: MsalBroadcastService,
    private msalService: MsalService,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    // Check if running in browser (not SSR)
    if (isPlatformBrowser(this.platformId)) {
      this.isIframe = window !== window.parent && !window.opener;
      
      // Handle redirect response
      this.msalService.handleRedirectObservable().subscribe({
        next: (result) => {
          if (result) {
            console.log('Redirect login successful', result);
            this.setLoginDisplay();
            this.getAccessToken();
          }
        },
        error: (error) => console.error('Redirect login failed', error)
      });
      
      this.broadcastService.inProgress$
        .pipe(takeUntil(this._destroying$))
        .subscribe((status: InteractionStatus) => {
          if (status === InteractionStatus.None) {
            this.setLoginDisplay();
          }
        });
    }

    this.broadcastService.msalSubject$
      .pipe(takeUntil(this._destroying$))
      .subscribe((result: EventMessage) => {
        if (result.eventType === EventType.LOGIN_SUCCESS) {
          this.setLoginDisplay();
          this.getAccessToken();
        }
      });
  }

  setLoginDisplay() {
    this.loginDisplay = this.authService.isLoggedIn();
    if (this.loginDisplay) {
      this.userInfo = this.authService.getActiveAccount();
    }
  }

  login() {
    this.authService.login();
  }

  logout() {
    this.authService.logout();
    this.accessToken = null;
    this.userInfo = null;
  }

  getAccessToken() {
    this.authService.getAccessToken().subscribe({
      next: (result) => {
        this.accessToken = result.accessToken;
        console.log('Access token:', this.accessToken);
      },
      error: (error) => console.error('Failed to get access token', error)
    });
  }

  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }
}
