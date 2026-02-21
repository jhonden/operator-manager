import { useEffect, useRef, useState } from 'react';
import * as monaco from 'monaco-editor';

// 配置 Monaco Worker（避免 Web Worker 警告）
// 对于 Groovy 等非官方语言，返回 undefined 避免警告
(window as any).MonacoEnvironment = {
  getWorkerUrl: () => undefined,
  getWorker: () => undefined,
};

interface CodeEditorProps {
  language: 'java' | 'groovy' | 'javascript' | 'typescript';
  value?: string;
  onChange?: (value: string) => void;
  onMount?: (editor: any) => void;
  height?: number | string;
  readOnly?: boolean;
  theme?: 'vs-light' | 'vs-dark';
}

/**
 * Monaco Editor component
 */
const CodeEditor: React.FC<CodeEditorProps> = ({
  language,
  value = '',
  onChange,
  onMount,
  height = 500,
  readOnly = false,
  theme = 'vs-light',
}) => {
  const editorRef = useRef<HTMLDivElement>(null);
  const [editor, setEditor] = useState<monaco.editor.IStandaloneCodeEditor | null>(null);

  useEffect(() => {
    if (!editorRef.current) return;

    // Create editor
    const monacoEditor = monaco.editor.create(editorRef.current, {
      value,
      language,
      theme,
      readOnly,
      automaticLayout: true,
      minimap: { enabled: true },
      fontSize: 14,
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      wordWrap: 'on',
      formatOnPaste: true,
      formatOnType: true,
    });

    setEditor(monacoEditor);

    // 通知父组件编辑器已挂载
    onMount?.(monacoEditor);

    // Handle content changes
    const changeHandler = monacoEditor.onDidChangeModelContent(() => {
      const newValue = monacoEditor.getValue();
      onChange?.(newValue);
    });

    // Cleanup
    return () => {
      changeHandler.dispose();
      monacoEditor.dispose();
    };
  }, []);

  // Update editor content when value prop changes
  useEffect(() => {
    if (editor && value !== editor.getValue()) {
      editor.setValue(value);
    }
  }, [value, editor]);

  // Update language when it changes
  useEffect(() => {
    if (editor) {
      const model = editor.getModel();
      if (model) {
        monaco.editor.setModelLanguage(model, language);
      }
    }
  }, [language, editor]);

  // Update theme when it changes
  useEffect(() => {
    if (editor) {
      monaco.editor.setTheme(theme);
    }
  }, [theme, editor]);

  // Update read-only state
  useEffect(() => {
    if (editor) {
      editor.updateOptions({ readOnly });
    }
  }, [readOnly, editor]);

  return (
    <div
      ref={editorRef}
      style={{
        height: typeof height === 'number' ? `${height}px` : height,
        border: '1px solid #d9d9d9',
        borderRadius: '4px',
        overflow: 'hidden',
      }}
    />
  );
};

export default CodeEditor;
