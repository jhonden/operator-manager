import { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Input,
  Select,
  Card,
  message,
  Popconfirm,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  SearchOutlined,
  ReloadOutlined,
  AppstoreOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { OperatorPackage, PackageFilters } from '@/types';
import { packageApi } from '@/api/package';

/**
 * Operator package list page
 */
const PackageListPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [packages, setPackages] = useState<OperatorPackage[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 12,
    total: 0,
  });

  // Filter states
  const [filters, setFilters] = useState<PackageFilters>({
    keyword: '',
    status: undefined,
  });

  const fetchPackages = async () => {
    setLoading(true);
    try {
      const response = await packageApi.getAllPackages(
        filters.status,
        filters.keyword,
        pagination.current - 1,
        pagination.pageSize
      );

      if (response.data) {
        setPackages(response.data.content);
        setPagination({
          ...pagination,
          total: response.data.totalElements,
        });
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch packages');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPackages();
  }, [pagination.current, pagination.pageSize]);

  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchPackages();
  };

  const handleReset = () => {
    setFilters({
      keyword: '',
      status: undefined,
    });
    setPagination({ ...pagination, current: 1 });
    setTimeout(() => fetchPackages(), 0);
  };

  const handleDelete = async (id: number) => {
    try {
      await packageApi.deletePackage(id);
      message.success('Package deleted successfully');
      fetchPackages();
    } catch (error: any) {
      message.error(error.message || 'Failed to delete package');
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={
          <Space>
            <AppstoreOutlined />
            <span>Operator Packages</span>
            <Tag color="blue">{pagination.total} total</Tag>
          </Space>
        }
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/packages/create')}
          >
            Create Package
          </Button>
        }
      >
        {/* Search and Filters */}
        <Space style={{ marginBottom: 24 }} wrap>
          <Input
            placeholder="Search packages..."
            prefix={<SearchOutlined />}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Select
            placeholder="Status"
            value={filters.status}
            onChange={(value) => setFilters({ ...filters, status: value })}
            style={{ width: 120 }}
            allowClear
          >
            <Select.Option value="DRAFT">Draft</Select.Option>
            <Select.Option value="PUBLISHED">Published</Select.Option>
            <Select.Option value="ARCHIVED">Archived</Select.Option>
          </Select>
          <Button type="primary" onClick={handleSearch}>
            Search
          </Button>
          <Button onClick={handleReset}>
            Reset
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchPackages}>
            Refresh
          </Button>
        </Space>

        {/* Package Cards Grid */}
        <Row gutter={[16, 16]}>
          {packages.map((pkg) => (
            <Col xs={24} sm={12} lg={8} xl={6} key={pkg.id}>
              <Card
                hoverable
                style={{ height: '100%' }}
                actions={[
                  <Button
                    type="text"
                    icon={<EyeOutlined />}
                    onClick={() => navigate(`/packages/${pkg.id}`)}
                  >
                    View
                  </Button>,
                  <Button
                    type="text"
                    icon={<EditOutlined />}
                    onClick={() => navigate(`/packages/${pkg.id}/edit`)}
                  >
                    Edit
                  </Button>,
                  <Popconfirm
                    title="Are you sure you want to delete this package?"
                    onConfirm={() => handleDelete(pkg.id)}
                    okText="Yes"
                    cancelText="No"
                  >
                    <Button type="text" danger icon={<DeleteOutlined />}>
                      Delete
                    </Button>
                  </Popconfirm>,
                ]}
              >
                <Card.Meta
                  avatar={
                    <div
                      style={{
                        width: 48,
                        height: 48,
                        borderRadius: '8px',
                        background: pkg.icon
                          ? `url(${pkg.icon}) center/cover`
                          : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white',
                        fontSize: '24px',
                      }}
                    >
                      {!pkg.icon && '📦'}
                    </div>
                  }
                  title={
                    <Space direction="vertical" size={0}>
                      <span style={{ fontSize: '16px' }}>{pkg.name}</span>
                      <Space size={4}>
                        <Tag
                          color={
                            pkg.status === 'PUBLISHED'
                              ? 'success'
                              : pkg.status === 'ARCHIVED'
                              ? 'warning'
                              : 'default'
                          }
                          style={{ fontSize: '11px' }}
                        >
                          {pkg.status}
                        </Tag>
                        {pkg.featured && (
                          <Tag color="gold" style={{ fontSize: '11px' }}>
                            Featured
                          </Tag>
                        )}
                      </Space>
                    </Space>
                  }
                  description={
                    <div style={{ height: '80px', overflow: 'hidden' }}>
                      <div style={{ fontSize: '12px', color: '#8c8c8c', marginBottom: 8 }}>
                        {pkg.description || 'No description'}
                      </div>
                      <div style={{ fontSize: '12px', color: '#1890ff' }}>
                        📋 {pkg.operatorCount || 0} operators
                      </div>
                      <div style={{ fontSize: '11px', color: '#8c8c8c', marginTop: 4 }}>
                        🏷️ v{pkg.version || '0.0.1'}
                      </div>
                      <div style={{ fontSize: '11px', color: '#8c8c8c', marginTop: 4 }}>
                        ↓ {pkg.downloadsCount || 0} downloads
                      </div>
                    </div>
                  }
                />
              </Card>
            </Col>
          ))}
        </Row>

        {/* Pagination */}
        <div style={{ marginTop: 24, textAlign: 'center' }}>
          <Space>
            <Button
              disabled={pagination.current === 1}
              onClick={() =>
                setPagination({ ...pagination, current: pagination.current - 1 })
              }
            >
              Previous
            </Button>
            <span>
              Page {pagination.current} of {Math.ceil(pagination.total / pagination.pageSize)}
            </span>
            <Button
              disabled={pagination.current >= Math.ceil(pagination.total / pagination.pageSize)}
              onClick={() =>
                setPagination({ ...pagination, current: pagination.current + 1 })
              }
            >
              Next
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
};

export default PackageListPage;
