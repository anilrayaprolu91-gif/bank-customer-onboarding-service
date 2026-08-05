# React + TypeScript UI Implementation Plan
## Bank Customer Onboarding Service

**Document Purpose:** Complete blueprint for integrating a React+TS frontend into Spring Boot backend (monolith), with future separation strategy  
**Target Stack:** React 18+, TypeScript strict mode, Vite bundler, Tailwind CSS, TanStack Query, Zod validation  
**Timeline:** 3-4 weeks to production-ready UI (phased delivery)  

---

## 📊 EXECUTIVE SUMMARY

| Aspect | Details |
|--------|---------|
| **Current State** | Spring Boot REST API (standalone), no UI |
| **Target State** | React+TS UI served from Spring Boot (monolith) |
| **Future State** | Separate frontend & backend repos + independent deployment |
| **Build Time** | 3-4 weeks (phased: Phase 1: 1 week, Phase 2: 1 week, Phase 3: 1-2 weeks) |
| **Key Features** | Customer onboarding form, customer search, account management, real-time validation, error handling |
| **Code Quality** | TypeScript strict, component testing (Vitest), E2E testing (Playwright), accessibility (WCAG 2.1) |

---

## 🏗️ PHASE 1: MONOLITH SETUP (Week 1)

### 1.1 Project Structure (Monolith)

```
bank-customer-onboarding-service/
├── src/
│   ├── main/
│   │   ├── java/com/bank/onboarding/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── ...
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── static/  ← React app will be built here
│   │           └── index.html (replaced by build)
│   ├── test/
│   ├── componentTest/
│   └── apiTest/
│
├── frontend/  ⭐ NEW: React + TypeScript
│   ├── src/
│   │   ├── pages/          → Page components
│   │   ├── components/     → Reusable components
│   │   ├── hooks/          → Custom hooks
│   │   ├── services/       → API clients
│   │   ├── store/          → TanStack Query + Zustand
│   │   ├── types/          → TypeScript types (auto-generated from backend)
│   │   ├── utils/          → Helpers, validators
│   │   ├── styles/         → Tailwind CSS
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── public/
│   ├── vite.config.ts      → Vite bundler config
│   ├── tsconfig.json       → Strict TypeScript
│   ├── tailwind.config.js
│   ├── package.json
│   └── vitest.config.ts    → Unit test config
│
├── build.gradle            → Updated with frontend build task
├── Dockerfile              → Updated to build frontend first
├── docker-compose.yml      → No changes (backend runs on 8080)
│
└── docs/
    ├── FRONTEND_SETUP.md   ← You create this
    ├── API_CLIENT_GENERATION.md
    └── DEPLOYMENT_MIGRATION.md
```

### 1.2 Create Frontend Folder & Package.json

```bash
mkdir frontend
cd frontend
npm init -y
```

```json
{
  "name": "bank-onboarding-ui",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix",
    "type-check": "tsc --noEmit",
    "api:generate": "openapi-generator-cli generate -i http://localhost:8080/api-docs -g typescript-fetch -o src/api"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "@tanstack/react-query": "^5.28.0",
    "zustand": "^4.4.0",
    "axios": "^1.6.0",
    "zod": "^3.22.0",
    "@hookform/resolvers": "^3.3.0",
    "react-hook-form": "^7.48.0",
    "tailwindcss": "^3.4.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "lucide-react": "^0.292.0"
  },
  "devDependencies": {
    "typescript": "^5.3.0",
    "vite": "^5.0.0",
    "@vitejs/plugin-react": "^4.2.0",
    "vitest": "^1.0.0",
    "@testing-library/react": "^14.1.0",
    "@testing-library/jest-dom": "^6.1.0",
    "eslint": "^8.55.0",
    "eslint-config-prettier": "^9.1.0",
    "prettier": "^3.1.0",
    "@typescript-eslint/parser": "^6.15.0",
    "@typescript-eslint/eslint-plugin": "^6.15.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0"
  }
}
```

### 1.3 Create TypeScript Configuration

**frontend/tsconfig.json:**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "strictFunctionTypes": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@pages/*": ["./src/pages/*"],
      "@components/*": ["./src/components/*"],
      "@hooks/*": ["./src/hooks/*"],
      "@services/*": ["./src/services/*"],
      "@store/*": ["./src/store/*"],
      "@types/*": ["./src/types/*"],
      "@utils/*": ["./src/utils/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 1.4 Create Vite Configuration

**frontend/vite.config.ts:**
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // Proxy API calls to Spring Boot backend during development
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      },
    },
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    sourcemap: true,
  },
})
```

### 1.5 Create Tailwind Configuration

**frontend/tailwind.config.js:**
```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#2563eb',
        success: '#10b981',
        warning: '#f59e0b',
        danger: '#ef4444',
      },
    },
  },
  plugins: [],
}
```

### 1.6 Create Folder Structure & Index Files

```bash
cd frontend/src

# Create directories
mkdir -p pages components hooks services store types utils styles

# Create index files for barrel exports
touch pages/index.ts
touch components/index.ts
touch hooks/index.ts
touch services/index.ts
touch store/index.ts
touch types/index.ts
touch utils/index.ts
```

---

## 🎨 PHASE 2: CORE COMPONENTS & SERVICES (Week 2)

### 2.1 TypeScript Types (from backend API)

**frontend/src/types/api.types.ts:**
```typescript
// Auto-generated or manually typed from your OpenAPI spec
// Run: npm run api:generate (if using OpenAPI Generator)

export interface PersonalInfo {
  firstName: string
  lastName: string
  dateOfBirth: string // YYYY-MM-DD
  email: string
  phoneNumber: string // +E.164 format
  nationality: string // ISO 2-char code
  taxIdentificationNumber: string
}

export interface Address {
  addressType: 'HOME' | 'WORK' | 'MAILING'
  street: string
  city: string
  state: string
  postalCode: string
  country: string
  isPrimary: boolean
}

export interface KycDocument {
  documentType: 'PASSPORT' | 'DRIVERS_LICENSE' | 'NATIONAL_ID'
  documentNumber: string
  issuingAuthority: string
  issuingCountry: string
  issueDate: string // YYYY-MM-DD
  expiryDate: string // YYYY-MM-DD
}

export interface RiskProfile {
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  riskScore: number // 0-100
  assessedBy: string
  factors: {
    factorName: string
    factorDescription: string
    weight: number
  }[]
}

export interface CustomerOnboardingRequest {
  personalInfo: PersonalInfo
  addresses: Address[]
  kycDocuments: KycDocument[]
  riskProfile: RiskProfile
  initialAccount?: {
    accountType: 'SAVINGS' | 'CHECKING' | 'TERM_DEPOSIT'
    currency: string // ISO 4217
    initialDeposit: number
    productCode: string
  }
}

export interface CustomerResponse {
  id: string // UUID
  customerNumber: string // CUST-XXXX-XXXXXXXX
  personalInfo: PersonalInfo
  addresses: Address[]
  kycDocuments: KycDocument[]
  riskProfile: RiskProfile
  customerStatus: 'PENDING_VERIFICATION' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED'
  createdAt: string // ISO 8601
  updatedAt: string // ISO 8601
}

export interface AccountResponse {
  id: string // UUID
  customerId: string
  accountType: 'SAVINGS' | 'CHECKING' | 'TERM_DEPOSIT'
  accountNumber: string
  currency: string
  balance: number
  accountStatus: 'OPEN' | 'FROZEN' | 'CLOSED'
  createdAt: string
  updatedAt: string
}

export interface OnboardingStatusResponse {
  customerId: string
  currentStatus: string
  completedSteps: string[]
  remainingSteps: string[]
  eligibleForServices: boolean
  lastUpdated: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface ApiError {
  title: string
  detail: string
  status: number
  type: string
  violations?: {
    field: string
    message: string
  }[]
}
```

### 2.2 API Client Service

**frontend/src/services/api.client.ts:**
```typescript
import axios, { AxiosInstance, AxiosError } from 'axios'
import { ApiError } from '@types/api.types'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'

class ApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      timeout: 30000,
    })

    // Add correlation ID to every request
    this.client.interceptors.request.use((config) => {
      config.headers['X-Request-ID'] = this.generateCorrelationId()
      return config
    })

    // Handle errors consistently
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ApiError>) => {
        console.error('API Error:', error.response?.data)
        return Promise.reject(error)
      }
    )
  }

  private generateCorrelationId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }

  async get<T>(url: string, config = {}) {
    return this.client.get<T>(url, config)
  }

  async post<T>(url: string, data: unknown, config = {}) {
    return this.client.post<T>(url, data, config)
  }

  async patch<T>(url: string, data: unknown, config = {}) {
    return this.client.patch<T>(url, data, config)
  }

  async delete<T>(url: string, config = {}) {
    return this.client.delete<T>(url, config)
  }
}

export const apiClient = new ApiClient()
```

### 2.3 Customer API Service

**frontend/src/services/customer.service.ts:**
```typescript
import { apiClient } from './api.client'
import { ApiResponse, CustomerOnboardingRequest, CustomerResponse, OnboardingStatusResponse } from '@types/api.types'

export const customerService = {
  // Onboard a new customer
  async onboardCustomer(request: CustomerOnboardingRequest) {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      '/v1/customers/onboard',
      request
    )
    return response.data.data
  },

  // Get all customers (paginated)
  async getAllCustomers(page = 0, size = 20) {
    const response = await apiClient.get<ApiResponse<any>>(
      `/v1/customers?page=${page}&size=${size}`
    )
    return response.data.data
  },

  // Get customer by ID
  async getCustomerById(customerId: string) {
    const response = await apiClient.get<ApiResponse<CustomerResponse>>(
      `/v1/customers/${customerId}`
    )
    return response.data.data
  },

  // Get customer by customer number
  async getCustomerByNumber(customerNumber: string) {
    const response = await apiClient.get<ApiResponse<CustomerResponse>>(
      `/v1/customers/number/${customerNumber}`
    )
    return response.data.data
  },

  // Update customer status
  async updateCustomerStatus(customerId: string, status: string) {
    const response = await apiClient.patch<ApiResponse<CustomerResponse>>(
      `/v1/customers/${customerId}/status`,
      { customerStatus: status }
    )
    return response.data.data
  },

  // Get onboarding status
  async getOnboardingStatus(customerId: string) {
    const response = await apiClient.get<ApiResponse<OnboardingStatusResponse>>(
      `/v1/customers/${customerId}/onboarding-status`
    )
    return response.data.data
  },
}
```

### 2.4 Account API Service

**frontend/src/services/account.service.ts:**
```typescript
import { apiClient } from './api.client'
import { ApiResponse, AccountResponse, AccountCreationRequest } from '@types/api.types'

export const accountService = {
  // Create account for customer
  async createAccount(customerId: string, request: AccountCreationRequest) {
    const response = await apiClient.post<ApiResponse<AccountResponse>>(
      `/v1/customers/${customerId}/accounts`,
      request
    )
    return response.data.data
  },

  // Get all accounts for customer
  async getAccountsByCustomer(customerId: string) {
    const response = await apiClient.get<ApiResponse<AccountResponse[]>>(
      `/v1/customers/${customerId}/accounts`
    )
    return response.data.data
  },

  // Get single account
  async getAccountById(accountId: string) {
    const response = await apiClient.get<ApiResponse<AccountResponse>>(
      `/v1/accounts/${accountId}`
    )
    return response.data.data
  },

  // Close account
  async closeAccount(accountId: string) {
    const response = await apiClient.delete<ApiResponse<AccountResponse>>(
      `/v1/accounts/${accountId}`
    )
    return response.data.data
  },
}
```

### 2.5 Custom Hooks for Data Fetching

**frontend/src/hooks/useCustomer.ts:**
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { customerService } from '@services/customer.service'
import { CustomerOnboardingRequest } from '@types/api.types'

// Hook: Get customer by ID
export function useCustomer(customerId: string | null) {
  return useQuery({
    queryKey: ['customer', customerId],
    queryFn: () => customerService.getCustomerById(customerId!),
    enabled: !!customerId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

// Hook: List all customers
export function useCustomers(page = 0, size = 20) {
  return useQuery({
    queryKey: ['customers', page, size],
    queryFn: () => customerService.getAllCustomers(page, size),
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

// Hook: Onboard new customer (mutation)
export function useOnboardCustomer() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CustomerOnboardingRequest) =>
      customerService.onboardCustomer(request),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['customers'] })
      queryClient.setQueryData(['customer', data.id], data)
    },
  })
}

// Hook: Update customer status
export function useUpdateCustomerStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ customerId, status }: { customerId: string; status: string }) =>
      customerService.updateCustomerStatus(customerId, status),
    onSuccess: (data) => {
      queryClient.setQueryData(['customer', data.id], data)
    },
  })
}
```

### 2.6 Form Validation Schema

**frontend/src/utils/validation.schemas.ts:**
```typescript
import { z } from 'zod'

// Personal Info validation
export const personalInfoSchema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  dateOfBirth: z.string().refine((date) => {
    const birthDate = new Date(date)
    const age = new Date().getFullYear() - birthDate.getFullYear()
    return age >= 18
  }, 'Must be at least 18 years old'),
  email: z.string().email('Invalid email address'),
  phoneNumber: z.string().regex(/^\+[1-9]\d{1,14}$/, 'Invalid phone number format'),
  nationality: z.string().length(2, 'Nationality must be 2-char ISO code'),
  taxIdentificationNumber: z.string().min(8, 'Invalid TIN'),
})

// Address validation
export const addressSchema = z.object({
  addressType: z.enum(['HOME', 'WORK', 'MAILING']),
  street: z.string().min(5, 'Invalid street address'),
  city: z.string().min(2, 'City required'),
  state: z.string().min(2, 'State required'),
  postalCode: z.string().min(3, 'Invalid postal code'),
  country: z.string().length(2, 'Country must be 2-char code'),
  isPrimary: z.boolean(),
})

// KYC Document validation
export const kycDocumentSchema = z.object({
  documentType: z.enum(['PASSPORT', 'DRIVERS_LICENSE', 'NATIONAL_ID']),
  documentNumber: z.string().min(5, 'Invalid document number'),
  issuingAuthority: z.string().min(2, 'Issuing authority required'),
  issuingCountry: z.string().length(2, 'Country code required'),
  issueDate: z.string().refine((date) => new Date(date) <= new Date(), 'Issue date cannot be in future'),
  expiryDate: z.string().refine((date) => new Date(date) > new Date(), 'Document must not be expired'),
})

// Risk Profile validation
export const riskProfileSchema = z.object({
  riskLevel: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  riskScore: z.number().min(0).max(100),
  assessedBy: z.string().min(2),
  factors: z.array(z.object({
    factorName: z.string(),
    factorDescription: z.string(),
    weight: z.number().min(0).max(1),
  })),
})

// Complete onboarding request
export const onboardingRequestSchema = z.object({
  personalInfo: personalInfoSchema,
  addresses: z.array(addressSchema).min(1, 'At least one address required'),
  kycDocuments: z.array(kycDocumentSchema).min(1, 'At least one KYC document required'),
  riskProfile: riskProfileSchema,
})

export type OnboardingFormData = z.infer<typeof onboardingRequestSchema>
```

---

## 🎯 PHASE 3: PAGE COMPONENTS & UI (Week 3-4)

### 3.1 Layout Component

**frontend/src/components/Layout.tsx:**
```typescript
import React from 'react'
import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { Navigation } from './Navigation'

export const Layout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="flex">
        <Navigation />
        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

### 3.2 Customer Onboarding Page

**frontend/src/pages/OnboardingPage.tsx:**
```typescript
import React from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useOnboardCustomer } from '@hooks/useCustomer'
import { onboardingRequestSchema, OnboardingFormData } from '@utils/validation.schemas'
import { OnboardingForm } from '@components/OnboardingForm'
import { LoadingSpinner } from '@components/LoadingSpinner'
import { AlertBox } from '@components/AlertBox'

export const OnboardingPage: React.FC = () => {
  const { mutate, isPending, isError, error, data } = useOnboardCustomer()
  const { control, handleSubmit } = useForm<OnboardingFormData>({
    resolver: zodResolver(onboardingRequestSchema),
  })

  const onSubmit = async (data: OnboardingFormData) => {
    mutate(data)
  }

  if (isPending) return <LoadingSpinner message="Processing your onboarding..." />

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Customer Onboarding</h1>

      {isError && (
        <AlertBox
          type="error"
          title="Onboarding Failed"
          message={(error as any)?.response?.data?.message || 'An error occurred'}
        />
      )}

      {data && (
        <AlertBox
          type="success"
          title="Success!"
          message={`Customer ${data.customerNumber} onboarded successfully`}
        />
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-lg shadow p-8">
        <OnboardingForm control={control} />
        <button
          type="submit"
          className="mt-6 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Submit Onboarding
        </button>
      </form>
    </div>
  )
}
```

### 3.3 Customer Search Page

**frontend/src/pages/CustomerSearchPage.tsx:**
```typescript
import React, { useState } from 'react'
import { useCustomer } from '@hooks/useCustomer'
import { CustomerDetails } from '@components/CustomerDetails'
import { SearchInput } from '@components/SearchInput'
import { LoadingSpinner } from '@components/LoadingSpinner'

export const CustomerSearchPage: React.FC = () => {
  const [customerId, setCustomerId] = useState<string | null>(null)
  const { data, isLoading, error } = useCustomer(customerId)

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Customer Search</h1>

      <SearchInput onSearch={setCustomerId} placeholder="Enter Customer ID or Customer Number" />

      {isLoading && <LoadingSpinner message="Loading customer details..." />}

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          Failed to load customer
        </div>
      )}

      {data && <CustomerDetails customer={data} />}
    </div>
  )
}
```

### 3.4 Routing Setup

**frontend/src/App.tsx:**
```typescript
import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from '@store/queryClient'
import { Layout } from '@components/Layout'
import { OnboardingPage } from '@pages/OnboardingPage'
import { CustomerSearchPage } from '@pages/CustomerSearchPage'
import { AccountManagementPage } from '@pages/AccountManagementPage'
import { Dashboard } from '@pages/Dashboard'

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/onboarding" element={<OnboardingPage />} />
            <Route path="/search" element={<CustomerSearchPage />} />
            <Route path="/accounts" element={<AccountManagementPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
```

### 3.5 Playwright UI Field & Navigation Inventory

Use the following UI map when building the React/Vite frontend so Playwright tests can target stable selectors and cover every major user journey.

#### A. Global App Shell / Navigation

| Area | Route / Location | UI Elements | Recommended Selectors | Playwright Purpose |
|---|---|---|---|---|
| App shell | `/*` | logo, app title, user/help actions | `data-testid="app-logo"`, `data-testid="app-title"`, `data-testid="help-button"` | verify shell renders on every page |
| Primary nav | all pages | Dashboard, Onboarding, Customers, Accounts, Reports | `data-testid="nav-dashboard"`, `data-testid="nav-onboarding"`, `data-testid="nav-customers"`, `data-testid="nav-accounts"`, `data-testid="nav-reports"` | route coverage and active-state checks |
| Breadcrumbs | detail pages | home → list → detail trail | `data-testid="breadcrumb"` | verify navigation context |
| Toast / alerts | all pages | success, error, warning, info | `role="alert"`, `data-testid="toast-*"` | validate success/failure feedback |
| Loading state | all pages | spinner, skeleton, progress bar | `data-testid="loading-spinner"`, `data-testid="loading-skeleton"` | assert async state transitions |

#### B. Dashboard Page (`/`)

| Section | Fields / Actions | Recommended Selectors | Playwright Checks |
|---|---|---|---|
| Quick actions | onboard customer, search customer, open accounts | `data-testid="quick-onboard"`, `data-testid="quick-search"`, `data-testid="quick-accounts"` | click-through route validation |
| KPI cards | total customers, pending verifications, active accounts, recent onboarding count | `data-testid="kpi-total-customers"`, `data-testid="kpi-pending-verifications"`, `data-testid="kpi-active-accounts"`, `data-testid="kpi-recent-onboardings"` | render with numeric values |
| Recent activity | latest onboarding events table/list | `data-testid="recent-activity-table"` | row count, empty state, refresh behavior |
| Environment badge | DEV / ST1 / ST3 / PROD | `data-testid="environment-badge"` | ensure config is displayed correctly |

#### C. Customer Onboarding Flow (`/onboarding`)

Build this as a multi-step form or a long single form. Either way, keep the field IDs stable.

##### Step 1 — Personal Information

| Field | Input Type | Selector | Notes |
|---|---|---|---|
| First name | text | `data-testid="first-name"` | required |
| Last name | text | `data-testid="last-name"` | required |
| Date of birth | date | `data-testid="date-of-birth"` | must be adult |
| Email | email | `data-testid="email"` | unique for duplicate-email test |
| Phone number | tel | `data-testid="phone-number"` | E.164 format |
| Nationality | select | `data-testid="nationality"` | ISO 2-char |
| Tax ID | text | `data-testid="tax-identification-number"` | required |

##### Step 2 — Address Information

| Field | Input Type | Selector | Notes |
|---|---|---|---|
| Address type | radio/select | `data-testid="address-type"` | HOME / WORK / MAILING |
| Street | text | `data-testid="street"` | required |
| City | text | `data-testid="city"` | required |
| State | text | `data-testid="state"` | required |
| Postal code | text | `data-testid="postal-code"` | required |
| Country | select | `data-testid="country"` | default AU |
| Primary address | checkbox | `data-testid="is-primary"` | one primary required |
| Add address | button | `data-testid="add-address"` | for multiple address scenarios |
| Remove address | button | `data-testid="remove-address"` | for row removal coverage |

##### Step 3 — KYC Documents

| Field | Input Type | Selector | Notes |
|---|---|---|---|
| Document type | select | `data-testid="document-type"` | PASSPORT / DRIVERS_LICENSE / NATIONAL_ID |
| Document number | text | `data-testid="document-number"` | required |
| Issuing authority | text | `data-testid="issuing-authority"` | required |
| Issuing country | select | `data-testid="issuing-country"` | ISO 2-char |
| Issue date | date | `data-testid="issue-date"` | must not be future |
| Expiry date | date | `data-testid="expiry-date"` | must be future |
| Add document | button | `data-testid="add-document"` | for multiple documents |
| Remove document | button | `data-testid="remove-document"` | negative/edge coverage |

##### Step 4 — Risk Profile

| Field | Input Type | Selector | Notes |
|---|---|---|---|
| Risk level | radio/select | `data-testid="risk-level"` | LOW / MEDIUM / HIGH |
| Risk score | number/slider | `data-testid="risk-score"` | 0-100 |
| Assessed by | text | `data-testid="assessed-by"` | analyst/system |
| Risk factor name | text | `data-testid="risk-factor-name"` | repeatable |
| Risk factor description | textarea | `data-testid="risk-factor-description"` | repeatable |
| Risk factor weight | number | `data-testid="risk-factor-weight"` | 0-1 |
| Add factor | button | `data-testid="add-risk-factor"` | multi-factor coverage |
| Remove factor | button | `data-testid="remove-risk-factor"` | row deletion |

##### Step 5 — Initial Account (Optional)

| Field | Input Type | Selector | Notes |
|---|---|---|---|
| Enable initial account | checkbox/toggle | `data-testid="enable-initial-account"` | show/hide account block |
| Account type | select | `data-testid="initial-account-type"` | SAVINGS / CHECKING / TERM_DEPOSIT |
| Currency | select | `data-testid="initial-account-currency"` | ISO 4217, default AUD |
| Initial deposit | number | `data-testid="initial-deposit"` | currency amount |
| Product code | text | `data-testid="product-code"` | product identifier |

##### Step 6 — Submission / Result

| UI Item | Selector | Playwright Checks |
|---|---|---|
| Submit button | `data-testid="submit-onboarding"` | enabled/disabled state, click submission |
| Save draft | `data-testid="save-draft"` | optional persisted draft behavior |
| Reset form | `data-testid="reset-form"` | all inputs cleared |
| Success panel | `data-testid="onboarding-success"` | contains customer number, ID, next steps |
| Error summary | `data-testid="form-error-summary"` | field validation list |

#### D. Customer Search Page (`/search`)

| Field / Action | Selector | Playwright Checks |
|---|---|---|
| Search by customer ID | `data-testid="search-by-id"` | accepts UUID |
| Search by customer number | `data-testid="search-by-number"` | accepts CUST- format |
| Search input | `data-testid="customer-search-input"` | type + submit |
| Search button | `data-testid="search-submit"` | triggers API call |
| Clear search | `data-testid="search-clear"` | resets search state |
| Result card | `data-testid="customer-result-card"` | displays summary fields |
| Empty state | `data-testid="search-empty-state"` | no result message |
| Not found state | `data-testid="search-not-found"` | 404 display |

#### E. Customer Detail Page / Drawer

Use either a full page or right-side drawer when a search result is opened.

| Area | Selector | Fields to verify |
|---|---|---|
| Customer header | `data-testid="customer-header"` | name, status, customer number |
| Personal info panel | `data-testid="customer-personal-info"` | email, phone, DOB, nationality |
| Address panel | `data-testid="customer-addresses"` | multiple addresses, primary flag |
| KYC panel | `data-testid="customer-kyc-documents"` | document type, expiry |
| Risk panel | `data-testid="customer-risk-profile"` | risk score, factors |
| Onboarding status panel | `data-testid="customer-onboarding-status"` | completed/remaining steps |
| Edit customer status | `data-testid="edit-status-button"` | open status dialog |

#### F. Account Management Page (`/accounts`)

| Section | Fields / Actions | Recommended Selectors | Playwright Checks |
|---|---|---|---|
| Customer lookup | customer id / number | `data-testid="accounts-customer-id"`, `data-testid="accounts-customer-number"` | query accounts for a customer |
| Create account button | create account | `data-testid="create-account"` | opens form/dialog |
| Account type | select | `data-testid="account-type"` | SAVINGS / CHECKING / TERM_DEPOSIT |
| Currency | select | `data-testid="account-currency"` | ISO 4217 |
| Deposit | number | `data-testid="account-initial-deposit"` | validation and formatting |
| Open account submit | button | `data-testid="submit-create-account"` | creates new account |
| Accounts table | table | `data-testid="accounts-table"` | row count, headers, empty state |
| Account details row | row actions | `data-testid="view-account"`, `data-testid="close-account"` | open/close flow |
| Close confirmation | dialog | `data-testid="close-account-dialog"` | confirm/cancel coverage |

#### G. Common Validation and Error States

| State | Selector | What to test |
|---|---|---|
| Inline field error | `data-testid="field-error-*"` | required, format, range validation |
| API error banner | `data-testid="api-error-banner"` | 400 / 409 / 422 / 500 |
| Network error banner | `data-testid="network-error-banner"` | offline / timeout handling |
| Disabled submit | `data-testid="submit-disabled"` | invalid form prevents submission |
| Retry button | `data-testid="retry-button"` | recovery after failure |

#### H. Playwright Test Coverage Matrix

| Test Group | Suggested Route(s) | Suggested Assertions |
|---|---|---|
| Smoke | `/`, `/onboarding`, `/search` | page loads, nav works, happy path submit |
| Navigation | all routes | active nav state, back/forward behavior |
| Form validation | `/onboarding` | required fields, invalid formats, error summaries |
| Multi-value fields | `/onboarding` | add/remove address, document, risk factor |
| Search flows | `/search` | ID vs number search, empty state, not found |
| Account flows | `/accounts` | create, list, close, confirmation dialog |
| Accessibility | all routes | labels, roles, keyboard navigation |
| Resilience | all routes | loading, API errors, retry states |

#### I. Recommended Testing Conventions

- Prefer `data-testid` for all stable Playwright locators.
- Use semantic labels (`label`/`aria-label`) for accessibility and fallback selectors.
- Keep route names consistent with menu labels.
- Give every repeatable list a unique container selector.
- Use one selector convention across the app:
  - Page root: `data-testid="page-<name>"`
  - Form field: `data-testid="<kebab-case-field-name>"`
  - Button/action: `data-testid="<action-name>"`
  - Table row: `data-testid="row-<entity-id>"`

This inventory is the basis for writing robust Playwright UI tests against the React/Vite frontend.

---

## 🚀 PHASE 4: BUILD & DEPLOYMENT (Week 4)

### 4.1 Update build.gradle

Add a task to build the frontend before packaging the JAR:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    // ... other plugins
}

// Frontend build task
task buildFrontend(type: Exec) {
    workingDir 'frontend'
    commandLine 'npm', 'run', 'build'
    onlyIf { !System.getProperty('skipFrontend') }
}

// Ensure frontend builds before boot JAR
bootJar.dependsOn buildFrontend

// Copy frontend build output to static resources
task copyFrontend(type: Copy) {
    from('frontend/dist')
    into('build/resources/main/static')
}

processResources.dependsOn copyFrontend
```

### 4.2 Spring Boot Static Resource Configuration

**src/main/resources/application.properties:**
```properties
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.add-mappings=true
# Serve index.html for all non-API routes (SPA fallback)
server.servlet.context-path=/
```

### 4.3 SPA Routing Fix (Optional Controller)

**src/main/java/com/bank/onboarding/config/ResourceConfig.java:**
```java
package com.bank.onboarding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Route all non-API requests to index.html for React Router
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{x:[\\w\\-]+}").setViewName("forward:/index.html");
        registry.addViewController("/{x:^(?!api).*$}/**").setViewName("forward:/index.html");
    }
}
```

### 4.4 Docker Build Update

**Dockerfile:**
```dockerfile
# Build backend + frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

# Build backend with frontend
FROM gradle:8.5-jdk21 AS backend-builder
WORKDIR /app
COPY . .
COPY --from=frontend-builder /app/dist ./frontend/dist
RUN gradle assemble --no-daemon -DskipFrontend=true

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.5 Environment File Setup

**frontend/.env.example:**
```
VITE_API_URL=/api
VITE_APP_NAME=Bank Customer Onboarding
```

**frontend/.env.development:**
```
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=Bank Customer Onboarding (Dev)
```

**frontend/.env.production:**
```
VITE_API_URL=/api
VITE_APP_NAME=Bank Customer Onboarding
```

---

## 🔄 FUTURE: SEPARATION STRATEGY (For Later)

When you're ready to separate frontend and backend:

### Timeline: Weeks 1-2

**Week 1: Preparation**
- Create new GitHub repo: `bank-onboarding-ui`
- Copy entire `frontend/` folder
- Update CI/CD to deploy frontend independently

**Week 2: Deployment**
- Deploy frontend to separate domain/CDN
- Update backend CORS configuration
- Implement API versioning for compatibility
- Switch traffic to separate UI

### Modified Architecture (After Separation)

```
GitHub Repositories (separate teams)
│
├── bank-customer-onboarding-service/  (Backend team)
│   ├── src/main/                      (remove static/)
│   ├── Dockerfile                     (no frontend build)
│   └── build.gradle                   (remove frontend tasks)
│
└── bank-onboarding-ui/                (Frontend team)
    ├── src/
    ├── public/
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile                     (Node + Nginx)
    ├── .github/workflows/             (separate Playwright E2E)
    └── DEPLOYMENT.md
```

### CORS Configuration (For Separated Deployment)

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins(
                        "http://localhost:5173",  // dev
                        "https://ui.bank.example.com"  // prod
                    )
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

---

## 📋 IMPLEMENTATION CHECKLIST

### Phase 1: Setup
- [ ] Create `frontend/` folder structure
- [ ] Initialize npm project
- [ ] Setup TypeScript, Vite, Tailwind
- [ ] Create `.env` files

### Phase 2: Services & Types
- [ ] Create TypeScript API types
- [ ] Implement API client service
- [ ] Create customer API service
- [ ] Create account API service
- [ ] Create custom hooks (useQuery/useMutation)
- [ ] Create form validation schemas

### Phase 3: Components
- [ ] Create Layout component
- [ ] Create reusable UI components (Button, Input, Card, etc.)
- [ ] Create OnboardingPage
- [ ] Create CustomerSearchPage
- [ ] Create AccountManagementPage
- [ ] Create Dashboard
- [ ] Setup React Router

### Phase 4: Build & Deploy
- [ ] Update build.gradle
- [ ] Update Dockerfile
- [ ] Test local build
- [ ] Test Docker build
- [ ] Update GitHub Actions (if applicable)

### Phase 5 (Future): Separation
- [ ] Create separate frontend repo
- [ ] Update CORS configuration
- [ ] Deploy frontend independently
- [ ] Update DNS/CDN routing

---

## 🧪 TESTING STRATEGY

### Unit Tests (Vitest)

**tests/components/OnboardingForm.test.tsx:**
```typescript
import { render, screen } from '@testing-library/react'
import { OnboardingForm } from '@components/OnboardingForm'
import { describe, it, expect } from 'vitest'

describe('OnboardingForm', () => {
  it('renders all form sections', () => {
    render(<OnboardingForm control={mockControl} />)
    expect(screen.getByText('Personal Information')).toBeInTheDocument()
    expect(screen.getByText('Addresses')).toBeInTheDocument()
    expect(screen.getByText('KYC Documents')).toBeInTheDocument()
  })
})
```

### E2E Tests (Playwright)

**See your existing PLAYWRIGHT_FRAMEWORK_PROMPTS.md for integration with backend tests**

Add E2E tests in `frontend/e2e/`:
```typescript
import { test, expect } from '@playwright/test'

test('should onboard customer via UI', async ({ page }) => {
  await page.goto('http://localhost:5173/onboarding')
  await page.fill('#firstName', 'John')
  await page.fill('#email', 'john@example.com')
  // ... fill other fields
  await page.click('button:has-text("Submit Onboarding")')
  await expect(page.locator('text=Success')).toBeVisible()
})
```

---

## 📚 KEY FILES TO CREATE

| File | Purpose |
|------|---------|
| `frontend/vite.config.ts` | Bundler config + API proxy |
| `frontend/src/services/api.client.ts` | HTTP client with interceptors |
| `frontend/src/services/customer.service.ts` | Customer API methods |
| `frontend/src/hooks/useCustomer.ts` | React Query hooks |
| `frontend/src/types/api.types.ts` | TypeScript interfaces |
| `frontend/src/utils/validation.schemas.ts` | Zod validation |
| `frontend/src/pages/OnboardingPage.tsx` | Onboarding form page |
| `frontend/src/pages/CustomerSearchPage.tsx` | Search page |
| `frontend/tailwind.config.js` | Tailwind theming |
| `build.gradle` | Updated with frontend build task |
| `Dockerfile` | Multi-stage build |

---

## 💡 BEST PRACTICES HIGHLIGHTED

### ✅ Type Safety
- Generate types from OpenAPI/Swagger
- Use Zod for runtime validation
- Strict TypeScript `noImplicitAny`

### ✅ State Management
- TanStack Query for server state
- Zustand for client state (if needed)
- No prop drilling

### ✅ API Integration
- Centralized API client
- Request/response interceptors
- Correlation IDs for observability

### ✅ Form Handling
- React Hook Form + Zod
- Field-level validation
- Error display per field

### ✅ Testing
- Component tests (Vitest)
- E2E tests (Playwright)
- Integration tests overlap with backend tests

### ✅ Performance
- Code splitting via React Router
- Lazy loading components
- Image optimization

### ✅ Accessibility
- Semantic HTML
- ARIA labels
- Keyboard navigation

---

## 📖 DOCUMENTATION FILES TO CREATE

1. `docs/FRONTEND_SETUP.md` — Local dev setup
2. `docs/API_CLIENT_GENERATION.md` — Auto-generate types from OpenAPI
3. `docs/DEPLOYMENT_MIGRATION.md` — Separation strategy
4. `docs/COMPONENT_LIBRARY.md` — Reusable component catalog
5. `docs/STATE_MANAGEMENT.md` — Query + mutation patterns

---

## ✨ SUCCESS CRITERIA

When complete, you'll have:

✅ **Monolithic Deployment**
- React UI served from `http://localhost:8080/`
- Spring Boot backend at `http://localhost:8080/api`
- Single Docker image, single deployment

✅ **Type Safety**
- Zero `any` types
- All types from OpenAPI or manually defined
- Runtime validation with Zod

✅ **User Experience**
- Responsive Tailwind design
- Real-time form validation
- Loading states & error handling

✅ **Testing**
- Unit tests for components (Vitest)
- E2E tests for user flows (Playwright)
- Integration tests (via existing backend tests)

✅ **Future-Ready**
- Clear folder boundaries
- API client abstraction
- CORS configuration ready
- Easy to extract to separate repo

---

**Ready to start Phase 1? Begin with folder structure & npm setup!**

*Next: Create `frontend/` folder and initialize project per 1.2-1.4*

