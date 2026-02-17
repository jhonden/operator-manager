import { Menu, Layout } from 'antd';
import {
  CodeOutlined,
  AppstoreOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '@/stores/useAuthStore';

const { Sider } = Layout;

interface Props {
  collapsed: boolean;
}

/**
 * Sidebar navigation component
 */
const Sidebar: React.FC<Props> = ({ collapsed }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuthStore();

  const menuItems = [
    {
      key: 'operators-group',
      label: 'Operators',
      type: 'group' as const,
      children: [
        {
          key: '/operators',
          icon: <CodeOutlined />,
          label: 'All Operators',
          onClick: () => navigate('/operators'),
        },
      ],
    },
    {
      key: 'packages-group',
      label: 'Packages',
      type: 'group' as const,
      children: [
        {
          key: '/packages',
          icon: <AppstoreOutlined />,
          label: 'All Packages',
          onClick: () => navigate('/packages'),
        },
      ],
    },
    {
      key: '/market',
      icon: <ShopOutlined />,
      label: 'Marketplace',
      onClick: () => navigate('/market'),
    },
  ];

  // Get current selected key based on location
  const getSelectedKey = () => {
    const path = location.pathname;

    if (path.startsWith('/operators')) {
      return '/operators';
    }
    if (path.startsWith('/packages')) {
      return '/packages';
    }
    if (path.startsWith('/market')) {
      return '/market';
    }

    return path;
  };

  return (
    <Sider width={200} style={{ background: '#001529' }}>
      <div style={{
        height: '64px',
        display: 'flex',
        alignItems: 'center',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
      }}>
        {!collapsed && (
          <h1 style={{
            color: 'white',
            fontSize: 18,
            fontWeight: 600,
            margin: 0,
          }}>
            Operator Manager
          </h1>
        )}
      </div>

      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[getSelectedKey()]}
        items={menuItems}
        style={{ borderRight: 0 }}
      />
    </Sider>
  );
};

export default Sidebar;
