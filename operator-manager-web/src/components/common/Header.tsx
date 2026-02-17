import { Layout, Button, Space, Dropdown, Avatar, Badge, message } from 'antd';
import { UserOutlined, LogoutOutlined, SettingOutlined, BellOutlined, MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '@/stores/useAuthStore';

const { Header } = Layout;

interface Props {
  collapsed: boolean;
  onToggle: () => void;
}

/**
 * Header component
 */
const HeaderComponent: React.FC<Props> = ({ collapsed, onToggle }) => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await import('@/api/auth').then((m) => m.default.logout());
      logout();
      message.success('Logged out successfully');
      navigate('/login');
    } catch (error) {
      message.error('Failed to logout');
    }
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Settings',
      onClick: () => navigate('/settings'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: handleLogout,
    },
  ];

  const notifications = (
    <div style={{ width: 300 }}>
      <p style={{ textAlign: 'center', color: '#999' }}>No notifications</p>
    </div>
  );

  return (
    <Header style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 24px',
      background: '#001529',
      boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
    }}>
      <Button
        type="text"
        icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        onClick={onToggle}
        style={{
          color: 'white',
          fontSize: '18px',
        }}
      />

      <Space size="large">
        <Badge count={0} offset={[10, 0]}>
          <Button
            type="text"
            icon={<BellOutlined />}
            style={{ color: 'white', fontSize: '16px' }}
          />
        </Badge>

        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Space style={{ cursor: 'pointer' }}>
            <Avatar
              size="small"
              src={user?.avatarUrl}
              icon={!user?.avatarUrl && <UserOutlined />}
              style={{
                backgroundColor: '#1677ff',
                color: 'white',
              }}
            />
            <span style={{ color: 'white', marginLeft: 8 }}>
              {user?.fullName || user?.username}
            </span>
          </Space>
        </Dropdown>
      </Space>
    </Header>
  );
};

export default HeaderComponent;
