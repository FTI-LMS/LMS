
import { Injectable } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { AuthenticationResult } from '@azure/msal-browser';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { loginRequest } from './auth.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private msalService: MsalService) {}

  login(): Observable<AuthenticationResult> {
    // Use popup authentication since redirects don't work in iframes
    return this.msalService.loginPopup(loginRequest);
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
      return this.msalService.acquireTokenSilent(accessTokenRequest);
    } else {
      throw new Error('No active account');
    }
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getActiveAccount() != null;
  }
}
