import { Navigate, Outlet, useLocation } from 'react-router-dom';
import useAuthStore from '@/stores/useAuthStore';

/**
 * Protected route component that checks authentication
 */
const ProtectedRoute: React.FC = () => {
  const { isAuthenticated } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    // Redirect to login page with return location
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
