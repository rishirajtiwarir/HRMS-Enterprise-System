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

  ngOnInit() {
    const user = this.authService.currentUserValue;
    if (user) {
      this.username = user.username;
      this.userRole = user.roles.join(', ').replace('ROLE_', '');
      this.isAdminOrHr = this.authService.hasRole(['ROLE_ADMIN', 'ROLE_HR']);
    }

    if (this.isAdminOrHr) {
      this.loadDashboardStats();
    }
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
