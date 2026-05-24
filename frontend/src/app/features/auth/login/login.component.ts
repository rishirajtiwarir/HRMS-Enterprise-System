import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { HttpClient } from '@angular/common/http';

// Angular Material Imports
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    MatIconModule
  ],
  template: `
    <div class="login-wrapper">
      <!-- Glow backdrops behind everything -->
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>

      <div class="split-container">
        <!-- Left Side: Interactive Brand Showcase & Demo helper -->
        <div class="brand-section">
          <div class="brand-header">
            <div class="logo-circle">
              <span class="material-icons brand-icon">corporate_fare</span>
            </div>
            <span class="brand-title">Enterprise HRMS</span>
          </div>

          <div class="brand-hero">
            <h1 class="hero-title">Automate Your <br><span class="gradient-text">Workforce Operations</span></h1>
            <p class="hero-subtitle">Experience a modern HR management platform. Track attendance, approve leave requests, and run payroll with absolute security and ease.</p>
          </div>

          <!-- Dashboard Mockup Card (Premium Glassmorphic Element) -->
          <div class="dashboard-mockup glass-effect">
            <div class="mockup-header">
              <div class="mockup-dots">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
              </div>
              <div class="mockup-search">Overview Dashboard</div>
            </div>
            <div class="mockup-content">
              <div class="mockup-grid">
                <div class="mockup-card">
                  <span class="material-icons mockup-icon text-blue">people</span>
                  <div class="mockup-info">
                    <span class="label">Total Employees</span>
                    <span class="val">142</span>
                  </div>
                </div>
                <div class="mockup-card">
                  <span class="material-icons mockup-icon text-purple">date_range</span>
                  <div class="mockup-info">
                    <span class="label">Pending Leaves</span>
                    <span class="val">5 Requests</span>
                  </div>
                </div>
                <div class="mockup-card">
                  <span class="material-icons mockup-icon text-pink">payments</span>
                  <div class="mockup-info">
                    <span class="label">Payroll Cycle</span>
                    <span class="val">98.4%</span>
                  </div>
                </div>
              </div>
              
              <!-- Subtle dynamic mockup chart -->
              <div class="mockup-chart-placeholder">
                <div class="chart-bar" style="height: 35%;"></div>
                <div class="chart-bar" style="height: 55%;"></div>
                <div class="chart-bar" style="height: 48%;"></div>
                <div class="chart-bar" style="height: 78%;"></div>
                <div class="chart-bar" style="height: 60%;"></div>
                <div class="chart-bar" style="height: 72%;"></div>
                <div class="chart-bar" style="height: 90%;"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Side: Clean Centered Login Card -->
        <div class="form-section">
          <div class="login-card glass-card">
            <div class="form-header">
              <h2>Welcome Back</h2>
              <p>Please sign in to access your HR workspace</p>
            </div>

            <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
              
              <div class="custom-form-group">
                <label for="username">Username</label>
                <div class="input-container">
                  <span class="material-icons input-icon">person</span>
                  <input 
                    type="text" 
                    id="username"
                    formControlName="username" 
                    placeholder="Enter your username" 
                    autocomplete="username">
                </div>
                <span class="error-msg" *ngIf="loginForm.get('username')?.touched && loginForm.get('username')?.hasError('required')">
                  Username is required
                </span>
              </div>

              <div class="custom-form-group">
                <label for="password">Password</label>
                <div class="input-container">
                  <span class="material-icons input-icon">lock</span>
                  <input 
                    [type]="hidePassword ? 'password' : 'text'" 
                    id="password"
                    formControlName="password" 
                    placeholder="Enter your password" 
                    autocomplete="current-password">
                  <button type="button" class="visibility-toggle" (click)="hidePassword = !hidePassword" aria-label="Toggle password visibility">
                    <span class="material-icons">{{hidePassword ? 'visibility_off' : 'visibility'}}</span>
                  </button>
                </div>
                <span class="error-msg" *ngIf="loginForm.get('password')?.touched && loginForm.get('password')?.hasError('required')">
                  Password is required
                </span>
              </div>

              <button type="submit" class="login-btn" [disabled]="loading || loginForm.invalid">
                <mat-spinner *ngIf="loading" diameter="22" class="spinner"></mat-spinner>
                <span *ngIf="!loading">Sign In</span>
              </button>
            </form>

            <!-- Quick Demo Credentials Drawer -->
            <div class="demo-credentials-panel">
              <div class="divider">
                <span class="line"></span>
                <span class="divider-text">Quick Demo Access</span>
                <span class="line"></span>
              </div>
              
              <div class="credentials-list">
                <button type="button" class="cred-chip" (click)="autofill('admin', 'admin123')">
                  <span class="material-icons chip-icon">admin_panel_settings</span>
                  <div class="chip-info">
                    <span class="role">Admin Account</span>
                    <span class="creds">User: admin | Pass: admin123</span>
                  </div>
                </button>
                
                <button type="button" class="cred-chip" (click)="autofill('john_doe', 'emp123')">
                  <span class="material-icons chip-icon">badge</span>
                  <div class="chip-info">
                    <span class="role">Employee Account</span>
                    <span class="creds">User: john_doe | Pass: emp123</span>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Floating Glassmorphic Support Button -->
      <button class="support-fab glass-effect" (click)="toggleSupportDrawer()" aria-label="Contact Support">
        <span class="material-icons fab-icon">support_agent</span>
        <span class="fab-text">Support & Feedback</span>
      </button>

      <!-- Glassmorphic Support Slide-out Drawer -->
      <div class="support-drawer glass-effect" [class.open]="showSupportDrawer">
        <div class="drawer-header">
          <div class="header-left">
            <span class="material-icons drawer-icon text-blue">forum</span>
            <h3>Support & Feedback</h3>
          </div>
          <button class="close-btn" (click)="toggleSupportDrawer()">
            <span class="material-icons">close</span>
          </button>
        </div>

        <p class="drawer-desc">Need assistance or want to leave feedback? Submit your ticket below and it will instantly notify the administrator via email and phone calls.</p>

        <form [formGroup]="supportForm" (ngSubmit)="submitSupport()" class="drawer-form">
          <div class="custom-form-group">
            <label for="support-name">Your Full Name</label>
            <div class="input-container">
              <span class="material-icons input-icon">person</span>
              <input type="text" id="support-name" formControlName="name" placeholder="Enter your full name">
            </div>
          </div>

          <div class="custom-form-group">
            <label for="support-email">Your Email Address</label>
            <div class="input-container">
              <span class="material-icons input-icon">email</span>
              <input type="email" id="support-email" formControlName="email" placeholder="Enter your email">
            </div>
          </div>

          <div class="custom-form-group">
            <label for="support-subject">Subject</label>
            <div class="input-container">
              <span class="material-icons input-icon">subject</span>
              <input type="text" id="support-subject" formControlName="subject" placeholder="What is this regarding?">
            </div>
          </div>

          <div class="custom-form-group">
            <label for="support-message">Detailed Message</label>
            <div class="input-container message-container">
              <span class="material-icons input-icon msg-icon">message</span>
              <textarea id="support-message" formControlName="message" placeholder="Type your message here..." rows="4"></textarea>
            </div>
          </div>

          <button type="submit" class="submit-support-btn" [disabled]="supportLoading || supportForm.invalid">
            <mat-spinner *ngIf="supportLoading" diameter="22" class="spinner"></mat-spinner>
            <span *ngIf="!supportLoading">Submit Ticket</span>
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      position: relative;
      width: 100vw;
      height: 100vh;
      background-color: hsl(224, 71%, 4%);
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    // Glowing Animated Blobs
    .blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(120px);
      opacity: 0.15;
      animation: drift 20s infinite alternate ease-in-out;
      pointer-events: none;
      z-index: 1;
    }
    .blob-1 {
      width: 600px;
      height: 600px;
      background-color: hsl(224, 95%, 60%); // Blue
      top: -150px;
      left: -150px;
      animation-delay: 0s;
    }
    .blob-2 {
      width: 600px;
      height: 600px;
      background-color: hsl(263, 90%, 65%); // Purple
      bottom: -150px;
      right: -150px;
      animation-delay: 4s;
    }
    .blob-3 {
      width: 400px;
      height: 400px;
      background-color: hsl(346, 85%, 60%); // Pink
      top: 30%;
      left: 45%;
      animation-delay: 8s;
    }

    @keyframes drift {
      0% { transform: translate(0, 0) scale(1); }
      50% { transform: translate(60px, -40px) scale(1.1); }
      100% { transform: translate(-40px, 60px) scale(0.95); }
    }

    .split-container {
      position: relative;
      z-index: 2;
      display: flex;
      width: 100%;
      height: 100%;
      max-width: 1600px;
      margin: 0 auto;
    }

    .brand-section {
      flex: 1.2;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 4rem;
      color: white;
      z-index: 2;
      position: relative;

      @media (max-width: 992px) {
        display: none; // Hide on tablet and mobile
      }
    }

    .brand-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;

      .logo-circle {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 44px;
        height: 44px;
        border-radius: 12px;
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%);
        border: 1px solid rgba(255, 255, 255, 0.15);
        box-shadow: 0 8px 32px rgba(59, 130, 246, 0.1);
      }

      .brand-icon {
        font-size: 1.5rem;
        background: linear-gradient(135deg, #3b82f6, #8b5cf6);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .brand-title {
        font-size: 1.25rem;
        font-weight: 700;
        letter-spacing: -0.5px;
        background: linear-gradient(135deg, #ffffff 0%, #cbd5e1 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }

    .brand-hero {
      margin-top: auto;
      margin-bottom: 2rem;
      max-width: 580px;

      .hero-title {
        font-size: 3rem;
        font-weight: 800;
        line-height: 1.15;
        letter-spacing: -1.5px;
        margin-bottom: 1.5rem;

        .gradient-text {
          background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          filter: drop-shadow(0 2px 8px rgba(59, 130, 246, 0.25));
        }
      }

      .hero-subtitle {
        font-size: 1.1rem;
        color: hsl(215, 20%, 75%);
        line-height: 1.6;
      }
    }

    // Dashboard Mockup Card
    .dashboard-mockup {
      background: rgba(15, 23, 42, 0.45);
      border: 1px solid rgba(255, 255, 255, 0.08);
      backdrop-filter: blur(16px);
      border-radius: 16px;
      padding: 1.5rem;
      margin-top: auto;
      box-shadow: 0 30px 60px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.1);
      max-width: 600px;
      animation: floatUp 1.2s ease-out;

      .mockup-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
        padding-bottom: 0.75rem;
        margin-bottom: 1rem;
      }

      .mockup-dots {
        display: flex;
        gap: 6px;
        
        .dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          &:nth-child(1) { background-color: #ef4444; }
          &:nth-child(2) { background-color: #eab308; }
          &:nth-child(3) { background-color: #22c55e; }
        }
      }

      .mockup-search {
        font-size: 0.75rem;
        color: rgba(255, 255, 255, 0.4);
        background: rgba(255, 255, 255, 0.05);
        padding: 4px 16px;
        border-radius: 20px;
        border: 1px solid rgba(255, 255, 255, 0.05);
      }

      .mockup-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 1rem;
        margin-bottom: 1.25rem;
      }

      .mockup-card {
        background: rgba(255, 255, 255, 0.03);
        border: 1px solid rgba(255, 255, 255, 0.04);
        border-radius: 10px;
        padding: 0.75rem;
        display: flex;
        align-items: center;
        gap: 0.5rem;

        .mockup-icon {
          font-size: 1.25rem;
          padding: 6px;
          border-radius: 8px;
          
          &.text-blue { background-color: rgba(59, 130, 246, 0.1); color: #3b82f6; }
          &.text-purple { background-color: rgba(139, 92, 246, 0.1); color: #8b5cf6; }
          &.text-pink { background-color: rgba(236, 72, 153, 0.1); color: #ec4899; }
        }

        .mockup-info {
          display: flex;
          flex-direction: column;
          
          .label {
            font-size: 0.65rem;
            color: rgba(255, 255, 255, 0.40);
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }
          
          .val {
            font-size: 0.85rem;
            font-weight: 700;
            color: white;
            margin-top: 1px;
          }
        }
      }

      .mockup-chart-placeholder {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        height: 60px;
        padding: 0 0.5rem;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);

        .chart-bar {
          width: 10%;
          border-radius: 4px 4px 0 0;
          background: linear-gradient(180deg, rgba(59, 130, 246, 0.6) 0%, rgba(59, 130, 246, 0.1) 100%);
          &:nth-child(even) {
            background: linear-gradient(180deg, rgba(139, 92, 246, 0.6) 0%, rgba(139, 92, 246, 0.1) 100%);
          }
          &:hover {
            opacity: 0.9;
          }
        }
      }
    }

    @keyframes floatUp {
      from { transform: translateY(30px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .form-section {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      z-index: 2;

      @media (max-width: 992px) {
        width: 100%;
        padding: 1.5rem;
      }
    }

    .login-card {
      width: 100%;
      max-width: 440px;
      padding: 3rem 2.5rem;
      border: 1px solid rgba(255, 255, 255, 0.08);
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6);
      background: rgba(15, 23, 42, 0.75);
      border-radius: 24px;
      backdrop-filter: blur(24px);
      -webkit-backdrop-filter: blur(24px);
      animation: fadeIn 0.8s ease-out;

      @media (max-width: 480px) {
        padding: 2rem 1.5rem;
      }
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: scale(0.96); }
      to { opacity: 1; transform: scale(1); }
    }

    .form-header {
      text-align: center;
      margin-bottom: 2.25rem;

      h2 {
        font-size: 2rem;
        font-weight: 700;
        color: white;
        letter-spacing: -0.75px;
        margin-bottom: 0.5rem;
      }

      p {
        font-size: 0.9rem;
        color: hsl(215, 20%, 65%);
      }
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .custom-form-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      label {
        font-size: 0.85rem;
        font-weight: 600;
        color: hsl(215, 20%, 75%);
        text-align: left;
        letter-spacing: 0.3px;
      }
    }

    .input-container {
      position: relative;
      display: flex;
      align-items: center;
      width: 100%;

      .input-icon {
        position: absolute;
        left: 14px;
        color: hsl(215, 15%, 55%);
        font-size: 1.25rem;
        pointer-events: none;
        transition: color 0.2s ease;
      }

      input {
        width: 100%;
        padding: 0.85rem 1rem 0.85rem 2.75rem;
        border-radius: 12px;
        border: 1px solid rgba(255, 255, 255, 0.1);
        background-color: rgba(30, 41, 59, 0.5);
        color: white;
        font-size: 0.95rem;
        outline: none;
        transition: all 0.2s ease;

        &::placeholder {
          color: rgba(255, 255, 255, 0.2);
        }

        &:focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
          background-color: rgba(30, 41, 59, 0.7);

          & + .input-icon {
            color: #3b82f6;
          }
        }
      }
    }

    .visibility-toggle {
      position: absolute;
      right: 14px;
      background: transparent;
      border: none;
      color: hsl(215, 15%, 60%);
      cursor: pointer;
      display: flex;
      align-items: center;
      padding: 0;

      &:hover {
        color: white;
      }
      span {
        font-size: 1.25rem;
      }
    }

    .error-msg {
      color: hsl(346, 84%, 65%);
      font-size: 0.75rem;
      text-align: left;
      margin-top: 0.15rem;
    }

    .login-btn {
      width: 100%;
      height: 48px;
      margin-top: 0.5rem;
      font-size: 1rem;
      font-weight: 600;
      border-radius: 12px;
      border: none;
      background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);

      &:hover:not([disabled]) {
        box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
        transform: translateY(-1px);
      }

      &:active:not([disabled]) {
        transform: translateY(0);
      }

      &[disabled] {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .spinner {
      margin: 0 auto;
      ::ng-deep circle {
        stroke: white !important;
      }
    }
    
    // Demo credentials divider
    .demo-credentials-panel {
      margin-top: 2.25rem;
      text-align: left;
    }

    .divider {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.25rem;

      .line {
        flex: 1;
        height: 1px;
        background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.1) 100%);
        
        &:last-child {
          background: linear-gradient(90deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
        }
      }

      .divider-text {
        font-size: 0.725rem;
        font-weight: 600;
        color: hsl(215, 20%, 55%);
        text-transform: uppercase;
        letter-spacing: 1px;
        white-space: nowrap;
      }
    }

    .credentials-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .cred-chip {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      background-color: rgba(30, 41, 59, 0.4);
      border: 1px solid rgba(255, 255, 255, 0.05);
      color: hsl(215, 20%, 85%);
      padding: 0.65rem 0.85rem;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s ease;
      width: 100%;

      .chip-icon {
        color: #3b82f6;
        font-size: 1.25rem;
        background-color: rgba(59, 130, 246, 0.1);
        padding: 6px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .chip-info {
        display: flex;
        flex-direction: column;
        text-align: left;
        gap: 0.1rem;

        .role {
          font-size: 0.8rem;
          font-weight: 600;
          color: white;
        }
        .creds {
          font-size: 0.7rem;
          color: hsl(215, 20%, 60%);
        }
      }

      &:hover {
        background-color: rgba(59, 130, 246, 0.08);
        border-color: rgba(59, 130, 246, 0.2);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }

    /* Floating Support Button */
    .support-fab {
      position: absolute;
      bottom: 2rem;
      right: 2rem;
      z-index: 99;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.25rem;
      border-radius: 30px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(30, 41, 59, 0.6);
      backdrop-filter: blur(12px);
      color: white;
      cursor: pointer;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.1);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: rgba(59, 130, 246, 0.2);
        border-color: rgba(59, 130, 246, 0.4);
        transform: translateY(-2px);
        box-shadow: 0 12px 40px rgba(59, 130, 246, 0.25);
      }

      .fab-icon {
        font-size: 1.25rem;
        color: #3b82f6;
      }

      .fab-text {
        font-size: 0.85rem;
        font-weight: 600;
        letter-spacing: 0.3px;
      }

      @media (max-width: 480px) {
        bottom: 1rem;
        right: 1rem;
        padding: 0.65rem 1rem;
        .fab-text { display: none; }
      }
    }

    /* Support Slide-out Drawer */
    .support-drawer {
      position: absolute;
      top: 0;
      right: -420px;
      width: 100%;
      max-width: 400px;
      height: 100%;
      z-index: 100;
      background: rgba(15, 23, 42, 0.85);
      border-left: 1px solid rgba(255, 255, 255, 0.08);
      backdrop-filter: blur(32px);
      -webkit-backdrop-filter: blur(32px);
      box-shadow: -20px 0 50px rgba(0, 0, 0, 0.5);
      padding: 2.5rem;
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      transition: right 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      overflow-y: auto;

      &.open {
        right: 0;
      }

      .drawer-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
        padding-bottom: 1rem;

        .header-left {
          display: flex;
          align-items: center;
          gap: 0.65rem;

          .drawer-icon {
            font-size: 1.5rem;
            color: #3b82f6;
          }

          h3 {
            font-size: 1.25rem;
            font-weight: 700;
            color: white;
            margin: 0;
            letter-spacing: -0.3px;
          }
        }

        .close-btn {
          background: transparent;
          border: none;
          color: rgba(255, 255, 255, 0.4);
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 4px;
          border-radius: 50%;
          transition: all 0.2s ease;

          &:hover {
            color: white;
            background: rgba(255, 255, 255, 0.05);
          }
        }
      }

      .drawer-desc {
        font-size: 0.85rem;
        color: hsl(215, 15%, 70%);
        line-height: 1.5;
        margin: 0;
      }

      .drawer-form {
        display: flex;
        flex-direction: column;
        gap: 1.2rem;
      }

      .message-container {
        textarea {
          width: 100%;
          padding: 0.85rem 1rem 0.85rem 2.75rem;
          border-radius: 12px;
          border: 1px solid rgba(255, 255, 255, 0.1);
          background-color: rgba(30, 41, 59, 0.5);
          color: white;
          font-size: 0.95rem;
          outline: none;
          resize: none;
          transition: all 0.2s ease;

          &::placeholder {
            color: rgba(255, 255, 255, 0.2);
          }

          &:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
            background-color: rgba(30, 41, 59, 0.7);
          }
        }

        .msg-icon {
          top: 14px;
        }
      }

      .submit-support-btn {
        width: 100%;
        height: 46px;
        font-size: 0.95rem;
        font-weight: 600;
        border-radius: 12px;
        border: none;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);

        &:hover:not([disabled]) {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
          box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
          transform: translateY(-1px);
        }

        &[disabled] {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }

      @media (max-width: 480px) {
        max-width: 100%;
        padding: 1.5rem;
      }
    }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private http = inject(HttpClient);

  loginForm: FormGroup = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  supportForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    subject: ['', [Validators.required]],
    message: ['', [Validators.required]]
  });

  loading = false;
  hidePassword = true;
  showSupportDrawer = false;
  supportLoading = false;

  toggleSupportDrawer() {
    this.showSupportDrawer = !this.showSupportDrawer;
  }

  submitSupport() {
    if (this.supportForm.invalid) {
      return;
    }
    this.supportLoading = true;
    this.http.post('http://localhost:8080/api/v1/feedback', this.supportForm.value).subscribe({
      next: () => {
        this.supportLoading = false;
        this.notificationService.showSuccess('Support ticket submitted successfully!');
        this.supportForm.reset();
        this.showSupportDrawer = false;
      },
      error: (err) => {
        this.supportLoading = false;
        this.notificationService.showError('Failed to submit support ticket. Please try again.');
      }
    });
  }

  autofill(user: string, pass: string) {
    this.loginForm.setValue({
      username: user,
      password: pass
    });
    this.loginForm.markAllAsTouched();
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.notificationService.showSuccess('Welcome back!');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        const errorMessage = err.error?.message || 'Invalid username or password!';
        this.notificationService.showError(errorMessage);
      }
    });
  }
}
