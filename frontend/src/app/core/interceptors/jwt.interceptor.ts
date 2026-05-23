import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Functional HTTP Interceptor that attaches the JWT Access Token to all outgoing requests.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  // Automatically rewrite local development api calls to the live production server
  let reqUrl = req.url;
  if (reqUrl.startsWith('http://localhost:8080/api/v1')) {
    reqUrl = reqUrl.replace('http://localhost:8080/api/v1', 'https://hrms-backend-ba7y.onrender.com/api/v1');
  }

  req = req.clone({
    url: reqUrl
  });

  // Exclude login/refresh API calls if they don't need authentication header
  const isAuthUrl = req.url.includes('/auth/login') || req.url.includes('/auth/refresh-token');

  if (token && !isAuthUrl) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
