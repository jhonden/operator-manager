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
import type { LibraryResponse, LibraryType } from '@/types/library';
import { libraryApi } from '@/api/library';
import LibraryFormModal from '@/components/library/LibraryFormModal';
import LibraryDetailModal from '@/components/library/LibraryDetailModal';

// 库类型选项
const LibraryTypeOptions = [
  { value: 'CONSTANT', label: '常量库' },
  { value: 'METHOD', label: '方法库' },
  { value: 'MODEL', label: '模型库' },
  { value: 'CUSTOM', label: '自定义' },
];

/**
 * 过滤条件
 */
interface LibraryFilters {
  keyword: string;
  libraryType?: LibraryType;
}

/**
 * 公共库列表页面
 */
const LibraryListPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [libraries, setLibraries] = useState<LibraryResponse[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });

  // 过滤条件状态
  const [filters, setFilters] = useState<LibraryFilters>({
    keyword: '',
    libraryType: undefined,
  });

  // 弹窗状态
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedLibrary, setSelectedLibrary] = useState<LibraryResponse | null>(null);

  // 获取公共库列表
  const fetchLibraries = async () => {
    setLoading(true);
    try {
      const response = await libraryApi.searchLibraries({
        keyword: filters.keyword || undefined,
        libraryType: filters.libraryType,
        page: pagination.current - 1,
        size: pagination.pageSize,
      });

      if (response.data) {
        setLibraries(response.data.content);
        setPagination({
          ...pagination,
          total: response.data.totalElements,
        });
      }
    } catch (error: any) {
      message.error(error.message || '获取公共库列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLibraries();
  }, [pagination.current, pagination.pageSize]);

  // 搜索
  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchLibraries();
  };

  // 重置
  const handleReset = () => {
    setFilters({
      keyword: '',
      libraryType: undefined,
    });
    setPagination({ ...pagination, current: 1 });
    setTimeout(() => fetchLibraries(), 0);
  };

  // 删除公共库
  const handleDelete = async (id: number) => {
    try {
      await libraryApi.deleteLibrary(id);
      message.success('公共库删除成功');
      fetchLibraries();
    } catch (error: any) {
      message.error(error.message || '删除公共库失败');
    }
  };

  // 查看详情
  const handleViewDetail = (library: LibraryResponse) => {
    setSelectedLibrary(library);
    setDetailModalVisible(true);
  };

  // 编辑
  const handleEdit = (library: LibraryResponse) => {
    setSelectedLibrary(library);
    setEditModalVisible(true);
  };

  // 新建
  const handleCreate = () => {
    setSelectedLibrary(null);
    setCreateModalVisible(true);
  };

  // 刷新
  const handleRefresh = () => {
    fetchLibraries();
  };

  // 表格列定义
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 300,
      render: (text: string) => (
        <Tooltip title={text}>
          <span>{text || '-'}</span>
        </Tooltip>
      ),
    },
    {
      title: '类型',
      dataIndex: 'libraryType',
      key: 'libraryType',
      width: 120,
      render: (type: string) => {
        const option = LibraryTypeOptions.find(opt => opt.value === type);
        const colorMap: Record<string, string> = {
          CONSTANT: 'blue',
          METHOD: 'green',
          MODEL: 'purple',
          CUSTOM: 'orange',
        };
        return <Tag color={colorMap[type]}>{option ? option.label : type}</Tag>;
      },
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 100,
      render: (version: string) => <Tag color="cyan">{version}</Tag>,
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 120,
      render: (text: string) => text || '-',
    },
    {
      title: '文件数',
      dataIndex: 'files',
      key: 'fileCount',
      width: 100,
      render: (files: any[]) => <span>{files?.length || 0}</span>,
    },
    {
      title: '使用次数',
      dataIndex: 'usageCount',
      key: 'usageCount',
      width: 100,
      render: (count: number) => <span>{count || 0}</span>,
    },
    {
      title: '创建人',
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 120,
      render: (text: string) => text || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'actions',
      width: 200,
      fixed: 'right' as const,
      render: (_: any, record: LibraryResponse) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description="确定要删除这个公共库吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
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
            <span>公共库管理</span>
            <Tag color="blue">{pagination.total} 个</Tag>
          </Space>
        }
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            新建公共库
          </Button>
        }
      >
        {/* 搜索和过滤 */}
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="搜索公共库名称..."
            prefix={<SearchOutlined />}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Select
            placeholder="库类型"
            value={filters.libraryType}
            onChange={(value) => setFilters({ ...filters, libraryType: value })}
            style={{ width: 120 }}
            allowClear
          >
            {LibraryTypeOptions.map(option => (
              <Select.Option key={option.value} value={option.value}>
                {option.label}
              </Select.Option>
            ))}
          </Select>
          <Button type="primary" onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>
            重置
          </Button>
          <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
            刷新
          </Button>
        </Space>

        {/* 公共库表格 */}
        <Table
          columns={columns}
          dataSource={libraries}
          rowKey="id"
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) =>
              setPagination({ ...pagination, current: page, pageSize }),
          }}
          scroll={{ x: 1400 }}
        />
      </Card>

      {/* 公共库创建/编辑弹窗 */}
      <LibraryFormModal
        visible={createModalVisible || editModalVisible}
        library={editModalVisible ? selectedLibrary : null}
        onCancel={() => {
          setCreateModalVisible(false);
          setEditModalVisible(false);
          setSelectedLibrary(null);
        }}
        onSuccess={() => {
          fetchLibraries();
          setCreateModalVisible(false);
          setEditModalVisible(false);
          setSelectedLibrary(null);
        }}
      />

      {/* 公共库详情查看弹窗 */}
      <LibraryDetailModal
        visible={detailModalVisible}
        library={selectedLibrary}
        onCancel={() => {
          setDetailModalVisible(false);
          setSelectedLibrary(null);
        }}
      />
    </div>
  );
};

export default LibraryListPage;
