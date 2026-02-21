import { useState, useEffect, useRef } from 'react';
import { Button, message, Modal, Input } from 'antd';
import { PlusOutlined, DeleteOutlined, SaveOutlined, FileOutlined, CheckCircleOutlined, EditOutlined, CheckOutlined } from '@ant-design/icons';
import { libraryApi } from '@/api/library';
import type { LibraryResponse, LibraryFileResponse } from '@/types/library';
import CodeEditor from '@/components/code/CodeEditor';
import { useParams, useNavigate } from 'react-router-dom';

/**
 * 文件状态
 */
interface FileState {
  data: LibraryFileResponse;
  isDirty: boolean;  // 是否有未保存的修改（草稿状态）
  isSaving: boolean;  // 是否正在保存
  originalCode: string;  // 从数据库查询出来的原始 code 值
}

/**
 * 公共库代码编辑页面
 * 类似 VS Code 的左侧文件列表 + 右侧代码编辑器布局
 */
const LibraryCodeEditorPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [library, setLibrary] = useState<LibraryResponse | null>(null);
  const [activeFileIndex, setActiveFileIndex] = useState<number>(0);
  const [fileStates, setFileStates] = useState<FileState[]>([]);
  const activeFileIndexRef = useRef(activeFileIndex);  // 使用 ref 保存最新的 activeFileIndex

  // 文件名编辑相关状态
  const [addFileModalVisible, setAddFileModalVisible] = useState(false);
  const [newFileName, setNewFileName] = useState('');
  const [editingFileId, setEditingFileId] = useState<number | null>(null);  // 正在编辑文件名的文件 ID
  const [editingFileName, setEditingFileName] = useState('');  // 编辑中的文件名

  // 加载公共库详情
  const fetchLibrary = async () => {
    setLoading(true);
    try {
      const response = await libraryApi.getLibraryById(Number(id));
      if (response.data) {
        setLibrary(response.data);

        // 初始化文件状态
        const libraryFiles = response.data.files || [];
        const states: FileState[] = libraryFiles.map((file) => ({
          data: {
            ...file,
            code: file.code || '',
          },
          isDirty: false,
          isSaving: false,
          originalCode: file.code || '',  // 保存原始值用于比较
        }));

        setFileStates(states);

        // 默认选中第一个文件
        if (states.length > 0) {
          setActiveFileIndex(0);
        }
      }
    } catch (error: any) {
      message.error(error.message || '加载公共库失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLibrary();
  }, [id]);

  // 同步 activeFileIndexRef 和 activeFileIndex
  useEffect(() => {
    activeFileIndexRef.current = activeFileIndex;
  }, [activeFileIndex]);

  // 显示添加文件对话框
  const handleShowAddFileModal = () => {
    setNewFileName('');
    setAddFileModalVisible(true);
  };

  // 确认添加文件
  const handleConfirmAddFile = async () => {
    if (!library || !newFileName.trim()) {
      message.warning('请输入文件名');
      return;
    }

    // 检查文件名是否已存在
    const fileNameExists = fileStates.some(state => state.data.fileName === newFileName.trim());
    if (fileNameExists) {
      message.warning('文件名已存在');
      return;
    }

    const fileName = newFileName.trim();
    const orderIndex = fileStates.length;

    try {
      const response = await libraryApi.createLibraryFile(library.id, {
        fileName,
        orderIndex,
      });

      if (response.success && response.data) {
        // 在数组末尾添加新文件，确保 code 字段有默认值
        const newFile: FileState = {
          data: {
            ...response.data,
            code: response.data.code || '',
          },
          isDirty: false,
          isSaving: false,
          originalCode: response.data.code || '',
        };

        // 使用函数式更新确保状态更新的原子性
        setFileStates(prevStates => [...prevStates, newFile]);

        // 选中新创建的文件
        setActiveFileIndex(orderIndex);

        // 关闭对话框
        setAddFileModalVisible(false);
        message.success('文件添加成功');
      } else {
        message.error('创建文件失败');
      }
    } catch (error: any) {
      message.error(error.message || '添加文件失败');
    }
  };

  // 取消添加文件
  const handleCancelAddFile = () => {
    setAddFileModalVisible(false);
    setNewFileName('');
  };

  // 开始编辑文件名
  const handleStartEditFileName = (fileId: number, fileName: string) => {
    setEditingFileId(fileId);
    setEditingFileName(fileName);
  };

  // 确认编辑文件名
  const handleConfirmEditFileName = async () => {
    if (!library || editingFileId === null) return;

    const fileName = editingFileName.trim();
    if (!fileName) {
      message.warning('文件名不能为空');
      return;
    }

    // 检查文件名是否与其他文件重复
    const fileNameExists = fileStates.some(state =>
      state.data.id !== editingFileId && state.data.fileName === fileName
    );
    if (fileNameExists) {
      message.warning('文件名已存在');
      return;
    }

    const fileState = fileStates.find(state => state.data.id === editingFileId);
    if (!fileState || fileState.data.fileName === fileName) {
      setEditingFileId(null);
      setEditingFileName('');
      return;
    }

    try {
      await libraryApi.updateLibraryFileName(library.id, editingFileId, { fileName });

      // 更新文件名
      setFileStates(prevStates =>
        prevStates.map(state =>
          state.data.id === editingFileId
            ? { ...state, data: { ...state.data, fileName } }
            : state
        )
      );

      message.success('文件名已保存');
      setEditingFileId(null);
      setEditingFileName('');
    } catch (error: any) {
      message.error(error.message || '保存文件名失败');
    }
  };

  // 取消编辑文件名
  const handleCancelEditFileName = () => {
    setEditingFileId(null);
    setEditingFileName('');
  };

  // 删除文件（立即调用后端删除）
  const handleDeleteFile = async (fileId: number) => {
    if (!library) return;

    try {
      await libraryApi.deleteLibraryFile(library.id, fileId);
      message.success('文件删除成功');

      // 重新加载库信息
      await fetchLibrary();
    } catch (error: any) {
      console.error('删除文件失败:', error);
      message.error(error.message || '删除文件失败');
    }
  };

  // 更新文件代码内容（使用 ref 获取最新的 activeFileIndex）
  const handleUpdateFileCode = (code: string) => {
    const currentActiveIndex = activeFileIndexRef.current;

    setFileStates(prevStates =>
      prevStates.map((state, i) => {
        if (i === currentActiveIndex) {
          const newCode = code || '';
          const isDirty = newCode !== state.originalCode;
          return { ...state, data: { ...state.data, code: newCode }, isDirty };
        }
        return state;
      })
    );
  };

  // 保存文件代码（使用 ref 获取最新的 activeFileIndex）
  const handleSaveFileCode = async () => {
    const currentActiveIndex = activeFileIndexRef.current;

    if (!library || currentActiveIndex < 0) {
      return;
    }

    // 直接从当前 fileStates 获取 fileState
    const fileState = fileStates[currentActiveIndex];

    if (!fileState || !fileState.isDirty) {
      return;
    }

    const codeToSave = fileState.data.code;

    setFileStates(prevStates => prevStates.map((state, i) =>
      i === currentActiveIndex ? { ...state, isSaving: true } : state
    ));

    try {
      await libraryApi.updateLibraryFileContent(library.id, fileState.data.id, codeToSave);

      // 保存成功后更新状态
      setFileStates(prevStates => prevStates.map((state, i) =>
        i === currentActiveIndex ? { ...state, isDirty: false, isSaving: false, originalCode: codeToSave } : state
      ));
      message.success('文件保存成功');
    } catch (error: any) {
      message.error(error.message || '保存文件失败');
      setFileStates(prevStates => prevStates.map((state, i) =>
        i === currentActiveIndex ? { ...state, isSaving: false } : state
      ));
    }
  };

  // 选择文件
  const handleSelectFile = (index: number) => {
    // 同步更新状态和 ref
    setActiveFileIndex(index);
    activeFileIndexRef.current = index;
  };

  // 返回列表页
  const handleBack = () => {
    // 检查是否有未保存的草稿
    const hasDirtyFiles = fileStates.some(state => state.isDirty);
    if (hasDirtyFiles) {
      message.warning('有未保存的文件，请先保存');
      return;
    }

    navigate('/libraries');
  };

  // Ctrl+S 保存快捷键
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      handleSaveFileCode();
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px 0' }}>加载中...</div>;
  }

  const currentFile = fileStates[activeFileIndex]?.data;
  const currentFileState = fileStates[activeFileIndex];

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }} onKeyDown={handleKeyDown}>
      {/* 顶部工具栏 */}
      <div style={{
        padding: '12px 24px',
        borderBottom: '1px solid #f0f0f0',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>
        <div>
          <h2 style={{ margin: 0 }}>
            {library?.name} <span style={{ fontSize: 14, color: '#999', fontWeight: 'normal' }}> - 代码编辑</span>
          </h2>
          <span style={{ marginLeft: 16, color: '#666', fontSize: 12 }}>
            {fileStates.length} 个文件 • {fileStates.filter(s => s.isDirty).length} 个未保存
          </span>
        </div>
        <div>
          <Button onClick={handleBack}>返回列表</Button>
          <Button
            type="primary"
            icon={<SaveOutlined />}
            onClick={handleSaveFileCode}
            disabled={!currentFile || !currentFileState?.isDirty}
          >
            保存 (Ctrl+S)
          </Button>
        </div>
      </div>

      {/* 主内容区域 */}
      <div style={{ flex: 1, display: 'flex', overflow: 'hidden' }}>
        {/* 左侧文件列表 */}
        <div style={{
          width: 300,
          borderRight: '1px solid #f0f0f0',
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: '#f5f5f5',
        }}>
          {/* 文件列表头部 */}
          <div style={{
            padding: '12px',
            borderBottom: '1px solid #e8e8e8',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}>
            <span style={{ fontWeight: 600 }}>文件列表</span>
            <Button
              type="primary"
              size="small"
              icon={<PlusOutlined />}
              onClick={handleShowAddFileModal}
            >
              添加文件
            </Button>
          </div>

          {/* 文件列表 */}
          <div style={{ flex: 1, overflowY: 'auto' }}>
            {fileStates.map((state, index) => (
              <div
                key={state.data.id}
                style={{
                  padding: '12px',
                  borderBottom: '1px solid #e8e8e8',
                  cursor: 'pointer',
                  backgroundColor: activeFileIndex === index ? '#e6f7ff' : 'transparent',
                  transition: 'background-color 0.2s',
                }}
                onClick={() => handleSelectFile(index)}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    {state.isSaving ? (
                      <span style={{ color: '#1890ff', fontSize: 12 }}>
                        保存中...
                      </span>
                    ) : state.isDirty ? (
                      <EditOutlined style={{ color: '#faad14', fontSize: 12 }} />
                    ) : (
                      <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 12 }} />
                    )}
                  </div>
                  <span style={{ fontSize: 12, color: '#999' }}>
                    {(state.data.code || '').length} 字符
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  {editingFileId === state.data.id ? (
                    // 编辑状态：显示输入框和确认按钮
                    <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 8 }}>
                      <Input
                        size="small"
                        defaultValue={editingFileName}
                        autoFocus
                        onBlur={handleConfirmEditFileName}
                        onChange={(e) => setEditingFileName(e.target.value)}
                        onPressEnter={handleConfirmEditFileName}
                        onClick={(e) => e.stopPropagation()}
                        style={{ flex: 1 }}
                      />
                      <Button
                        type="text"
                        size="small"
                        icon={<CheckOutlined />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleConfirmEditFileName();
                        }}
                        style={{ color: '#52c41a' }}
                      />
                    </div>
                  ) : (
                    // 非编辑状态：显示文件名和编辑按钮
                    <>
                      <div style={{ flex: 1, fontSize: 12, display: 'flex', alignItems: 'center' }}>
                        <FileOutlined style={{ marginRight: 4 }} />
                        {state.data.fileName}
                      </div>
                      <Button
                        type="text"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleStartEditFileName(state.data.id, state.data.fileName);
                        }}
                        style={{ color: '#1890ff' }}
                      />
                    </>
                  )}
                  <Button
                    type="text"
                    size="small"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteFile(state.data.id);
                    }}
                  >
                    删除
                  </Button>
                </div>
              </div>
            ))}

            {fileStates.length === 0 && (
              <div style={{
                textAlign: 'center',
                padding: '40px 0',
                color: '#999',
              }}>
                暂无文件，点击"添加文件"按钮添加代码文件
              </div>
            )}
          </div>
        </div>

        {/* 右侧代码编辑器 */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          {currentFile ? (
            <>
              <div style={{
                padding: '8px 16px',
                borderBottom: '1px solid #f0f0f0',
                backgroundColor: '#fafafa',
                fontSize: 12,
                color: '#666',
              }}>
                <span style={{ marginRight: 16 }}>
                  <FileOutlined /> {currentFile.fileName}
                </span>
                <span style={{ color: '#999' }}>
                  {currentFileState?.isDirty ? '（已修改，未保存）' : '（已保存）'}
                </span>
              </div>
              <CodeEditor
                language="groovy"
                value={currentFile.code}
                onChange={handleUpdateFileCode}
                height="calc(100vh - 120px)"
              />
            </>
          ) : (
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              height: '100%',
              color: '#999',
            }}>
              请选择一个文件进行编辑
            </div>
          )}
        </div>
      </div>

      {/* 添加文件对话框 */}
      <Modal
        title="添加文件"
        open={addFileModalVisible}
        onOk={handleConfirmAddFile}
        onCancel={handleCancelAddFile}
        okText="确定"
        cancelText="取消"
      >
        <Input
          placeholder="请输入文件名（例如：Example.groovy）"
          value={newFileName}
          onChange={(e) => setNewFileName(e.target.value)}
          onPressEnter={handleConfirmAddFile}
          autoFocus
        />
      </Modal>
    </div>
  );
};

export default LibraryCodeEditorPage;
