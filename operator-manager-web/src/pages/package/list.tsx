import { useState, useEffect } from 'react';
import {
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
import { t } from '@/utils/i18n';

/**
 * Operator package list page
 */
const PackageListPage: React.FC = () => {
  const navigate = useNavigate();
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
      message.error(error.message || '获取算子包列表失败');
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
      message.success('算子包删除成功');
      fetchPackages();
    } catch (error: any) {
      message.error(error.message || '删除算子包失败');
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={
          <Space>
            <AppstoreOutlined />
            <span>算子包</span>
            <Tag color="blue">{t('common.total')} {pagination.total}</Tag>
          </Space>
        }
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/packages/create')}
          >
            创建算子包
          </Button>
        }
      >
        {/* Search and Filters */}
        <Space style={{ marginBottom: 24 }} wrap>
          <Input
            placeholder="搜索算子包..."
            prefix={<SearchOutlined />}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Select
            placeholder={t('placeholder.selectLanguage')}
            value={filters.status}
            onChange={(value) => setFilters({ ...filters, status: value })}
            style={{ width: 120 }}
            allowClear
          >
            <Select.Option value="DRAFT">{t('operator.status.draft')}</Select.Option>
            <Select.Option value="PUBLISHED">{t('operator.status.published')}</Select.Option>
            <Select.Option value="ARCHIVED">{t('operator.status.archived')}</Select.Option>
          </Select>
          <Button type="primary" onClick={handleSearch}>
            {t('common.search')}
          </Button>
          <Button onClick={handleReset}>
            {t('common.reset')}
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchPackages}>
            {t('common.refresh')}
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
                    {t('common.view')}
                  </Button>,
                  <Button
                    type="text"
                    icon={<EditOutlined />}
                    onClick={() => navigate(`/packages/${pkg.id}/edit`)}
                  >
                    {t('common.edit')}
                  </Button>,
                  <Popconfirm
                    title="确定要删除此算子包吗？"
                    onConfirm={() => handleDelete(pkg.id)}
                    okText={t('common.yes')}
                    cancelText={t('common.no')}
                  >
                    <Button type="text" danger icon={<DeleteOutlined />}>
                      {t('common.delete')}
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
                            精选
                          </Tag>
                        )}
                      </Space>
                    </Space>
                  }
                  description={
                    <div style={{ height: '80px', overflow: 'hidden' }}>
                      <div style={{ fontSize: '12px', color: '#8c8c8c', marginBottom: 8 }}>
                        {pkg.description || '暂无描述'}
                      </div>
                      <div style={{ fontSize: '12px', color: '#1890ff' }}>
                        📦 {pkg.operatorCount || 0} 个算子
                      </div>
                      <div style={{ fontSize: '11px', color: '#8c8c8c', marginTop: 4 }}>
                        🏷️ v{pkg.version || '0.0.1'}
                      </div>
                      <div style={{ fontSize: '11px', color: '#8c8c8c', marginTop: 4 }}>
                        ↓ {pkg.downloadsCount || 0} 次下载
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
              {t('common.previous')}
            </Button>
            <span>
              第 {pagination.current} 页，共 {Math.ceil(pagination.total / pagination.pageSize)} 页
            </span>
            <Button
              disabled={pagination.current >= Math.ceil(pagination.total / pagination.pageSize)}
              onClick={() =>
                setPagination({ ...pagination, current: pagination.current + 1 })
              }
            >
              {t('common.next')}
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
};

export default PackageListPage;
