import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule],
  template: `
    <div class="attendance-wrapper">
      <div class="header-actions">
        <h1>Attendance Portal</h1>
      </div>

      <!-- Quick Action: Mark check-in / check-out -->
      <div *ngIf="employeeId" class="glass-card status-panel">
        <div class="status-header">
          <h3>Daily Attendance Logger</h3>
          <span class="badge" [ngClass]="getTodayBadgeClass()">{{ todayStatus ? todayStatus.status : 'NOT MARKED' }}</span>
        </div>

        <div class="status-times-row">
          <div class="time-block">
            <p class="lbl">Check In</p>
            <p class="time">{{ todayStatus?.checkIn ? todayStatus.checkIn : '--:--:--' }}</p>
          </div>
          <div class="time-block">
            <p class="lbl">Check Out</p>
            <p class="time">{{ todayStatus?.checkOut ? todayStatus.checkOut : '--:--:--' }}</p>
          </div>
          <div class="time-block">
            <p class="lbl">Total Hours</p>
            <p class="time">{{ todayStatus?.workHours ? todayStatus.workHours + ' hrs' : '--' }}</p>
          </div>
        </div>

        <div class="log-actions-row">
          <button mat-flat-button color="primary" class="btn btn-primary" (click)="checkIn()" [disabled]="!!todayStatus">
            <mat-icon>login</mat-icon> Check In
          </button>
          <button mat-flat-button color="accent" class="btn btn-primary check-out-btn" (click)="checkOut()" [disabled]="!todayStatus || !!todayStatus.checkOut">
            <mat-icon>logout</mat-icon> Check Out
          </button>
        </div>
      </div>

      <!-- Attendance History -->
      <div class="glass-card history-panel">
        <div class="history-header">
          <h3>{{ isAdminOrHr ? 'Daily Attendance Monitor' : 'Your Attendance Records' }}</h3>
          
          <div *ngIf="!isAdminOrHr && employeeId" class="date-filters">
            <div class="filter-group">
              <label>From</label>
              <input type="date" [(ngModel)]="startDate" (change)="loadHistory()">
            </div>
            <div class="filter-group">
              <label>To</label>
              <input type="date" [(ngModel)]="endDate" (change)="loadHistory()">
            </div>
            <button class="btn btn-secondary" (click)="exportPdf()"><mat-icon>download</mat-icon> Print Report</button>
          </div>
        </div>

        <div class="custom-table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Date</th>
                <th *ngIf="isAdminOrHr">Employee Name</th>
                <th>Check In</th>
                <th>Check Out</th>
                <th>Work Hours</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let record of history">
                <td>{{ record.date }}</td>
                <td *ngIf="isAdminOrHr" class="bold-text">{{ record.employeeName }}</td>
                <td>{{ record.checkIn || '--' }}</td>
                <td>{{ record.checkOut || '--' }}</td>
                <td>{{ record.workHours ? record.workHours + ' hrs' : '--' }}</td>
                <td>
                  <span class="badge" [ngClass]="{
                    'badge-success': record.status === 'PRESENT',
                    'badge-warning': record.status === 'LATE',
                    'badge-danger': record.status === 'ABSENT'
                  }">{{ record.status }}</span>
                </td>
              </tr>
              <tr *ngIf="history.length === 0">
                <td [attr.colspan]="isAdminOrHr ? 6 : 5" class="no-data">No attendance records found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .attendance-wrapper {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .header-actions {
      h1 {
        font-size: 1.75rem;
        font-weight: 700;
      }
    }
    .status-panel {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .status-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      h3 {
        font-size: 1.15rem;
        font-weight: 600;
      }
    }
    .status-times-row {
      display: flex;
      gap: 3rem;
      border-bottom: 1px solid var(--border);
      padding-bottom: 1.5rem;
    }
    .time-block {
      .lbl {
        font-size: 0.8rem;
        color: var(--text-secondary);
        text-transform: uppercase;
        margin-bottom: 0.25rem;
      }
      .time {
        font-size: 1.75rem;
        font-weight: 700;
        color: var(--text-primary);
      }
    }
    .log-actions-row {
      display: flex;
      gap: 1rem;
    }
    .check-out-btn {
      background-color: var(--accent) !important;
      &:hover {
        opacity: 0.95;
        box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3) !important;
      }
    }
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.25rem;
      flex-wrap: wrap;
      gap: 1rem;
      h3 {
        font-size: 1.15rem;
        font-weight: 600;
      }
    }
    .date-filters {
      display: flex;
      align-items: flex-end;
      gap: 1rem;
      flex-wrap: wrap;
    }
    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      label {
        font-size: 0.75rem;
        color: var(--text-secondary);
        text-transform: uppercase;
      }
      input {
        padding: 0.5rem;
        border-radius: 6px;
        border: 1px solid var(--border);
        background-color: var(--surface);
        color: var(--text-primary);
      }
    }
    .no-data {
      text-align: center;
      color: var(--text-secondary);
      padding: 2.5rem !important;
    }
    .bold-text {
      font-weight: 600;
    }
  `]
})
export class AttendanceComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  todayStatus: any = null;
  history: any[] = [];
  employeeId: number | null = null;
  isAdminOrHr = false;

  // Filter properties
  startDate = '';
  endDate = '';

  ngOnInit() {
    this.employeeId = this.authService.getEmployeeId();
    this.isAdminOrHr = this.authService.hasRole(['ROLE_ADMIN', 'ROLE_HR']);

    // Set default range filter for the last 30 days
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    this.startDate = thirtyDaysAgo.toISOString().substring(0, 10);
    this.endDate = today.toISOString().substring(0, 10);

    if (this.employeeId) {
      this.loadTodayStatus();
    }
    this.loadHistory();
  }

  loadTodayStatus() {
    this.http.get<any>('http://localhost:8080/api/v1/attendance/today-status').subscribe({
      next: (data) => this.todayStatus = data,
      error: () => {}
    });
  }

  loadHistory() {
    let url = 'http://localhost:8080/api/v1/attendance/today'; // Default for admin/hr is showing today's logs
    
    if (!this.isAdminOrHr && this.employeeId) {
      url = `http://localhost:8080/api/v1/attendance/employee/${this.employeeId}?startDate=${this.startDate}&endDate=${this.endDate}`;
    }

    this.http.get<any[]>(url).subscribe({
      next: (data) => this.history = data,
      error: () => {}
    });
  }

  checkIn() {
    this.http.post<any>('http://localhost:8080/api/v1/attendance/check-in', {}).subscribe({
      next: (data) => {
        this.notificationService.showSuccess('Checked in successfully!');
        this.todayStatus = data;
        this.loadHistory();
      },
      error: (err) => {
        this.notificationService.showError(err.error?.message || 'Check-in failed.');
      }
    });
  }

  checkOut() {
    this.http.post<any>('http://localhost:8080/api/v1/attendance/check-out', {}).subscribe({
      next: (data) => {
        this.notificationService.showSuccess('Checked out successfully!');
        this.todayStatus = data;
        this.loadHistory();
      },
      error: (err) => {
        this.notificationService.showError(err.error?.message || 'Check-out failed.');
      }
    });
  }

  getTodayBadgeClass(): string {
    if (!this.todayStatus) return 'badge-danger';
    if (this.todayStatus.status === 'PRESENT') return 'badge-success';
    if (this.todayStatus.status === 'LATE') return 'badge-warning';
    return 'badge-danger';
  }

  exportPdf() {
    // Generate browser print trigger of the table
    window.print();
  }
}
