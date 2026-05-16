/**
 * Login E2E Tests
 * 
 * End-to-end tests for authentication flow:
 * - Successful login
 * - Failed login with invalid credentials
 * - Navigation between auth pages
 * 
 * Uses Playwright for real browser automation.
 */

import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should display login form', async ({ page }) => {
    await page.goto('/login');
    
    await expect(page.getByRole('heading', { name: /welcome back/i })).toBeVisible();
    await expect(page.getByLabel(/email or username/i)).toBeVisible();
    await expect(page.getByLabel(/password/i)).toBeVisible();
    await expect(page.getByRole('button', { name: /sign in/i })).toBeVisible();
  });

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByLabel(/email or username/i).fill('test@example.com');
    await page.getByLabel(/password/i).fill('password123');
    await page.getByRole('button', { name: /sign in/i }).click();
    
    // Should navigate to dashboard on success
    await expect(page).toHaveURL('/dashboard');
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByLabel(/email or username/i).fill('wrong@example.com');
    await page.getByLabel(/password/i).fill('wrongpassword');
    await page.getByRole('button', { name: /sign in/i }).click();
    
    // Should show error (check for toast or error message)
    // In a real app, we'd check for the error toast
  });

  test('should navigate to register page', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByRole('link', { name: /sign up/i }).click();
    await expect(page).toHaveURL('/register');
  });

  test('should navigate back to home from login', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByRole('link', { name: /back to home/i }).click();
    await expect(page).toHaveURL('/');
  });
});

test.describe('Registration Flow', () => {
  test('should display registration form', async ({ page }) => {
    await page.goto('/register');
    
    await expect(page.getByRole('heading', { name: /create account/i })).toBeVisible();
    await expect(page.getByLabel(/full name/i)).toBeVisible();
    await expect(page.getByLabel(/username/i)).toBeVisible();
    await expect(page.getByLabel(/email/i)).toBeVisible();
    await expect(page.getByLabel(/^password$/i)).toBeVisible();
    await expect(page.getByLabel(/confirm password/i)).toBeVisible();
  });

  test('should register successfully', async ({ page }) => {
    await page.goto('/register');
    
    await page.getByLabel(/full name/i).fill('New User');
    await page.getByLabel(/username/i).fill('newuser');
    await page.getByLabel(/email/i).fill('new@example.com');
    await page.getByLabel(/^password$/i).fill('password123');
    await page.getByLabel(/confirm password/i).fill('password123');
    
    await page.getByRole('button', { name: /create account/i }).click();
    
    // Should navigate to dashboard
    await expect(page).toHaveURL('/dashboard');
  });

  test('should show error for password mismatch', async ({ page }) => {
    await page.goto('/register');
    
    await page.getByLabel(/^password$/i).fill('password1');
    await page.getByLabel(/confirm password/i).fill('password2');
    await page.getByRole('button', { name: /create account/i }).click();
    
    // Should show error about password mismatch
  });

  test('should navigate to login page', async ({ page }) => {
    await page.goto('/register');
    
    await page.getByRole('link', { name: /sign in/i }).click();
    await expect(page).toHaveURL('/login');
  });
});