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
  Modal,
  Tooltip,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  SearchOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { Operator, OperatorFilters } from '@/types';
import { DataFormatOptions, GeneratorOptions } from '@/types';
import { operatorApi } from '@/api/operator';
import { t } from '@/utils/i18n';

// Helper function to convert data format codes to labels
const formatDataFormat = (dataFormat?: string) => {
  if (!dataFormat) return '-';
  return dataFormat.split(',').map(code => {
    const option = DataFormatOptions.find(opt => opt.value === code);
    return option ? option.label : code;
  }).join(', ');
};

/**
 * Operator list page
 */
const OperatorListPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [operators, setOperators] = useState<Operator[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });

  // Filter states
  const [filters, setFilters] = useState<OperatorFilters>({
    keyword: '',
    language: undefined,
    status: undefined,
  });

  const fetchOperators = async () => {
    setLoading(true);
    try {
      const response = await operatorApi.getAllOperators({
        language: filters.language,
        status: filters.status,
        keyword: filters.keyword,
        page: pagination.current - 1,
        size: pagination.pageSize
      });

      if (response.data) {
        setOperators(response.data.content);
        setPagination({
          ...pagination,
          total: response.data.totalElements,
        });
      }
    } catch (error: any) {
      message.error(error.message || t('message.operator.fetchFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOperators();
  }, [pagination.current, pagination.pageSize]);

  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchOperators();
  };

  const handleReset = () => {
    setFilters({
      keyword: '',
      language: undefined,
      status: undefined,
    });
    setPagination({ ...pagination, current: 1 });
    setTimeout(() => fetchOperators(), 0);
  };

  const handleDelete = async (id: number) => {
    try {
      await operatorApi.deleteOperator(id);
      message.success(t('message.operator.deletedSuccess'));
      fetchOperators();
    } catch (error: any) {
      message.error(error.message || t('message.operator.deletedFailed'));
    }
  };

  const columns = [
    {
      title: t('common.id'),
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: t('common.name'),
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text: string, record: Operator) => (
        <a onClick={() => navigate(`/operators/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: t('common.description'),
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 300,
    },
    {
      title: t('operator.language'),
      dataIndex: 'language',
      key: 'language',
      width: 100,
      render: (language: string) => {
        const color = language === 'JAVA' ? 'blue' : 'green';
        return <Tag color={color}>{language}</Tag>;
      },
    },
    {
      title: t('operator.status'),
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const colorMap: Record<string, string> = {
          DRAFT: 'default',
          PUBLISHED: 'success',
          ARCHIVED: 'warning',
        };
        return <Tag color={colorMap[status]}>{status}</Tag>;
      },
    },
    {
      title: t('common.version'),
      dataIndex: 'version',
      key: 'version',
      width: 100,
    },
    {
      title: t('operator.operatorCode'),
      dataIndex: 'operatorCode',
      key: 'operatorCode',
      width: 150,
      render: (text: string) => (
        <Tooltip title={text}>
          <span style={{ fontFamily: 'monospace' }}>{text}</span>
        </Tooltip>
      ),
    },
    {
      title: t('operator.objectCode'),
      dataIndex: 'objectCode',
      key: 'objectCode',
      width: 150,
      render: (text: string) => (
        <Tooltip title={text}>
          <span style={{ fontFamily: 'monospace' }}>{text}</span>
        </Tooltip>
      ),
    },
    {
      title: t('operator.dataFormat'),
      dataIndex: 'dataFormat',
      key: 'dataFormat',
      width: 150,
      render: (dataFormat: string) => (
        <Tooltip title={formatDataFormat(dataFormat)}>
          <span>{formatDataFormat(dataFormat)}</span>
        </Tooltip>
      ),
    },
    {
      title: t('operator.generator'),
      dataIndex: 'generator',
      key: 'generator',
      width: 100,
      render: (generator: string) => {
        if (!generator) return '-';
        const option = GeneratorOptions.find(opt => opt.value === generator);
        return <Tag color={generator === 'dynamic' ? 'blue' : 'green'}>
          {option ? option.label : generator}
        </Tag>;
      },
    },
    {
      title: t('common.createdBy'),
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 120,
    },
    {
      title: t('common.createdAt'),
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: t('common.actions'),
      key: 'actions',
      width: 180,
      fixed: 'right' as const,
      render: (_: any, record: Operator) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/operators/${record.id}`)}
          >
            {t('common.view')}
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/operators/${record.id}/edit`)}
          >
            {t('common.edit')}
          </Button>
          <Popconfirm
            title={t('message.operator.deleteConfirm')}
            onConfirm={() => handleDelete(record.id)}
            okText={t('common.yes')}
            cancelText={t('common.no')}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              {t('common.delete')}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={
          <Space>
            <span>算子列表</span>
            <Tag color="blue">{t('common.total')} {pagination.total}</Tag>
          </Space>
        }
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/operators/create')}
          >
            {t('common.create')} 算子
          </Button>
        }
      >
        {/* Search and Filters */}
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder={t('placeholder.searchOperators')}
            prefix={<SearchOutlined />}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Select
            placeholder={t('placeholder.selectLanguage')}
            value={filters.language}
            onChange={(value) => setFilters({ ...filters, language: value })}
            style={{ width: 120 }}
            allowClear
          >
            <Select.Option value="JAVA">{t('language.java')}</Select.Option>
            <Select.Option value="GROOVY">{t('language.groovy')}</Select.Option>
          </Select>
          <Select
            placeholder={t('placeholder.selectStatus')}
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
          <Button icon={<ReloadOutlined />} onClick={fetchOperators}>
            {t('common.refresh')}
          </Button>
        </Space>

        {/* Operators Table */}
        <Table
          columns={columns}
          dataSource={operators}
          rowKey="id"
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total) => `${t('common.total')} ${total} ${t('common.items')}`,
            onChange: (page, pageSize) =>
              setPagination({ ...pagination, current: page, pageSize }),
          }}
          scroll={{ x: 1500 }}
        />
      </Card>
    </div>
  );
};

export default OperatorListPage;
