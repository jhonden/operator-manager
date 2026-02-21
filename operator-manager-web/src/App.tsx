import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { useEffect } from 'react';
import useAuthStore from './stores/useAuthStore';
import Layout from './components/common/Layout';
import ProtectedRoute from './components/common/ProtectedRoute';
import LoginPage from './pages/auth/login';
import RegisterPage from './pages/auth/register';
import OperatorListPage from './pages/operator/list';
import OperatorDetailPage from './pages/operator/detail';
import OperatorCreatePage from './pages/operator/create';
import PackageListPage from './pages/package/list';
import PackageDetailPage from './pages/package/detail';
import PackageCreatePage from './pages/package/create';
import LibraryListPage from './pages/library/list';

// Placeholder components for unimplemented pages
const PlaceholderPage: React.FC<{ title: string }> = ({ title }) => (
  <div style={{ padding: '24px', textAlign: 'center' }}>
    <h2>{title}</h2>
    <p style={{ color: '#999' }}>Coming Soon...</p>
  </div>
);

function App() {
  const { checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, []);

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        algorithm: theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 6,
        },
      }}
    >
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected routes */}
          <Route path="/" element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route index element={<Navigate to="/operators" replace />} />

              {/* Operator routes */}
              <Route path="operators">
                <Route index element={<OperatorListPage />} />
                <Route path="create" element={<OperatorCreatePage />} />
                <Route path=":id" element={<OperatorDetailPage />} />
                <Route path=":id/edit" element={<OperatorCreatePage />} />
              </Route>

              {/* Package routes */}
              <Route path="packages">
                <Route index element={<PackageListPage />} />
                <Route path="create" element={<PackageCreatePage />} />
                <Route path=":id" element={<PackageDetailPage />} />
                <Route path=":id/edit" element={<PackageCreatePage />} />
              </Route>

              {/* Library routes */}
              <Route path="libraries">
                <Route index element={<LibraryListPage />} />
              </Route>

              {/* Execution routes */}
              <Route path="execution">
                <Route path="tasks">
                  <Route index element={<PlaceholderPage title="Task List" />} />
                  <Route path=":id" element={<PlaceholderPage title="Task Detail" />} />
                </Route>
              </Route>

              {/* Version routes */}
              <Route path="versions" element={<PlaceholderPage title="Versions List" />} />

              {/* User routes */}
              <Route path="profile" element={<PlaceholderPage title="Profile" />} />
              <Route path="settings" element={<PlaceholderPage title="Settings" />} />
            </Route>
          </Route>

          {/* Catch all - 404 */}
          <Route
            path="*"
            element={
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '100vh',
                fontSize: '24px',
                color: '#999'
              }}>
                404 - Page Not Found
              </div>
            }
          />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
