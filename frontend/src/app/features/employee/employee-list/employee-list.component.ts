import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

// Angular Material Imports
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    <div class="employee-wrapper">
      <div class="header-actions">
        <h1>Employee Directory</h1>
        <button *ngIf="isAdminOrHr" mat-flat-button color="primary" class="btn-primary" (click)="openCreateForm()">
          <mat-icon>person_add</mat-icon> Add Employee
        </button>
      </div>

      <!-- Add / Edit Drawer Panel -->
      <div *ngIf="showForm" class="glass-card form-drawer">
        <h3>{{ isEditMode ? 'Edit Employee Details' : 'Register New Employee' }}</h3>
        
        <form [formGroup]="empForm" (ngSubmit)="saveEmployee()" class="grid-form">
          <div class="custom-form-group">
            <label>First Name</label>
            <input type="text" formControlName="firstName" placeholder="First name">
          </div>
          <div class="custom-form-group">
            <label>Last Name</label>
            <input type="text" formControlName="lastName" placeholder="Last name">
          </div>
          <div class="custom-form-group">
            <label>Email Address</label>
            <input type="email" formControlName="email" placeholder="email@enterprise.com">
          </div>
          <div class="custom-form-group">
            <label>Phone Number</label>
            <input type="text" formControlName="phone" placeholder="+1-555-xxxx">
          </div>
          <div class="custom-form-group">
            <label>Joining Date</label>
            <input type="date" formControlName="joiningDate">
          </div>
          <div class="custom-form-group">
            <label>Designation</label>
            <input type="text" formControlName="designation" placeholder="e.g. Senior Architect">
          </div>
          <div class="custom-form-group">
            <label>Salary ($)</label>
            <input type="number" formControlName="salary" placeholder="Yearly base salary">
          </div>
          <div class="custom-form-group">
            <label>Department</label>
            <select formControlName="departmentId">
              <option [value]="null">Select Department</option>
              <option *ngFor="let dept of departments" [value]="dept.id">{{ dept.name }}</option>
            </select>
          </div>

          <!-- Account details for registration (only visible when creating new) -->
          <ng-container *ngIf="!isEditMode">
            <div class="custom-form-group">
              <label>System Username</label>
              <input type="text" formControlName="username" placeholder="Login username">
            </div>
            <div class="custom-form-group">
              <label>System Password</label>
              <input type="password" formControlName="password" placeholder="Min 6 characters">
            </div>
            <div class="custom-form-group">
              <label>System Role</label>
              <select formControlName="role">
                <option value="employee">Employee</option>
                <option value="hr">HR Lead</option>
                <option value="admin">Administrator</option>
              </select>
            </div>
          </ng-container>

          <div class="form-actions-row">
            <button type="button" class="btn btn-secondary" (click)="closeForm()">Cancel</button>
            <button type="submit" class="btn btn-primary" [disabled]="empForm.invalid">
              {{ isEditMode ? 'Update Changes' : 'Register' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Filters Row -->
      <div class="glass-card filter-card">
        <span class="material-symbols-outlined search-icon">search</span>
        <input type="text" [(ngModel)]="searchText" (input)="onSearchChange()" placeholder="Search employees by name or email..." class="search-input">
      </div>

      <!-- Data Table -->
      <div class="custom-table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Designation</th>
              <th>Department</th>
              <th>Salary</th>
              <th>Status</th>
              <th *ngIf="isAdminOrHr" style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let emp of employees">
              <td>{{ emp.id }}</td>
              <td class="bold-text">{{ emp.firstName }} {{ emp.lastName }}</td>
              <td>{{ emp.email }}</td>
              <td>{{ emp.designation }}</td>
              <td>{{ emp.departmentName || 'N/A' }}</td>
              <td>\${{ emp.salary | number:'1.2-2' }}</td>
              <td>
                <span class="badge" [ngClass]="{
                  'badge-success': emp.status === 'ACTIVE',
                  'badge-danger': emp.status === 'TERMINATED',
                  'badge-warning': emp.status === 'ON_LEAVE'
                }">{{ emp.status }}</span>
              </td>
              <td *ngIf="isAdminOrHr" class="actions-cell">
                <button class="action-btn edit-btn" (click)="openEditForm(emp)" title="Edit profile">
                  <mat-icon>edit</mat-icon>
                </button>
                <button class="action-btn delete-btn" (click)="deleteEmployee(emp.id)" title="Deactivate">
                  <mat-icon>person_remove</mat-icon>
                </button>
              </td>
            </tr>
            <tr *ngIf="employees.length === 0">
              <td colspan="8" class="no-data">No employees found.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <mat-paginator 
        [length]="totalElements"
        [pageSize]="pageSize"
        [pageSizeOptions]="[5, 10, 20]"
        (page)="onPageChange($event)"
        class="custom-paginator">
      </mat-paginator>
    </div>
  `,
  styles: [`
    .employee-wrapper {
      display: flex;
      flex-direction: column;
      gap: 1rem;
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
    .filter-card {
      padding: 0.75rem 1.25rem;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.5rem;

      .search-icon {
        color: var(--text-secondary);
      }
      .search-input {
        border: none;
        background: transparent;
        color: var(--text-primary);
        font-size: 1rem;
        outline: none;
        width: 100%;
      }
    }
    .form-drawer {
      animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
      h3 {
        margin-bottom: 1.5rem;
        font-size: 1.2rem;
        font-weight: 600;
      }
    }
    .grid-form {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1.25rem;
    }
    .form-actions-row {
      grid-column: 1 / -1;
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1rem;
    }
    .bold-text {
      font-weight: 600;
    }
    .actions-cell {
      text-align: right;
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
    }
    .action-btn {
      background: transparent;
      border: none;
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;
      display: inline-flex;
      align-items: center;

      &:hover {
        background-color: var(--surface-secondary);
      }
      mat-icon {
        font-size: 1.25rem;
        width: 20px;
        height: 20px;
      }
    }
    .edit-btn mat-icon {
      color: var(--primary);
    }
    .delete-btn mat-icon {
      color: var(--danger);
    }
    .no-data {
      text-align: center;
      color: var(--text-secondary);
      padding: 2rem !important;
    }
    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-10px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class EmployeeListComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  employees: any[] = [];
  departments: any[] = [];
  isAdminOrHr = false;

  // Search & Pagination states
  searchText = '';
  pageIndex = 0;
  pageSize = 10;
  totalElements = 0;

  // Form states
  showForm = false;
  isEditMode = false;
  editingId: number | null = null;
  empForm!: FormGroup;

  ngOnInit() {
    this.isAdminOrHr = this.authService.hasRole(['ROLE_ADMIN', 'ROLE_HR']);
    this.initForm();
    this.loadEmployees();
    this.loadDepartments();
  }

  private initForm() {
    // Shared form layout: RegisterRequest includes employee + user fields
    this.empForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      joiningDate: [new Date().toISOString().substring(0, 10), Validators.required],
      designation: ['', Validators.required],
      salary: [50000.0, [Validators.required, Validators.min(0)]],
      departmentId: [null, Validators.required],
      
      // User specific fields (conditional validations handled programmatically)
      username: [''],
      password: [''],
      role: ['employee']
    });
  }

  loadEmployees() {
    this.http.get<any>(`http://localhost:8080/api/v1/employees?search=${this.searchText}&page=${this.pageIndex}&size=${this.pageSize}`).subscribe({
      next: (data) => {
        this.employees = data.content;
        this.totalElements = data.totalElements;
      },
      error: () => {}
    });
  }

  loadDepartments() {
    this.http.get<any[]>('http://localhost:8080/api/v1/departments').subscribe({
      next: (data) => this.departments = data,
      error: () => {}
    });
  }

  onSearchChange() {
    this.pageIndex = 0;
    this.loadEmployees();
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmployees();
  }

  openCreateForm() {
    this.isEditMode = false;
    this.editingId = null;
    this.empForm.reset({
      joiningDate: new Date().toISOString().substring(0, 10),
      salary: 50000.0,
      departmentId: null,
      role: 'employee'
    });
    
    // Add validations for user fields
    this.empForm.get('username')?.setValidators([Validators.required, Validators.minLength(3)]);
    this.empForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.empForm.get('username')?.updateValueAndValidity();
    this.empForm.get('password')?.updateValueAndValidity();
    
    this.showForm = true;
  }

  openEditForm(emp: any) {
    this.isEditMode = true;
    this.editingId = emp.id;
    
    this.empForm.patchValue({
      firstName: emp.firstName,
      lastName: emp.lastName,
      email: emp.email,
      phone: emp.phone,
      joiningDate: emp.joiningDate,
      designation: emp.designation,
      salary: emp.salary,
      departmentId: emp.departmentId
    });

    // Clear validations for user registration fields in edit mode
    this.empForm.get('username')?.clearValidators();
    this.empForm.get('password')?.clearValidators();
    this.empForm.get('username')?.updateValueAndValidity();
    this.empForm.get('password')?.updateValueAndValidity();

    this.showForm = true;
  }

  closeForm() {
    this.showForm = false;
  }

  saveEmployee() {
    if (this.empForm.invalid) return;

    if (this.isEditMode && this.editingId) {
      // Perform employee update (PUT)
      this.http.put<any>(`http://localhost:8080/api/v1/employees/${this.editingId}`, this.empForm.value).subscribe({
        next: () => {
          this.notificationService.showSuccess('Employee profile updated successfully.');
          this.closeForm();
          this.loadEmployees();
        },
        error: (err) => {
          this.notificationService.showError(err.error?.message || 'Failed to update employee details.');
        }
      });
    } else {
      // Perform employee registration (POST) - which creates User credentials as well
      this.http.post<any>('http://localhost:8080/api/v1/auth/register', this.empForm.value).subscribe({
        next: () => {
          this.notificationService.showSuccess('Employee account registered successfully!');
          this.closeForm();
          this.loadEmployees();
        },
        error: (err) => {
          this.notificationService.showError(err.error?.message || 'Registration failed.');
        }
      });
    }
  }

  deleteEmployee(id: number) {
    if (confirm('Are you sure you want to deactivate this employee? This will disable their login credentials.')) {
      this.http.delete<any>(`http://localhost:8080/api/v1/employees/${id}`).subscribe({
        next: () => {
          this.notificationService.showSuccess('Employee deactivated successfully.');
          this.loadEmployees();
        },
        error: (err) => {
          this.notificationService.showError(err.error?.message || 'Failed to deactivate employee.');
        }
      });
    }
  }
}
