import { useState, useEffect } from 'react';
import { Modal, Table, Button, Space, Input, Tag, message, Select } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import type { LibraryResponse, LibraryType } from '@/types/library';
import { libraryApi } from '@/api/library';

const { Option } = Select;

interface LibrarySelectorModalProps {
  visible: boolean;
  onCancel: () => void;
  onLibrarySelect: (selectedLibrary: LibraryResponse) => void;
}

/**
 * 公共库选择弹窗
 */
const LibrarySelectorModal: React.FC<LibrarySelectorModalProps> = ({
  visible,
  onCancel,
  onLibrarySelect,
}) => {
  const [libraries, setLibraries] = useState<LibraryResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [keyword, setKeyword] = useState('');
  const [selectedLibraryType, setSelectedLibraryType] = useState<LibraryType | undefined>(undefined);

  // 库类型选项
  const LibraryTypeOptions = [
    { value: 'CONSTANT', label: '常量库' },
    { value: 'METHOD', label: '方法库' },
    { value: 'MODEL', label: '模型库' },
    { value: 'CUSTOM', label: '自定义' },
  ];

  // 获取公共库列表
  const fetchLibraries = async () => {
    setLoading(true);
    try {
      console.log('[Library Selector Modal] Fetching libraries, keyword:', keyword, 'libraryType:', selectedLibraryType);
      const response = await libraryApi.searchLibraries({
        keyword: keyword || undefined,
        libraryType: selectedLibraryType,
        page: 0,
        size: 100,
      });

      if (response.data) {
        console.log('[Library Selector Modal] Libraries fetched:', response.data.content);
        setLibraries(response.data.content);
      }
    } catch (error: any) {
      console.error('[Library Selector Modal] Error fetching libraries:', error);
      message.error(error.message || '获取公共库列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (visible) {
      fetchLibraries();
    }
  }, [visible]);

  // 搜索
  const handleSearch = () => {
    fetchLibraries();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setSelectedLibraryType(undefined);
    fetchLibraries();
  };

  // 选择公共库
  const handleOk = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先选择一个公共库');
      return;
    }

    if (selectedRowKeys.length > 1) {
      message.warning('一次只能选择一个公共库');
      return;
    }

    const selectedId = selectedRowKeys[0] as number;
    const selectedLibrary = libraries.find(lib => lib.id === selectedId);

    if (selectedLibrary && onLibrarySelect) {
      console.log('[Library Selector Modal] Selected library:', selectedLibrary);
      onLibrarySelect(selectedLibrary);
      onCancel();
    }
  };

  // 类型标签颜色
  const getLibraryTypeColor = (type: string) => {
    const colorMap: Record<string, string> = {
      CONSTANT: 'blue',
      METHOD: 'green',
      MODEL: 'purple',
      CUSTOM: 'orange',
    };
    return colorMap[type] || 'default';
  };

  // 表格列
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '公共库名称',
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
    },
    {
      title: '类型',
      dataIndex: 'libraryType',
      key: 'libraryType',
      width: 120,
      render: (type: LibraryType) => (
        <Tag color={getLibraryTypeColor(type)}>
          {LibraryTypeOptions.find(opt => opt.value === type)?.label || type}
        </Tag>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 100,
    },
    {
      title: '文件数',
      dataIndex: 'files',
      key: 'files',
      width: 100,
      render: (files: any[]) => (files?.length || 0),
    },
  ];

  return (
    <Modal
      title="选择公共库"
      open={visible}
      onCancel={onCancel}
      width={1000}
      footer={[
        <Button onClick={onCancel}>取消</Button>,
        <Button type="primary" onClick={handleOk} disabled={selectedRowKeys.length !== 1}>
          确定
        </Button>,
      ]}
    >
      <Space style={{ marginBottom: 16 }}>
        <Input
          placeholder="搜索公共库名称"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onPressEnter={handleSearch}
          style={{ width: 300 }}
          suffix={
            <Button
              type="text"
              icon={<SearchOutlined />}
              onClick={handleSearch}
            >
              搜索
            </Button>
          }
        />
        <Select
          placeholder="所有类型"
          value={selectedLibraryType}
          onChange={setSelectedLibraryType}
          style={{ width: 150 }}
          allowClear
        >
          {LibraryTypeOptions.map(opt => (
            <Option key={opt.value} value={opt.value}>
              {opt.label}
            </Option>
          ))}
        </Select>
        <Button onClick={handleReset}>重置</Button>
      </Space>

      <Table
        rowSelection={{
          type: 'radio',
          selectedRowKeys,
          onChange: (keys) => setSelectedRowKeys(keys as React.Key[]),
        }}
        loading={loading}
        columns={columns}
        dataSource={libraries}
        rowKey="id"
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 个`,
        }}
        scroll={{ y: 400 }}
      />
    </Modal>
  );
};

export default LibrarySelectorModal;
