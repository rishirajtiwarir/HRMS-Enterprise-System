import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-payroll',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule],
  template: `
    <div class="payroll-wrapper">
      <div class="header-actions">
        <h1>Payroll &amp; Compensation</h1>
        <button *ngIf="isAdminOrHr" mat-flat-button color="primary" class="btn-primary" (click)="toggleBulkPanel()">
          <mat-icon>price_check</mat-icon> Generate Monthly Payroll
        </button>
      </div>

      <!-- Bulk Generation Panel (Admin/HR) -->
      <div *ngIf="showBulkPanel" class="glass-card config-panel">
        <h3>Process Monthly Salary Cycle</h3>
        <p class="description-lbl">This will calculate basic salaries, allowances, and deductions for all active employees for the selected period.</p>
        
        <div class="date-inputs-row">
          <div class="custom-form-group">
            <label>Cycle Start Date</label>
            <input type="date" [(ngModel)]="cycleStart">
          </div>
          <div class="custom-form-group">
            <label>Cycle End Date</label>
            <input type="date" [(ngModel)]="cycleEnd">
          </div>
        </div>

        <div class="form-actions-row">
          <button class="btn btn-secondary" (click)="closeBulkPanel()">Cancel</button>
          <button class="btn btn-primary" (click)="generateBulkPayroll()">Generate Invoices</button>
        </div>
      </div>

      <!-- Payroll Records -->
      <div class="glass-card records-card">
        <h3>{{ isAdminOrHr ? 'Payroll Registry' : 'Your Payslips' }}</h3>

        <div class="custom-table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Invoice ID</th>
                <th *ngIf="isAdminOrHr">Employee Name</th>
                <th>Designation</th>
                <th>Period</th>
                <th>Basic Salary</th>
                <th>Net Salary</th>
                <th>Status</th>
                <th style="text-align: right;">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let pay of payrolls">
                <td>#{{ pay.id }}</td>
                <td *ngIf="isAdminOrHr" class="bold-text">{{ pay.employeeName }}</td>
                <td>{{ pay.designation }}</td>
                <td>
                  <div class="period-lbl">
                    <span>{{ pay.payPeriodStart }}</span> to <span>{{ pay.payPeriodEnd }}</span>
                  </div>
                </td>
                <td>\${{ pay.basicSalary | number:'1.2-2' }}</td>
                <td class="bold-text">\${{ pay.netSalary | number:'1.2-2' }}</td>
                <td>
                  <span class="badge" [ngClass]="{
                    'badge-success': pay.status === 'PAID',
                    'badge-warning': pay.status === 'PENDING'
                  }">{{ pay.status }}</span>
                </td>
                <td class="actions-cell">
                  <div class="row-actions">
                    <!-- Mark Paid action (Admin/HR only) -->
                    <button *ngIf="isAdminOrHr && pay.status === 'PENDING'" class="action-btn mark-paid-btn" (click)="markAsPaid(pay.id)" title="Release payment">
                      <mat-icon>task_alt</mat-icon> Pay
                    </button>
                    <!-- Download Payslip action -->
                    <button class="action-btn download-btn" (click)="downloadPayslip(pay.id)" title="Download Payslip">
                      <mat-icon>download</mat-icon> Payslip
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="payrolls.length === 0">
                <td colspan="8" class="no-data">No payroll records found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .payroll-wrapper {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      h1 {
        font-size: 1.75rem;
        font-weight: 700;
      }
    }
    .config-panel {
      animation: slideDown 0.3s ease;
      h3 {
        font-size: 1.15rem;
        font-weight: 600;
        margin-bottom: 0.25rem;
      }
      .description-lbl {
        font-size: 0.85rem;
        color: var(--text-secondary);
        margin-bottom: 1.25rem;
      }
    }
    .date-inputs-row {
      display: flex;
      gap: 1.5rem;
      flex-wrap: wrap;
      margin-bottom: 1.25rem;
    }
    .form-actions-row {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
    }
    .period-lbl {
      font-size: 0.85rem;
      color: var(--text-secondary);
      span {
        font-weight: 500;
        color: var(--text-primary);
      }
    }
    .actions-cell {
      text-align: right;
    }
    .row-actions {
      display: inline-flex;
      justify-content: flex-end;
      gap: 0.5rem;
      width: 100%;
    }
    .mark-paid-btn {
      color: var(--success);
      font-weight: 600;
      font-size: 0.85rem;
      gap: 0.25rem;
      &:hover {
        background-color: rgba(34, 197, 94, 0.1) !important;
      }
    }
    .download-btn {
      color: var(--primary);
      font-weight: 600;
      font-size: 0.85rem;
      gap: 0.25rem;
      &:hover {
        background-color: rgba(59, 130, 246, 0.1) !important;
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
    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-10px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class PayrollComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  payrolls: any[] = [];
  employeeId: number | null = null;
  isAdminOrHr = false;

  // Bulk parameters
  showBulkPanel = false;
  cycleStart = '';
  cycleEnd = '';

  ngOnInit() {
    this.employeeId = this.authService.getEmployeeId();
    this.isAdminOrHr = this.authService.hasRole(['ROLE_ADMIN', 'ROLE_HR']);

    // Set default dates to current month range
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    this.cycleStart = firstDay.toISOString().substring(0, 10);
    this.cycleEnd = lastDay.toISOString().substring(0, 10);

    this.loadPayrollRegistry();
  }

  loadPayrollRegistry() {
    let url = 'http://localhost:8080/api/v1/payroll';
    if (!this.isAdminOrHr && this.employeeId) {
      url = `http://localhost:8080/api/v1/payroll/employee/${this.employeeId}`;
    }

    this.http.get<any[]>(url).subscribe({
      next: (data) => this.payrolls = data,
      error: () => {}
    });
  }

  toggleBulkPanel() {
    this.showBulkPanel = !this.showBulkPanel;
  }

  closeBulkPanel() {
    this.showBulkPanel = false;
  }

  generateBulkPayroll() {
    if (!this.cycleStart || !this.cycleEnd) {
      this.notificationService.showError('Please choose a valid cycle date range.');
      return;
    }

    this.http.post<any[]>(`http://localhost:8080/api/v1/payroll/generate-bulk?startDate=${this.cycleStart}&endDate=${this.cycleEnd}`, {}).subscribe({
      next: (data) => {
        this.notificationService.showSuccess(`Processed monthly invoices. Generated ${data.length} records.`);
        this.showBulkPanel = false;
        this.loadPayrollRegistry();
      },
      error: (err) => {
        this.notificationService.showError(err.error?.message || 'Failed to process monthly salaries.');
      }
    });
  }

  markAsPaid(id: number) {
    this.http.put<any>(`http://localhost:8080/api/v1/payroll/${id}/status?status=PAID`, {}).subscribe({
      next: () => {
        this.notificationService.showSuccess('Payout released and employee notified.');
        this.loadPayrollRegistry();
      },
      error: (err) => {
        this.notificationService.showError(err.error?.message || 'Failed to process payout.');
      }
    });
  }

  downloadPayslip(id: number) {
    // Download as a raw attachment file
    const url = `http://localhost:8080/api/v1/payroll/${id}/payslip`;
    
    this.http.get(url, { responseType: 'text' }).subscribe({
      next: (response) => {
        // Create an anchor tag and download the file text locally
        const blob = new Blob([response], { type: 'text/plain;charset=utf-8' });
        const downloadUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `payslip_invoice_${id}.txt`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.notificationService.showSuccess('Payslip download started.');
      },
      error: (err) => {
        this.notificationService.showError('Failed to retrieve payslip document.');
      }
    });
  }
}
