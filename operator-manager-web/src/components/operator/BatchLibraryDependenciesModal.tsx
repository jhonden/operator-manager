import React, { useState, useEffect } from 'react';
import { Modal, Transfer, message } from 'antd';
import { operatorApi } from '@/api/operator';
import { libraryApi } from '@/api/library';
import type { LibraryResponse, Operator } from '@/types';

interface BatchLibraryDependenciesModalProps {
  visible: boolean;
  selectedOperators: Operator[];
  onCancel: () => void;
  onSuccess: () => void;
}

/**
 * 批量更新算子公共库依赖弹窗
 */
const BatchLibraryDependenciesModal: React.FC<BatchLibraryDependenciesModalProps> = ({
  visible,
  selectedOperators,
  onCancel,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [libraries, setLibraries] = useState<LibraryResponse[]>([]);
  const [targetKeys, setTargetKeys] = useState<string[]>([]);

  // 加载公共库列表
  useEffect(() => {
    if (visible) {
      console.log('[Batch Library Dependencies Modal] 加载公共库列表');
      fetchLibraries();
    }
  }, [visible]);

  const fetchLibraries = async () => {
    try {
      const response = await libraryApi.searchLibraries({
        page: 0,
        size: 100,
      });
      if (response.data) {
        setLibraries(response.data.content);
        console.log('[Batch Library Dependencies Modal] 公共库列表加载成功, 数量:', response.data.content.length);
      }
    } catch (error: any) {
      console.error('[Batch Library Dependencies Modal] 加载公共库列表失败:', error);
      message.error(error.message || '加载公共库列表失败');
    }
  };

  const handleOk = async () => {
    if (selectedOperators.length === 0) {
      message.warning('请先选择算子');
      return;
    }

    if (targetKeys.length === 0) {
      message.warning('请至少选择一个公共库');
      return;
    }

    setLoading(true);
    try {
      const operatorIds = selectedOperators.map(op => op.id);
      const libraryIds = targetKeys.map(key => Number(key));

      console.log('[Batch Library Dependencies Modal] 开始批量更新, operatorIds:', operatorIds, 'libraryIds:', libraryIds);

      await operatorApi.batchUpdateLibraryDependencies({
        operatorIds,
        libraryIds,
      });

      message.success('批量更新公共库依赖成功');
      console.log('[Batch Library Dependencies Modal] 批量更新成功');
      onSuccess();
      setTargetKeys([]);
    } catch (error: any) {
      console.error('[Batch Library Dependencies Modal] 批量更新失败:', error);
      message.error(error.message || '批量更新公共库依赖失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    console.log('[Batch Library Dependencies Modal] 取消批量更新');
    setTargetKeys([]);
    onCancel();
  };

  const handleChange = (targetKeys: React.Key[], _direction: 'left' | 'right', _moveKeys: React.Key[]) => {
    console.log('[Batch Library Dependencies Modal] 选择的公共库变化:', targetKeys);
    setTargetKeys(targetKeys as string[]);
  };

  return (
    <Modal
      title="批量更新算子公共库依赖"
      open={visible}
      onOk={handleOk}
      onCancel={handleCancel}
      confirmLoading={loading}
      width={800}
      okText="确定"
      cancelText="取消"
    >
      <div style={{ marginBottom: 16 }}>
        <p>
          已选择 <strong>{selectedOperators.length}</strong> 个算子：
        </p>
        <div style={{ maxHeight: 100, overflowY: 'auto', marginBottom: 16, padding: '8px', backgroundColor: '#f5f5f5', borderRadius: '4px' }}>
          {selectedOperators.map(op => (
            <span key={op.id} style={{ display: 'inline-block', margin: '4px' }}>
              <span style={{ backgroundColor: '#1890ff', color: 'white', padding: '2px 8px', borderRadius: '4px', fontSize: '12px' }}>
                {op.name}
              </span>
            </span>
          ))}
        </div>
      </div>

      <Transfer
        dataSource={libraries.map(lib => ({ key: String(lib.id), ...lib }))}
        titles={['可选公共库', '已选择公共库']}
        targetKeys={targetKeys}
        onChange={handleChange}
        render={item => (
          <div>
            <div style={{ fontWeight: 'bold' }}>{item.name}</div>
            <div style={{ fontSize: '12px', color: '#999' }}>
              {item.category} · {item.version}
            </div>
          </div>
        )}
        listStyle={{
          width: 300,
          height: 400,
        }}
        showSearch
        filterOption={(inputValue, item) =>
          item.name.toLowerCase().includes(inputValue.toLowerCase()) ||
          item.category.toLowerCase().includes(inputValue.toLowerCase())
        }
      />
    </Modal>
  );
};

export default BatchLibraryDependenciesModal;
