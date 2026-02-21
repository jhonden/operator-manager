import { useState, useEffect } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  Button,
  message,
  Space,
} from 'antd';
import type { LibraryResponse, LibraryRequest } from '@/types/library';
import { libraryApi } from '@/api/library';

const { TextArea } = Input;

// 库类型选项
const LibraryTypeOptions = [
  { value: 'CONSTANT', label: '常量库' },
  { value: 'METHOD', label: '方法库' },
  { value: 'MODEL', label: '模型库' },
  { value: 'CUSTOM', label: '自定义' },
];

interface Props {
  visible: boolean;
  library: LibraryResponse | null;
  onCancel: () => void;
  onSuccess: () => void;
}

/**
 * 公共库创建/编辑弹窗（仅基本信息）
 */
const LibraryFormModal: React.FC<Props> = ({
  visible,
  library,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm<LibraryRequest>();
  const [loading, setLoading] = useState(false);

  // 初始化表单数据
  useEffect(() => {
    if (visible) {
      if (library) {
        // 编辑模式
        form.setFieldsValue({
          name: library.name,
          description: library.description,
          version: library.version,
          category: library.category,
          libraryType: library.libraryType,
        });
      } else {
        // 新建模式
        form.resetFields();
        form.setFieldsValue({
          libraryType: 'METHOD' as any,
          version: '1.0',
        });
      }
    }
  }, [visible, library, form]);

  // 提交
  const handleSubmit = async () => {
    try {
      // 验证表单
      const values = await form.validateFields();

      setLoading(true);
      const request: LibraryRequest = {
        ...values,
        files: library?.files || [], // 编辑时保留原有文件，新建时传空数组
      };

      if (library) {
        // 更新
        await libraryApi.updateLibrary(library.id, request);
        message.success('公共库更新成功');
      } else {
        // 创建
        await libraryApi.createLibrary(request);
        message.success('公共库创建成功');
      }

      onSuccess();
      onCancel();
      form.resetFields();
    } catch (error: any) {
      console.error('提交失败:', error);
      if (error.errorFields) {
        // 表单验证错误
        return;
      }
      message.error(error.message || '操作失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={library ? '编辑公共库' : '新建公共库'}
      open={visible}
      onCancel={onCancel}
      onOk={handleSubmit}
      confirmLoading={loading}
      width={600}
      okText="保存"
      cancelText="取消"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="库名称"
          name="name"
          rules={[
            { required: true, message: '请输入库名称' },
            { max: 255, message: '名称不能超过 255 个字符' },
          ]}
        >
          <Input placeholder="请输入库名称" />
        </Form.Item>

        <Form.Item
          label="库类型"
          name="libraryType"
          rules={[{ required: true, message: '请选择库类型' }]}
        >
          <Select placeholder="请选择库类型">
            {LibraryTypeOptions.map(option => (
              <Select.Option key={option.value} value={option.value}>
                {option.label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          label="版本号"
          name="version"
          rules={[{ required: true, message: '请输入版本号' }]}
        >
          <Input placeholder="例如: 1.0" />
        </Form.Item>

        <Form.Item label="分类" name="category">
          <Input placeholder="例如: 工具类" />
        </Form.Item>

        <Form.Item label="描述" name="description">
          <TextArea
            rows={4}
            placeholder="请输入描述信息"
            maxLength={1000}
            showCount
          />
        </Form.Item>

        {!library && (
          <div style={{
            marginTop: '16px',
            padding: '12px',
            backgroundColor: '#fffbe6',
            border: '1px solid #ffe58f',
            borderRadius: '4px',
          }}>
            <Space direction="vertical" size={4}>
              <span style={{ fontWeight: 600 }}>
                💡 提示
              </span>
              <span>公共库创建成功后，可在列表页点击"编辑代码"按钮添加和管理代码文件</span>
            </Space>
          </div>
        )}
      </Form>
    </Modal>
  );
};

export default LibraryFormModal;
