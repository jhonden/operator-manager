import { useState, useEffect } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  Button,
  Space,
  Card,
  message,
  Popconfirm,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import type { LibraryResponse, LibraryRequest, LibraryFileRequest } from '@/types/library';
import { libraryApi } from '@/api/library';
import CodeEditor from '@/components/code/CodeEditor';

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
 * 公共库创建/编辑弹窗
 */
const LibraryFormModal: React.FC<Props> = ({
  visible,
  library,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm<LibraryRequest>();
  const [loading, setLoading] = useState(false);
  const [files, setFiles] = useState<LibraryFileRequest[]>([]);
  const [activeFileIndex, setActiveFileIndex] = useState<number | null>(null);

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
        // 转换文件数据
        const libraryFiles: LibraryFileRequest[] = library.files.map((file, index) => ({
          fileName: file.fileName,
          code: file.code,
          orderIndex: index,
        }));
        setFiles(libraryFiles);
        if (libraryFiles.length > 0) {
          setActiveFileIndex(0);
        }
      } else {
        // 新建模式
        form.resetFields();
        form.setFieldsValue({
          libraryType: 'METHOD' as any,
          version: '1.0',
        });
        setFiles([]);
        setActiveFileIndex(null);
      }
    }
  }, [visible, library, form]);

  // 添加文件
  const handleAddFile = () => {
    const newFile: LibraryFileRequest = {
      fileName: `File${files.length + 1}.groovy`,
      code: '',
      orderIndex: files.length,
    };
    setFiles([...files, newFile]);
    setActiveFileIndex(files.length);
  };

  // 删除文件
  const handleDeleteFile = (index: number) => {
    const newFiles = files.filter((_, i) => i !== index);
    // 重新排序
    newFiles.forEach((file, i) => {
      file.orderIndex = i;
    });
    setFiles(newFiles);
    if (activeFileIndex === index) {
      setActiveFileIndex(newFiles.length > 0 ? 0 : null);
    } else if (activeFileIndex !== null && activeFileIndex > index) {
      setActiveFileIndex(activeFileIndex - 1);
    }
  };

  // 上移文件
  const handleMoveUp = (index: number) => {
    if (index === 0) return;
    const newFiles = [...files];
    [newFiles[index - 1], newFiles[index]] = [newFiles[index], newFiles[index - 1]];
    // 更新 orderIndex
    newFiles.forEach((file, i) => {
      file.orderIndex = i;
    });
    setFiles(newFiles);
    if (activeFileIndex === index) {
      setActiveFileIndex(index - 1);
    } else if (activeFileIndex === index - 1) {
      setActiveFileIndex(index);
    }
  };

  // 下移文件
  const handleMoveDown = (index: number) => {
    if (index === files.length - 1) return;
    const newFiles = [...files];
    [newFiles[index], newFiles[index + 1]] = [newFiles[index + 1], newFiles[index]];
    // 更新 orderIndex
    newFiles.forEach((file, i) => {
      file.orderIndex = i;
    });
    setFiles(newFiles);
    if (activeFileIndex === index) {
      setActiveFileIndex(index + 1);
    } else if (activeFileIndex === index + 1) {
      setActiveFileIndex(index);
    }
  };

  // 更新文件名
  const handleUpdateFileName = (index: number, fileName: string) => {
    const newFiles = [...files];
    newFiles[index].fileName = fileName;
    setFiles(newFiles);
  };

  // 更新文件代码
  const handleUpdateFileCode = (code: string) => {
    if (activeFileIndex === null) return;
    const newFiles = [...files];
    newFiles[activeFileIndex].code = code;
    setFiles(newFiles);
  };

  // 提交
  const handleSubmit = async () => {
    try {
      // 验证表单
      const values = await form.validateFields();

      // 验证文件
      if (files.length === 0) {
        message.warning('请至少添加一个文件');
        return;
      }

      // 验证文件名
      const emptyFileName = files.find(f => !f.fileName || f.fileName.trim() === '');
      if (emptyFileName) {
        message.warning('文件名不能为空');
        return;
      }

      // 验证文件代码
      const emptyFileCode = files.find(f => !f.code || f.code.trim() === '');
      if (emptyFileCode) {
        message.warning('请填写所有文件的代码内容');
        return;
      }

      setLoading(true);
      const request: LibraryRequest = {
        ...values,
        files: files,
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
      setFiles([]);
      setActiveFileIndex(null);
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
      width={1000}
      okText="保存"
      cancelText="取消"
    >
      <Form form={form} layout="vertical">
        <Tabs defaultActiveKey="basic">
          <Tabs.TabPane tab="基本信息" key="basic">
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
          </Tabs.TabPane>

          <Tabs.TabPane tab="代码文件" key="files">
            <Card
              size="small"
              title="文件列表"
              extra={
                <Button
                  type="primary"
                  size="small"
                  icon={<PlusOutlined />}
                  onClick={handleAddFile}
                >
                  添加文件
                </Button>
              }
              style={{ marginBottom: 16 }}
            >
              <Space direction="vertical" style={{ width: '100%' }} size={8}>
                {files.map((file, index) => (
                  <Card
                    key={index}
                    size="small"
                    type={activeFileIndex === index ? 'inner' : undefined}
                    onClick={() => setActiveFileIndex(index)}
                    style={{ cursor: 'pointer' }}
                  >
                    <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                      <Input
                        size="small"
                        value={file.fileName}
                        onChange={(e) => handleUpdateFileName(index, e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                        style={{ width: 300 }}
                        placeholder="文件名.groovy"
                      />
                      <Space size="small">
                        <Button
                          size="small"
                          icon={<ArrowUpOutlined />}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleMoveUp(index);
                          }}
                          disabled={index === 0}
                        />
                        <Button
                          size="small"
                          icon={<ArrowDownOutlined />}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleMoveDown(index);
                          }}
                          disabled={index === files.length - 1}
                        />
                        <Popconfirm
                          title="确认删除"
                          description="确定要删除这个文件吗？"
                          onConfirm={(e) => {
                            e?.stopPropagation();
                            handleDeleteFile(index);
                          }}
                          okText="确定"
                          cancelText="取消"
                        >
                          <Button
                            size="small"
                            danger
                            icon={<DeleteOutlined />}
                            onClick={(e) => e.stopPropagation()}
                          />
                        </Popconfirm>
                      </Space>
                    </Space>
                  </Card>
                ))}
              </Space>
            </Card>

            {activeFileIndex !== null && (
              <Card size="small" title="代码编辑">
                <CodeEditor
                  language="groovy"
                  value={files[activeFileIndex]?.code || ''}
                  onChange={handleUpdateFileCode}
                  height={400}
                />
              </Card>
            )}

            {files.length === 0 && (
              <div style={{ textAlign: 'center', color: '#999', padding: '40px' }}>
                暂无文件，请点击"添加文件"按钮添加代码文件
              </div>
            )}
          </Tabs.TabPane>
        </Tabs>
      </Form>
    </Modal>
  );
};

export default LibraryFormModal;
