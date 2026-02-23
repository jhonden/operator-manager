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
  Upload,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  SearchOutlined,
  ReloadOutlined,
  AppstoreOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { OperatorPackage } from '@/types';
import { packageApi } from '@/api/package';
import { t } from '@/utils/i18n';

/**
 * Operator package list page
 */
const PackageListPage: React.FC = () => {
  console.log('[PackageListPage] 组件渲染');

  const navigate = useNavigate();
  const [packages, setPackages] = useState<OperatorPackage[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 12,
    total: 0,
  });

  // Filter states
  const [filters, setFilters] = useState({
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

  const handleImport = async (file: File) => {
    console.log('[handleImport] 开始导入', file);
    try {
      const response = await packageApi.importPackage(file);
      console.log('[handleImport] API 响应', response);
      if (response.success && response.data) {
        message.success(
          `算子包导入成功！${t('common.operatorCreated')}: ${response.data.operatorsCreated}, ${t('common.operatorUpdated')}: ${response.data.operatorsUpdated}, ${t('common.libraryCreated')}: ${response.data.librariesCreated}, ${t('common.libraryUpdated')}: ${response.data.librariesUpdated}`
        );
        fetchPackages();
      } else {
        message.error(response.error || '导入算子包失败');
      }
    } catch (error: any) {
      console.error('[handleImport] 导入失败', error);
      message.error(error.message || '导入算子包失败');
    }
  };

  const uploadProps: UploadProps = {
    name: 'file',
    accept: '.zip,application/zip',
    maxCount: 1,
    showUploadList: false,
    beforeUpload: async (file) => {
      console.log('[beforeUpload] 文件验证中', file.name, file.type);
      const isZip = file.type === 'application/zip' || file.name.endsWith('.zip');
      if (!isZip) {
        message.error('只支持 ZIP 格式文件');
        return Upload.LIST_IGNORE;
      }

      const maxSize = 10 * 1024 * 1024; // 10MB
      if (file.size > maxSize) {
        message.error('文件大小超过限制（最大 10MB）');
        return Upload.LIST_IGNORE;
      }

      console.log('[beforeUpload] 验证通过，开始导入');
      // 直接在 beforeUpload 中调用导入函数
      handleImport(file);
      // 返回 false 阻止默认上传
      return Upload.LIST_IGNORE;
    },
    disabled: false,
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
          <Space>
            <Upload {...uploadProps} showUploadList={false}>
              <Button icon={<UploadOutlined />}>
                导入算子包
              </Button>
            </Upload>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => navigate('/packages/create')}
            >
              创建算子包
            </Button>
          </Space>
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
