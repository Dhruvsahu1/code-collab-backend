/**
 * Dashboard E2E Tests
 * 
 * End-to-end tests for dashboard functionality:
 * - Project listing
 * - Search functionality
 * - Create project modal
 * - Navigation
 */

import { test, expect } from '@playwright/test';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Login first - in a real app, we'd use a fixture or helper
    await page.goto('/login');
    await page.getByLabel(/email or username/i).fill('test@example.com');
    await page.getByLabel(/password/i).fill('password123');
    await page.getByRole('button', { name: /sign in/i }).click();
  });

  test('should display dashboard heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /welcome back/i })).toBeVisible();
  });

  test('should display projects section', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /your projects/i })).toBeVisible();
  });

  test('should allow searching projects', async ({ page }) => {
    const searchInput = page.getByPlaceholder(/search projects/i);
    await searchInput.fill('test');
    
    // Projects should filter based on search
    await expect(searchInput).toHaveValue('test');
  });

  test('should open create project modal', async ({ page }) => {
    await page.getByRole('button', { name: /new project/i }).click();
    
    await expect(page.getByRole('heading', { name: /create new project/i })).toBeVisible();
  });

  test('should create a new project', async ({ page }) => {
    await page.getByRole('button', { name: /new project/i }).click();
    
    await page.getByLabel(/project name/i).fill('My New Project');
    await page.getByLabel(/description/i).fill('A test project');
    
    await page.getByRole('button', { name: /create project/i }).click();
    
    // Should redirect to editor
    await expect(page).toHaveURL(/\/editor\/\d+/);
  });

  test('should display logout button in sidebar', async ({ page }) => {
    // Sidebar should have logout option
    await expect(page.getByRole('button', { name: /sign out/i })).toBeVisible();
  });
});

test.describe('Protected Routes', () => {
  test('should redirect unauthenticated users from dashboard', async ({ page }) => {
    // Clear any existing auth
    await page.evaluate(() => localStorage.clear());
    
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/);
  });

  test('should redirect unauthenticated users from editor', async ({ page }) => {
    await page.evaluate(() => localStorage.clear());
    
    await page.goto('/editor/1');
    await expect(page).toHaveURL(/\/login/);
  });

  test('should allow authenticated users to access protected routes', async ({ page }) => {
    // Login first
    await page.goto('/login');
    await page.getByLabel(/email or username/i).fill('test@example.com');
    await page.getByLabel(/password/i).fill('password123');
    await page.getByRole('button', { name: /sign in/i }).click();
    
    // Now try to access editor
    await page.goto('/editor/1');
    // Should not redirect to login
    await expect(page).not.toHaveURL(/\/login/);
  });
});