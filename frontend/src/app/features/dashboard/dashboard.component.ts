import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="dashboard-wrapper">
      <div class="welcome-banner glass-card">
        <div>
          <h1>Welcome, {{ username }}!</h1>
          <p>Here is your enterprise overview for today.</p>
        </div>
        <div class="banner-badge">
          <span class="badge badge-success">{{ userRole }}</span>
        </div>
      </div>

      <!-- Counters Section (For Admin and HR roles) -->
      <div *ngIf="isAdminOrHr && stats; else employeeWelcome" class="metrics-grid">
        <div class="glass-card metric-card">
          <div class="metric-icon"><mat-icon>people</mat-icon></div>
          <div class="metric-info">
            <h3>Active Employees</h3>
            <p>{{ stats.activeEmployees }} <span class="sub-lbl">/ {{ stats.totalEmployees }} total</span></p>
          </div>
        </div>

        <div class="glass-card metric-card">
          <div class="metric-icon"><mat-icon>corporate_fare</mat-icon></div>
          <div class="metric-info">
            <h3>Departments</h3>
            <p>{{ stats.totalDepartments }}</p>
          </div>
        </div>

        <div class="glass-card metric-card">
          <div class="metric-icon"><mat-icon>assignment_turned_in</mat-icon></div>
          <div class="metric-info">
            <h3>Present Today</h3>
            <p>{{ stats.presentToday }}</p>
          </div>
        </div>

        <div class="glass-card metric-card">
          <div class="metric-icon"><mat-icon>pending_actions</mat-icon></div>
          <div class="metric-info">
            <h3>Pending Leaves</h3>
            <p class="warning-text">{{ stats.pendingLeaveRequests }}</p>
          </div>
        </div>
      </div>

      <!-- Detail grids (Charts / Lists) -->
      <div *ngIf="isAdminOrHr && stats" class="dashboard-details-grid">
        <div class="glass-card details-card">
          <h3>Employee Distribution by Department</h3>
          <div class="chart-list">
            <div *ngFor="let item of deptDistribution | keyvalue" class="chart-item">
              <div class="chart-label">
                <span>{{ item.key }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="chart-progress-bg">
                <div class="chart-progress-fill" [style.width.%]="getPercent(item.value, stats.activeEmployees)"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="glass-card details-card">
          <h3>Salary Allocation by Department</h3>
          <div class="chart-list">
            <div *ngFor="let item of deptSalaryBudget | keyvalue" class="chart-item">
              <div class="chart-label">
                <span>{{ item.key }}</span>
                <strong>$ {{ formatCurrency(item.value) }}</strong>
              </div>
              <div class="chart-progress-bg">
                <div class="chart-progress-fill budget-fill" [style.width.%]="getBudgetPercent(item.value)"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Feedback Support Tickets Inbox (For Admin and HR roles) -->
      <div *ngIf="isAdminOrHr" class="feedback-inbox-section glass-card">
        <div class="inbox-header">
          <div class="header-title">
            <mat-icon class="inbox-icon">feedback</mat-icon>
            <h2>Support & Feedback Inbox</h2>
          </div>
          <span class="badge badge-info" *ngIf="unreadCount > 0">{{ unreadCount }} Unread</span>
        </div>

        <div *ngIf="feedbacks.length === 0" class="empty-inbox">
          <mat-icon class="empty-icon">mail_outline</mat-icon>
          <p>No feedback or support messages received yet.</p>
        </div>

        <div *ngIf="feedbacks.length > 0" class="tickets-list">
          <div *ngFor="let item of feedbacks" class="ticket-item" [class.unread]="!item.read">
            <div class="ticket-top">
              <div class="ticket-sender">
                <strong>{{ item.name }}</strong>
                <span class="sender-email">&lt;{{ item.email }}&gt;</span>
              </div>
              <span class="ticket-date">{{ item.createdAt | date:'short' }}</span>
            </div>

            <div class="ticket-subject">
              <span class="subject-tag">Subject:</span> {{ item.subject }}
            </div>

            <p class="ticket-message">{{ item.message }}</p>

            <div class="ticket-actions">
              <button *ngIf="!item.read" class="action-btn read-btn" (click)="markFeedbackAsRead(item.id)">
                <mat-icon>done_all</mat-icon> Mark Read
              </button>
              <button class="action-btn delete-btn" (click)="deleteFeedback(item.id)">
                <mat-icon>delete</mat-icon> Delete
              </button>
            </div>
          </div>
        </div>
      </div>

      <ng-template #employeeWelcome>
        <div class="glass-card employee-quickactions">
          <h3>Quick Links</h3>
          <p>Please use the side navigation to check in, apply for leaves, or download your monthly payslips.</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .dashboard-wrapper {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .welcome-banner {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1)) !important;
      h1 {
        font-size: 1.75rem;
        font-weight: 700;
        margin-bottom: 0.25rem;
      }
      p {
        color: var(--text-secondary);
      }
    }
    .banner-badge {
      font-size: 1rem;
    }
    .sub-lbl {
      font-size: 0.875rem;
      color: var(--text-muted);
      font-weight: 400;
    }
    .warning-text {
      color: var(--warning) !important;
    }
    .dashboard-details-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 1.5rem;
    }
    .details-card {
      h3 {
        font-size: 1.1rem;
        font-weight: 600;
        margin-bottom: 1.5rem;
        border-left: 4px solid var(--primary);
        padding-left: 0.75rem;
      }
    }
    .chart-list {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }
    .chart-item {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
    }
    .chart-label {
      display: flex;
      justify-content: space-between;
      font-size: 0.9rem;
      color: var(--text-secondary);
    }
    .chart-progress-bg {
      height: 8px;
      width: 100%;
      background-color: var(--surface-secondary);
      border-radius: 9999px;
      overflow: hidden;
    }
    .chart-progress-fill {
      height: 100%;
      background: linear-gradient(90deg, var(--primary), var(--accent));
      border-radius: 9999px;
    }
    .budget-fill {
      background: linear-gradient(90deg, hsl(142, 72%, 45%), hsl(142, 72%, 60%)) !important;
    }
    .employee-quickactions {
      padding: 2rem;
      h3 {
        margin-bottom: 0.5rem;
      }
      p {
        color: var(--text-secondary);
      }
    }

    /* Feedback Inbox Styles */
    .feedback-inbox-section {
      margin-top: 1.5rem;
      padding: 2rem;
      display: flex;
      flex-direction: column;
      gap: 1.5rem;

      .inbox-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
        padding-bottom: 1rem;

        .header-title {
          display: flex;
          align-items: center;
          gap: 0.65rem;

          .inbox-icon {
            color: var(--primary);
            font-size: 1.5rem;
          }

          h2 {
            font-size: 1.25rem;
            font-weight: 700;
            color: white;
            margin: 0;
          }
        }
      }
    }

    .empty-inbox {
      text-align: center;
      padding: 3rem 1rem;
      color: var(--text-secondary);

      .empty-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
        opacity: 0.4;
      }

      p {
        margin: 0;
        font-size: 0.95rem;
      }
    }

    .tickets-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      max-height: 450px;
      overflow-y: auto;
      padding-right: 0.5rem;
    }

    .ticket-item {
      background: rgba(255, 255, 255, 0.02);
      border: 1px solid rgba(255, 255, 255, 0.04);
      border-radius: 12px;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      transition: all 0.25s ease;

      &.unread {
        border-left: 4px solid var(--primary);
        background: rgba(59, 130, 246, 0.03);
      }

      &:hover {
        background: rgba(255, 255, 255, 0.04);
        border-color: rgba(255, 255, 255, 0.08);
      }

      .ticket-top {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .ticket-sender {
          font-size: 0.95rem;
          color: white;
          display: flex;
          align-items: center;
          gap: 0.5rem;

          .sender-email {
            font-size: 0.8rem;
            color: var(--text-secondary);
          }
        }

        .ticket-date {
          font-size: 0.75rem;
          color: var(--text-muted);
        }
      }

      .ticket-subject {
        font-size: 0.9rem;
        color: white;
        font-weight: 500;

        .subject-tag {
          color: var(--primary);
          font-weight: 600;
        }
      }

      .ticket-message {
        font-size: 0.85rem;
        color: var(--text-secondary);
        line-height: 1.5;
        margin: 0;
        white-space: pre-wrap;
      }

      .ticket-actions {
        display: flex;
        gap: 0.75rem;
        margin-top: 0.25rem;

        .action-btn {
          display: flex;
          align-items: center;
          gap: 0.35rem;
          font-size: 0.75rem;
          font-weight: 600;
          padding: 6px 12px;
          border-radius: 6px;
          border: none;
          cursor: pointer;
          transition: all 0.2s ease;

          mat-icon {
            font-size: 1rem;
            width: 16px;
            height: 16px;
          }

          &.read-btn {
            background-color: rgba(59, 130, 246, 0.1);
            color: #3b82f6;

            &:hover {
              background-color: rgba(59, 130, 246, 0.2);
            }
          }

          &.delete-btn {
            background-color: rgba(239, 68, 68, 0.1);
            color: #ef4444;

            &:hover {
              background-color: rgba(239, 68, 68, 0.2);
            }
          }
        }
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  stats: any = null;
  username = '';
  userRole = '';
  isAdminOrHr = false;

  deptDistribution: Map<string, number> = new Map();
  deptSalaryBudget: Map<string, number> = new Map();

  feedbacks: any[] = [];
  unreadCount = 0;

  ngOnInit() {
    const user = this.authService.currentUserValue;
    if (user) {
      this.username = user.username;
      this.userRole = user.roles.join(', ').replace('ROLE_', '');
      this.isAdminOrHr = this.authService.hasRole(['ROLE_ADMIN', 'ROLE_HR']);
    }

    if (this.isAdminOrHr) {
      this.loadDashboardStats();
      this.loadFeedbacks();
    }
  }
  loadFeedbacks() {
    this.http.get<any[]>('http://localhost:8080/api/v1/feedback').subscribe({
      next: (data) => {
        this.feedbacks = data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.unreadCount = this.feedbacks.filter(f => !f.read).length;
      },
      error: () => {}
    });
  }

  markFeedbackAsRead(id: number) {
    this.http.put<any>(`http://localhost:8080/api/v1/feedback/${id}/read`, {}).subscribe({
      next: () => {
        this.loadFeedbacks();
      },
      error: () => {}
    });
  }

  deleteFeedback(id: number) {
    this.http.delete<any>(`http://localhost:8080/api/v1/feedback/${id}`).subscribe({
      next: () => {
        this.loadFeedbacks();
      },
      error: () => {}
    });
  }

  private loadDashboardStats() {
    this.http.get<any>('http://localhost:8080/api/v1/employees/dashboard/stats').subscribe({
      next: (data) => {
        this.stats = data;
        this.deptDistribution = data.employeesPerDepartment;
        this.deptSalaryBudget = data.salaryBudgetPerDepartment;
      },
      error: () => {}
    });
  }

  getPercent(value: any, total: number): number {
    const val = Number(value);
    if (!total) return 0;
    return (val / total) * 100;
  }

  getBudgetPercent(value: any): number {
    const val = Number(value);
    if (!this.stats || !this.stats.salaryBudgetPerDepartment) return 0;
    
    // Find the max department budget to scale
    const budgets = Object.values(this.stats.salaryBudgetPerDepartment).map(v => Number(v));
    const maxBudget = Math.max(...budgets, 1);
    return (val / maxBudget) * 100;
  }

  formatCurrency(value: any): string {
    const val = Number(value);
    return val.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
