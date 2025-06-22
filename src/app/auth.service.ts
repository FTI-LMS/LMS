
import { Injectable } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { AuthenticationResult } from '@azure/msal-browser';
import { Observable, from } from 'rxjs';
import { loginRequest } from './auth.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private msalService: MsalService) {}

  login(): Observable<AuthenticationResult> {
    return from(this.msalService.loginPopup(loginRequest));
  }

  logout(): void {
    this.msalService.logout();
  }

  getActiveAccount() {
    return this.msalService.instance.getActiveAccount();
  }

  getAccessToken(): Observable<AuthenticationResult> {
    const account = this.getActiveAccount();
    if (account) {
      const accessTokenRequest = {
        scopes: ['User.Read'],
        account: account
      };
      return from(this.msalService.acquireTokenSilent(accessTokenRequest));
    }
    throw new Error('No active account');
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getActiveAccount() != null;
  }
}
