
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

  login(): Observable<AuthenticationResult | null> {
    // Check if we're in an iframe
    const isIframe = window !== window.parent && !window.opener;
    
    if (isIframe) {
      // Use popup for iframes
      return this.msalService.loginPopup(loginRequest);
    } else {
      // Use redirect for standalone windows
      this.msalService.loginRedirect(loginRequest);
      return new Observable(observer => {
        observer.next(null);
        observer.complete();
      });
    }
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
      return new Observable(observer => {
        observer.error(new Error('No active account'));
      });
    }
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getActiveAccount() != null;
  }

  // Method to send access token to Spring Boot backend
  validateTokenWithBackend(accessToken: string): Observable<any> {
    const url = 'http://localhost:5000/api/graph/validate-token';
    const body = { accessToken: accessToken };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to get files from backend
  getFilesFromBackend(accessToken: string): Observable<any> {
    const url = 'http://localhost:5000/api/graph/files';
    const body = { accessToken: accessToken };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to get recent files from backend
  getRecentFilesFromBackend(accessToken: string, limit: number = 10): Observable<any> {
    const url = `http://localhost:5000/api/graph/files/recent?limit=${limit}`;
    const body = { accessToken: accessToken };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to get drive item children from backend
  getDriveItemChildrenFromBackend(accessToken: string, driveId: string, itemId: string): Observable<any> {
    const url = `http://localhost:5000/api/graph/drives/${driveId}/items/${itemId}/children`;
    const body = { accessToken: accessToken };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to categorize documents
  categorizeDocuments(documentsJson: string): Observable<any> {
    const url = 'http://localhost:5000/api/categorization/categorize';
    const body = { documentsJson: documentsJson };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to categorize drive items directly
  categorizeDriveItems(accessToken: string, driveId: string, itemId: string): Observable<any> {
    const url = 'http://localhost:5000/api/categorization/categorize-drive-items';
    const body = { 
      accessToken: accessToken,
      driveId: driveId,
      itemId: itemId
    };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to analyze a single video
  analyzeVideo(videoUrl: string): Observable<any> {
    const url = 'http://localhost:5000/api/video-analysis/analyze-video';
    const body = { videoUrl: videoUrl };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }

  // Method to analyze videos from drive
  analyzeDriveVideos(accessToken: string, driveId: string, itemId: string): Observable<any> {
    const url = 'http://localhost:5000/api/video-analysis/analyze-drive-videos';
    const body = { 
      accessToken: accessToken,
      driveId: driveId,
      itemId: itemId
    };
    
    return from(fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }).then(response => response.json()));
  }
}
