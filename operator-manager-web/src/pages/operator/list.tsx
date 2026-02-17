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
import { operatorApi } from '@/api/operator';

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
    categoryId: undefined,
  });

  const fetchOperators = async () => {
    setLoading(true);
    try {
      const response = await operatorApi.getAllOperators({
        language: filters.language,
        status: filters.status,
        categoryId: filters.categoryId,
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
      message.error(error.message || 'Failed to fetch operators');
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
      categoryId: undefined,
    });
    setPagination({ ...pagination, current: 1 });
    setTimeout(() => fetchOperators(), 0);
  };

  const handleDelete = async (id: number) => {
    try {
      await operatorApi.deleteOperator(id);
      message.success('Operator deleted successfully');
      fetchOperators();
    } catch (error: any) {
      message.error(error.message || 'Failed to delete operator');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text: string, record: Operator) => (
        <a onClick={() => navigate(`/operators/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 300,
    },
    {
      title: 'Language',
      dataIndex: 'language',
      key: 'language',
      width: 100,
      render: (language: string) => {
        const color = language === 'JAVA' ? 'blue' : 'green';
        return <Tag color={color}>{language}</Tag>;
      },
    },
    {
      title: 'Status',
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
      title: 'Category',
      dataIndex: ['category', 'name'],
      key: 'category',
      width: 150,
    },
    {
      title: 'Version',
      dataIndex: 'version',
      key: 'version',
      width: 100,
    },
    {
      title: 'Created By',
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 120,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: 'Actions',
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
            View
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/operators/${record.id}/edit`)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Are you sure you want to delete this operator?"
            onConfirm={() => handleDelete(record.id)}
            okText="Yes"
            cancelText="No"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              Delete
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
            <span>Operators</span>
            <Tag color="blue">{pagination.total} total</Tag>
          </Space>
        }
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/operators/create')}
          >
            Create Operator
          </Button>
        }
      >
        {/* Search and Filters */}
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="Search operators..."
            prefix={<SearchOutlined />}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Select
            placeholder="Language"
            value={filters.language}
            onChange={(value) => setFilters({ ...filters, language: value })}
            style={{ width: 120 }}
            allowClear
          >
            <Select.Option value="JAVA">Java</Select.Option>
            <Select.Option value="GROOVY">Groovy</Select.Option>
          </Select>
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
          <Button icon={<ReloadOutlined />} onClick={fetchOperators}>
            Refresh
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
            showTotal: (total) => `Total ${total} items`,
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
