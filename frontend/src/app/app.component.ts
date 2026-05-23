import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './core/services/auth.service';

// Angular Material Imports
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatMenuModule
  ],
  template: `
    <div [class.dark-theme]="isDarkMode" class="theme-wrapper">
      <!-- 1. Full-screen App Container when logged in -->
      <div *ngIf="authService.currentUser$ | async as user; else loginLayout" class="app-container">
        
        <!-- Sidenav -->
        <mat-sidenav-container class="sidenav-container">
          <mat-sidenav mode="side" opened class="custom-sidebar">
            <div class="sidebar-header">
              <span class="material-symbols-outlined logo-icon">corporate_fare</span>
              <h2>Enterprise HR</h2>
            </div>
            
            <mat-nav-list class="sidebar-menu">
              <a mat-list-item routerLink="/dashboard" routerLinkActive="active-menu">
                <mat-icon matListItemIcon>dashboard</mat-icon>
                <span matListItemTitle>Dashboard</span>
              </a>
              <a mat-list-item routerLink="/employees" routerLinkActive="active-menu">
                <mat-icon matListItemIcon>people</mat-icon>
                <span matListItemTitle>Employees</span>
              </a>
              <a mat-list-item routerLink="/leaves" routerLinkActive="active-menu">
                <mat-icon matListItemIcon>date_range</mat-icon>
                <span matListItemTitle>Leaves</span>
              </a>
              <a mat-list-item routerLink="/attendance" routerLinkActive="active-menu">
                <mat-icon matListItemIcon>assignment_turned_in</mat-icon>
                <span matListItemTitle>Attendance</span>
              </a>
              <a mat-list-item routerLink="/payroll" routerLinkActive="active-menu">
                <mat-icon matListItemIcon>payments</mat-icon>
                <span matListItemTitle>Payroll</span>
              </a>
            </mat-nav-list>

            <div class="sidebar-footer">
              <p>Logged in as</p>
              <h4>{{ user.username }}</h4>
            </div>
          </mat-sidenav>

          <!-- Main Layout -->
          <mat-sidenav-content class="main-layout-content">
            <mat-toolbar class="custom-navbar glass-card">
              <span class="navbar-title">Employee &amp; Leave Dashboard</span>
              <span class="navbar-spacer"></span>

              <!-- Theme Toggle -->
              <button mat-icon-button (click)="toggleTheme()" aria-label="Toggle dark mode">
                <mat-icon>{{ isDarkMode ? 'light_mode' : 'dark_mode' }}</mat-icon>
              </button>

              <!-- User Menu -->
              <button mat-button [matMenuTriggerFor]="menu" class="user-menu-btn">
                <mat-icon>account_circle</mat-icon>
                <span class="username-lbl">{{ user.username }}</span>
              </button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item (click)="logout()">
                  <mat-icon>exit_to_app</mat-icon>
                  <span>Logout</span>
                </button>
              </mat-menu>
            </mat-toolbar>

            <!-- Scrollable router display -->
            <div class="main-content">
              <router-outlet></router-outlet>
            </div>
          </mat-sidenav-content>
        </mat-sidenav-container>
      </div>

      <!-- 2. Simple clean layout for Login Page -->
      <ng-template #loginLayout>
        <div class="login-fullpage">
          <router-outlet></router-outlet>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .theme-wrapper {
      width: 100vw;
      height: 100vh;
      background-color: var(--background);
      color: var(--text-primary);
    }
    .sidenav-container {
      height: 100%;
      width: 100%;
      background-color: var(--background);
    }
    .custom-sidebar {
      width: 250px;
      background-color: var(--surface);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
    }
    .sidebar-header {
      padding: 1.5rem;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      border-bottom: 1px solid var(--border);

      .logo-icon {
        font-size: 2.25rem;
        color: var(--primary);
      }
      h2 {
        font-size: 1.25rem;
        font-weight: 700;
        color: var(--text-primary);
        letter-spacing: -0.5px;
      }
    }
    .sidebar-menu {
      padding-top: 1rem;
      flex: 1;

      a {
        margin: 0.25rem 0.75rem;
        border-radius: 8px;
        color: var(--text-secondary);

        &:hover {
          background-color: var(--surface-secondary);
        }
      }
      .active-menu {
        background-color: rgba(59, 130, 246, 0.1) !important;
        color: var(--primary) !important;
        font-weight: 600;
      }
    }
    .sidebar-footer {
      padding: 1.25rem;
      border-top: 1px solid var(--border);
      p {
        font-size: 0.75rem;
        color: var(--text-muted);
      }
      h4 {
        font-size: 0.875rem;
        color: var(--text-primary);
        margin-top: 0.25rem;
      }
    }
    .main-layout-content {
      display: flex;
      flex-direction: column;
      height: 100%;
      overflow: hidden;
    }
    .custom-navbar {
      background: var(--glass-bg);
      backdrop-filter: blur(12px);
      border: 1px solid var(--glass-border);
      border-radius: 0 0 16px 16px;
      margin: 0 1rem;
      height: 64px;
      padding: 0 1.5rem;
      box-shadow: var(--shadow-sm);
    }
    .navbar-title {
      font-size: 1.15rem;
      font-weight: 600;
    }
    .navbar-spacer {
      flex: 1 1 auto;
    }
    .username-lbl {
      margin-left: 0.5rem;
      font-weight: 500;
      text-transform: capitalize;
    }
    .login-fullpage {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: var(--background);
    }
  `]
})
export class AppComponent implements OnInit {
  authService = inject(AuthService);
  isDarkMode = false;

  ngOnInit() {
    // Check saved theme
    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme === 'dark';
    this.applyThemeClass();
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
    this.applyThemeClass();
  }

  private applyThemeClass() {
    if (this.isDarkMode) {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
  }

  logout() {
    this.authService.logout();
  }
}
