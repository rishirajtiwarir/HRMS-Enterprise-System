import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-leave-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatIconModule, MatButtonModule],
  template: `
    <div class="leaves-wrapper">
      <div class="header-row">
        <h1>Leave Management</h1>
        <button *ngIf="!isAdminOrHr" mat-flat-button color="primary" class="btn-primary" (click)="toggleApplyForm()">
          <mat-icon>add_box</mat-icon> Apply for Leave
        </button>
      </div>

      <!-- Balances Panel (For Employees) -->
      <div *ngIf="!isAdminOrHr && balances" class="balances-row">
        <div *ngFor="let item of balances | keyvalue" class="glass-card balance-card">
          <h4>{{ item.key }} Balance</h4>
          <p>{{ item.value }} <span>days left</span></p>
        </div>
      </div>

      <!-- Apply Leave Form -->
      <div *ngIf="showApplyForm" class="glass-card apply-form-card">
        <h3>Submit Leave Request</h3>
        <form [formGroup]="leaveForm" (ngSubmit)="submitRequest()" class="grid-form">
          <div class="custom-form-group">
            <label>Leave Category</label>
            <select formControlName="leaveType">
              <option value="SICK">Sick Leave</option>
              <option value="CASUAL">Casual Leave</option>
              <option value="VACATION">Vacation / Annual Leave</option>
              <option value="MATERNITY">Maternity / Parental Leave</option>
            </select>
          </div>
          <div class="custom-form-group">
            <label>Start Date</label>
            <input type="date" formControlName="startDate">
          </div>
          <div class="custom-form-group">
            <label>End Date</label>
            <input type="date" formControlName="endDate">
          </div>
          <div class="custom-form-group text-area-group">
            <label>Detailed Reason</label>
            <textarea formControlName="reason" rows="3" placeholder="Provide explanation for leave request..."></textarea>
          </div>

          <div class="form-actions-row">
            <button type="button" class="btn btn-secondary" (click)="closeApplyForm()">Cancel</button>
            <button type="submit" class="btn btn-primary" [disabled]="leaveForm.invalid">Submit Application</button>
          </div>
        </form>
      </div>

      <!-- Rejection Modal/Prompt Panel -->
      <div *ngIf="resolvingRequest" class="glass-card resolve-card">
        <h3>Reject Leave Request (ID: {{ resolvingRequest.id }})</h3>
        <p>Employee: <strong>{{ resolvingRequest.employeeName }}</strong></p>
        <div class="custom-form-group">
          <label>Explain Rejection Reason</label>
          <input type="text" [(ngModel)]="rejectionText" placeholder="e.g. Project deliverable deadline">
        </div>
        <div class="form-actions-row">
          <button type="button" class="btn btn-secondary" (click)="cancelResolve()">Cancel</button>
          <button type="button" class="btn btn-danger" (click)="confirmRejection()">Submit Rejection</button>
        </div>
      </div>

      <!-- Leaves History Table -->
      <div class="glass-card table-section">
        <h3>{{ isAdminOrHr ? 'Employee Leave Request Center' : 'Your Leave Application History' }}</h3>
        
        <div class="custom-table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th *ngIf="isAdminOrHr">Employee</th>
                <th>Leave Type</th>
                <th>Duration</th>
                <th>Reason</th>
                <th>Status</th>
                <th *ngIf="isAdminOrHr" style="text-align: right;">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let req of requests">
                <td>{{ req.id }}</td>
                <td *ngIf="isAdminOrHr" class="bold-text">{{ req.employeeName }}</td>
                <td>
                  <span class="type-badge">{{ req.leaveType }}</span>
                </td>
                <td>
                  <div class="date-range-lbl">
                    <span>{{ req.startDate }}</span> to <span>{{ req.endDate }}</span>
                  </div>
                </td>
                <td>{{ req.reason }}</td>
                <td>
                  <span class="badge" [ngClass]="{
                    'badge-success': req.status === 'APPROVED',
                    'badge-danger': req.status === 'REJECTED',
                    'badge-warning': req.status === 'PENDING'
                  }">{{ req.status }}</span>
                  
                  <p *ngIf="req.status === 'REJECTED' && req.rejectionReason" class="rejection-note">
                    Reason: {{ req.rejectionReason }}
                  </p>
                </td>
                <td *ngIf="isAdminOrHr" class="actions-cell">
                  <div *ngIf="req.status === 'PENDING'" class="row-actions">
                    <button class="action-btn approve-btn" (click)="resolveRequest(req, 'APPROVED')" title="Approve">
                      <mat-icon>check_circle</mat-icon> Approve
                    </button>
                    <button class="action-btn reject-btn" (click)="resolveRequest(req, 'REJECTED')" title="Reject">
                      <mat-icon>cancel</mat-icon> Reject
                    </button>
                  </div>
                  <span *ngIf="req.status !== 'PENDING'" class="processed-lbl">Resolved</span>
                </td>
              </tr>
              <tr *ngIf="requests.length === 0">
                <td colspan="7" class="no-data">No leave requests found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .leaves-wrapper {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      h1 {
        font-size: 1.75rem;
        font-weight: 700;
      }
    }
    .balances-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
    }
    .balance-card {
      text-align: center;
      padding: 1.25rem;
      h4 {
        font-size: 0.85rem;
        color: var(--text-secondary);
        font-weight: 500;
        text-transform: uppercase;
        margin-bottom: 0.5rem;
      }
      p {
        font-size: 1.75rem;
        font-weight: 700;
        color: var(--primary);
        span {
          font-size: 0.85rem;
          color: var(--text-muted);
          font-weight: 400;
        }
      }
    }
    .apply-form-card, .resolve-card {
      animation: slideDown 0.3s ease;
      h3 {
        margin-bottom: 1.25rem;
        font-size: 1.15rem;
        font-weight: 600;
      }
    }
    .grid-form {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1.25rem;
    }
    .text-area-group {
      grid-column: 1 / -1;
      textarea {
        resize: none;
      }
    }
    .form-actions-row {
      grid-column: 1 / -1;
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
    }
    .type-badge {
      font-weight: 600;
      font-size: 0.8rem;
      color: var(--primary);
      background-color: rgba(59, 130, 246, 0.1);
      padding: 2px 6px;
      border-radius: 4px;
    }
    .date-range-lbl {
      font-size: 0.875rem;
      color: var(--text-secondary);
      span {
        font-weight: 500;
        color: var(--text-primary);
      }
    }
    .rejection-note {
      font-size: 0.75rem;
      color: var(--danger);
      margin-top: 0.25rem;
      font-style: italic;
    }
    .actions-cell {
      text-align: right;
    }
    .row-actions {
      display: inline-flex;
      gap: 0.5rem;
    }
    .approve-btn {
      color: var(--success);
      font-weight: 600;
      font-size: 0.85rem;
      gap: 0.25rem;
      &:hover {
        background-color: rgba(34, 197, 94, 0.1) !important;
      }
    }
    .reject-btn {
      color: var(--danger);
      font-weight: 600;
      font-size: 0.85rem;
      gap: 0.25rem;
      &:hover {
        background-color: rgba(239, 68, 68, 0.1) !important;
      }
    }
    .processed-lbl {
      font-size: 0.85rem;
      color: var(--text-muted);
      font-weight: 500;
    }
    .no-data {
      text-align: center;
      color: var(--text-secondary);
      padding: 2.5rem !important;
    }
    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-10px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class LeaveListComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  requests: any[] = [];
  balances: any = null;
  isAdminOrHr = false;
  employeeId: number | null = null;

  // Form toggles
  showApplyForm = false;
  leaveForm!: FormGroup;

  // Resolving toggles
  resolvingRequest: any = null;
  rejectionText = '';

  ngOnInit() {
    this.isAdminOrHr = this.authService.hasRole(['ROLE_ADMIN', 'ROLE_HR']);
    this.employeeId = this.authService.getEmployeeId();
    this.initForm();
    this.loadLeaveRequests();
    
    if (!this.isAdminOrHr && this.employeeId) {
      this.loadLeaveBalances();
    }
  }

  private initForm() {
    this.leaveForm = this.fb.group({
      leaveType: ['VACATION', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  loadLeaveRequests() {
    let url = 'http://localhost:8080/api/v1/leaves';
    if (!this.isAdminOrHr && this.employeeId) {
      url = `http://localhost:8080/api/v1/leaves/employee/${this.employeeId}`;
    }

    this.http.get<any[]>(url).subscribe({
      next: (data) => this.requests = data,
      error: () => {}
    });
  }

  loadLeaveBalances() {
    if (this.employeeId) {
      this.http.get<any>(`http://localhost:8080/api/v1/leaves/employee/${this.employeeId}/balances`).subscribe({
        next: (data) => this.balances = data,
        error: () => {}
      });
    }
  }

  toggleApplyForm() {
    this.showApplyForm = !this.showApplyForm;
    this.leaveForm.reset({
      leaveType: 'VACATION'
    });
  }

  closeApplyForm() {
    this.showApplyForm = false;
  }

  submitRequest() {
    if (this.leaveForm.invalid) return;

    this.http.post<any>('http://localhost:8080/api/v1/leaves/request', this.leaveForm.value).subscribe({
      next: () => {
        this.notificationService.showSuccess('Leave application submitted successfully.');
        this.showApplyForm = false;
        this.loadLeaveRequests();
        this.loadLeaveBalances();
      },
      error: (err) => {
        this.notificationService.showError(err.error?.message || 'Failed to submit leave application.');
      }
    });
  }

  resolveRequest(req: any, status: string) {
    if (status === 'APPROVED') {
      this.http.put<any>(`http://localhost:8080/api/v1/leaves/${req.id}/resolve?status=APPROVED`, {}).subscribe({
        next: () => {
          this.notificationService.showSuccess('Leave request approved.');
          this.loadLeaveRequests();
        },
        error: (err) => {
          this.notificationService.showError(err.error?.message || 'Failed to approve request.');
        }
      });
    } else {
      // Prompt for rejection reason
      this.resolvingRequest = req;
      this.rejectionText = '';
    }
  }

  confirmRejection() {
    if (!this.rejectionText.trim()) {
      this.notificationService.showError('Rejection reason is required.');
      return;
    }

    this.http.put<any>(`http://localhost:8080/api/v1/leaves/${this.resolvingRequest.id}/resolve?status=REJECTED&rejectionReason=${encodeURIComponent(this.rejectionText)}`, {}).subscribe({
      next: () => {
        this.notificationService.showSuccess('Leave request rejected.');
        this.resolvingRequest = null;
        this.loadLeaveRequests();
      },
      error: (err) => {
        this.notificationService.showError(err.error?.message || 'Failed to reject request.');
      }
    });
  }

  cancelResolve() {
    this.resolvingRequest = null;
  }
}
