import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'employees',
        loadComponent: () => import('./features/employee/employee-list/employee-list.component').then(m => m.EmployeeListComponent)
      },
      {
        path: 'leaves',
        loadComponent: () => import('./features/leave-management/leave-list/leave-list.component').then(m => m.LeaveListComponent)
      },
      {
        path: 'attendance',
        loadComponent: () => import('./features/attendance/attendance.component').then(m => m.AttendanceComponent)
      },
      {
        path: 'payroll',
        loadComponent: () => import('./features/payroll/payroll.component').then(m => m.PayrollComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
