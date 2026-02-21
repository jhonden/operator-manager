import { Modal, Descriptions, Tag } from 'antd';
import type { LibraryResponse } from '@/types/library';

interface Props {
  visible: boolean;
  library: LibraryResponse | null;
  onCancel: () => void;
}

// 库类型标签映射
const LibraryTypeLabels: Record<string, string> = {
  CONSTANT: '常量库',
  METHOD: '方法库',
  MODEL: '模型库',
  CUSTOM: '自定义',
};

// 库类型颜色映射
const LibraryTypeColors: Record<string, string> = {
  CONSTANT: 'blue',
  METHOD: 'green',
  MODEL: 'purple',
  CUSTOM: 'orange',
};

/**
 * 公共库基本信息查看弹窗
 */
const LibraryDetailModal: React.FC<Props> = ({
  visible,
  library,
  onCancel,
}) => {
  if (!library) return null;

  return (
    <Modal
      title="公共库详情"
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={600}
    >
      <Descriptions bordered column={2}>
        <Descriptions.Item label="ID">{library.id}</Descriptions.Item>
        <Descriptions.Item label="名称">{library.name}</Descriptions.Item>
        <Descriptions.Item label="类型">
          <Tag color={LibraryTypeColors[library.libraryType]}>
            {LibraryTypeLabels[library.libraryType] || library.libraryType}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label="版本">
          <Tag color="cyan">{library.version}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="分类">
          {library.category || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="文件数">
          {library.files?.length || 0}
        </Descriptions.Item>
        <Descriptions.Item label="使用次数">
          {library.usageCount || 0}
        </Descriptions.Item>
        <Descriptions.Item label="创建人">
          {library.createdBy || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="创建时间" span={2}>
          {new Date(library.createdAt).toLocaleString('zh-CN')}
        </Descriptions.Item>
        <Descriptions.Item label="更新时间" span={2}>
          {new Date(library.updatedAt).toLocaleString('zh-CN')}
        </Descriptions.Item>
        <Descriptions.Item label="描述" span={2}>
          {library.description || '-'}
        </Descriptions.Item>
      </Descriptions>
    </Modal>
  );
};

export default LibraryDetailModal;
