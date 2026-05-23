import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = 'http://localhost:8080/api/v1';

  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.loadCurrentUser();
  }

  // Load current user from localStorage on initialization
  private loadCurrentUser() {
    const userJson = localStorage.getItem('currentUser');
    if (userJson) {
      try {
        this.currentUserSubject.next(JSON.parse(userJson));
      } catch (e) {
        this.logout();
      }
    }
  }

  public getAccessToken(): string | null {
    const user = this.currentUserValue;
    return user ? user.token : null;
  }

  public getRefreshToken(): string | null {
    const user = this.currentUserValue;
    return user ? user.refreshToken : null;
  }

  public get currentUserValue(): any {
    return this.currentUserSubject.value;
  }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/auth/login`, credentials).pipe(
      tap(user => {
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/auth/register`, userData);
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  // Check if current user possesses a required role
  hasRole(requiredRoles: string[]): boolean {
    const user = this.currentUserValue;
    if (!user || !user.roles) {
      return false;
    }
    return user.roles.some((role: string) => requiredRoles.includes(role));
  }

  getEmployeeId(): number | null {
    const user = this.currentUserValue;
    return user ? user.employeeId : null;
  }
}
