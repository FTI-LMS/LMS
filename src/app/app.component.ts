import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { EventMessage, EventType, InteractionStatus } from '@azure/msal-browser';
import { AuthService } from './auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, FormsModule],
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
  files: any[] = [];
  driveFiles: any[] = [];
  driveId: string = 'b!h-u0gl1vu0mtETS610zX9hufYTIu9hZJjUnn3YdGkBYfslJiWknLTrSquiV92Sgm';
  itemId: string = '015ZUXCKF5VF3V734C3JG3SQ4RE5RHPGBY';
  loading = false;
  showDriveConfig = false;
  categorizedDocuments: any = null;
  showCategories = false;
  videoAnalysisResults: any = null;
  showVideoAnalysis = false;
  analyzingVideo = false;

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

      // Handle redirect response for non-iframe scenarios
      if (!this.isIframe) {
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
      }

      // Initialize login display
      this.setLoginDisplay();

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
    this.authService.login().subscribe({
      next: (result) => {
        if (result) {
          console.log('Login successful', result);
          this.setLoginDisplay();
          this.getAccessToken();
        }
        // For redirect, result will be null and we'll handle success in handleRedirectObservable
      },
      error: (error) => console.error('Login failed', error)
    });
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

        // Validate token with backend and get files
        this.validateTokenAndGetFiles();
      },
      error: (error) => console.error('Failed to get access token', error)
    });
  }

  validateTokenAndGetFiles() {
    if (this.accessToken) {
      // Validate token with Spring Boot backend
      this.authService.validateTokenWithBackend(this.accessToken).subscribe({
        next: (response) => {
          console.log('Token validation response:', response);
          if (response.valid) {
            // Get files from backend
            this.getFilesFromBackend();
          }
        },
        error: (error) => console.error('Token validation failed:', error)
      });
    }
  }

  getFilesFromBackend() {
    if (this.accessToken) {
      this.authService.getFilesFromBackend(this.accessToken).subscribe({
        next: (response) => {
          console.log('Files from backend:', response);
          this.files = response.files || [];
        },
        error: (error) => console.error('Failed to get files from backend:', error)
      });
    }
  }

  getRecentFiles() {
    if (this.accessToken) {
      this.loading = true;
      this.authService.getRecentFilesFromBackend(this.accessToken, 10).subscribe({
        next: (response) => {
          console.log('Recent files response:', response);
          if (response.files) {
            this.files = response.files;
          }
          this.loading = false;
        },
        error: (error) => {
          console.error('Error fetching recent files:', error);
          this.loading = false;
        }
      });
    }
  }

  getDriveItemChildren(): void {
    this.showDriveConfig = true;
    if (this.accessToken && isPlatformBrowser(this.platformId)) {
      this.authService.getDriveItemChildrenFromBackend(this.accessToken, this.driveId, this.itemId).subscribe({
        next: (response) => {
          console.log('Drive item children response:', response);
          if (response && response.files) {
            this.driveFiles = response.files;
            console.log('Drive item children loaded:', this.driveFiles);
          }
        },
        error: (error) => {
          console.error('Error getting drive item children:', error);
        }
      });
    } else {
      console.log('No access token available or not in browser');
    }
  }

  categorizeDocuments(): void {
    if (this.accessToken && isPlatformBrowser(this.platformId)) {
      this.loading = true;
      this.authService.categorizeDriveItems(this.accessToken, this.driveId, this.itemId).subscribe({
        next: (response) => {
          console.log('Categorization response:', response);
          this.categorizedDocuments = response;
          this.showCategories = true;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error categorizing documents:', error);
          this.loading = false;
        }
      });
    }
  }

  getCategoryKeys(): string[] {
    return this.categorizedDocuments?.categories ? Object.keys(this.categorizedDocuments.categories) : [];
  }

  toggleCategoriesView(): void {
    this.showCategories = !this.showCategories;
  }

  analyzeVideos(): void {
    if (this.accessToken && isPlatformBrowser(this.platformId)) {
      this.analyzingVideo = true;
      this.authService.analyzeDriveVideos(this.accessToken, this.driveId, this.itemId).subscribe({
        next: (response) => {
          console.log('Video analysis response:', response);
          this.videoAnalysisResults = response;
          this.showVideoAnalysis = true;
          this.analyzingVideo = false;
        },
        error: (error) => {
          console.error('Error analyzing videos:', error);
          this.analyzingVideo = false;
        }
      });
    }
  }

  analyzeSpecificVideo(videoUrl: string): void {
    if (videoUrl && isPlatformBrowser(this.platformId)) {
      this.analyzingVideo = true;
      this.authService.analyzeVideo(videoUrl).subscribe({
        next: (response) => {
          console.log('Single video analysis response:', response);
          this.videoAnalysisResults = response;
          this.showVideoAnalysis = true;
          this.analyzingVideo = false;
        },
        error: (error) => {
          console.error('Error analyzing video:', error);
          this.analyzingVideo = false;
        }
      });
    }
  }

  toggleVideoAnalysisView(): void {
    this.showVideoAnalysis = !this.showVideoAnalysis;
  }

  isVideoFile(fileName: string): boolean {
    const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv'];
    return videoExtensions.some(ext => fileName.toLowerCase().endsWith(ext));
  }

  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }
}