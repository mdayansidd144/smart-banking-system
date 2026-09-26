import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Dashboard from './pages/Dashboard';
import Accounts from './pages/Accounts';
import AccountDetail from './pages/AccountDetail';
import Anomalies from './pages/Anomalies';
import LoanPage from './pages/LoanPage';
import AgentPage from './pages/AgentPage';
import AuditLogPage from './pages/AuditLogPage';
import AnalyticsPage from './pages/AnalyticsPage';
import BudgetsPage from './pages/BudgetsPage';
import BillsPage from './pages/BillsPage';
import FxRatesPage from './pages/FxRatesPage';
import SettingsPage from './pages/SettingsPage';
import KycPage from './pages/KycPage';
import AdminKycPage from './pages/AdminKycPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import LoanAmortizationPage from './pages/LoanAmortizationPage';
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="accounts" element={<Accounts />} />
        <Route path="accounts/:id" element={<AccountDetail />} />
        <Route path="anomalies" element={<Anomalies />} />
        <Route path="loan" element={<LoanPage />} />
        <Route path="agent" element={<AgentPage />} />
        <Route path="audit" element={<AuditLogPage />} />
        <Route path="analytics" element={<AnalyticsPage />} />
        <Route path="budgets" element={<BudgetsPage />} />
        <Route path="bills" element={<BillsPage />} />
        <Route path="fx" element={<FxRatesPage />} />
        <Route path="settings" element={<SettingsPage />} />
        <Route path="kyc" element={<KycPage />} />
        <Route path="admin/kyc" element={<AdminKycPage />} />
        <Route path="/loans/:id/amortization" element={<LoanAmortizationPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}