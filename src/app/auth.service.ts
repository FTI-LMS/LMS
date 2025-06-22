
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
    // Ensure MSAL is initialized before attempting login
    return from(
      this.msalService.instance.initialize().then(() => 
        this.msalService.loginPopup(loginRequest)
      )
    ).pipe(
      switchMap((result) => from(result))
    );
  }

  logout(): void {
    this.msalService.logout();
  }

  getActiveAccount() {
    return this.msalService.instance.getActiveAccount();
  }

  getAccessToken(): Observable<AuthenticationResult> {
    return from(
      this.msalService.instance.initialize().then(() => {
        const account = this.getActiveAccount();
        if (account) {
          const accessTokenRequest = {
            scopes: ['User.Read'],
            account: account
          };
          return this.msalService.acquireTokenSilent(accessTokenRequest);
        }
        throw new Error('No active account');
      })
    ).pipe(
      switchMap((result) => from(result))
    );
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getActiveAccount() != null;
  }
}
