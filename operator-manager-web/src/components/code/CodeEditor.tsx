import { useEffect, useRef, useState } from 'react';
import * as monaco from 'monaco-editor';

// 配置 Monaco Worker（避免 Web Worker 警告）
// 对于 Groovy 等非官方语言，返回 undefined 避免警告
(window as any).MonacoEnvironment = {
  getWorkerUrl: () => undefined,
  getWorker: () => undefined,
};

/**
 * 注册 Groovy 语言支持
 * 基于 Java 语法规则实现 Groovy 语法高亮
 */
const registerGroovyLanguage = () => {
  // 注册 Groovy 语言
  monaco.languages.register({ id: 'groovy' });

  // 使用 Java 的 Tokenizer 配置
  monaco.languages.setMonarchTokensProvider('groovy', {
    keywords: [
      'abstract', 'continue', 'for', 'new', 'switch', 'assert', 'default', 'goto',
      'package', 'synchronized', 'boolean', 'do', 'if', 'private', 'this', 'break',
      'double', 'implements', 'protected', 'throw', 'byte', 'else', 'import',
      'public', 'throws', 'case', 'enum', 'instanceof', 'return', 'transient',
      'catch', 'extends', 'int', 'short', 'try', 'char', 'final', 'interface',
      'static', 'void', 'class', 'finally', 'long', 'strictfp', 'volatile',
      'const', 'float', 'native', 'super', 'while', 'def', 'in', 'as', 'trait'
    ],

    operators: [
      '=', '>', '<', '!', '~', '?', ':', '==', '<=', '>=', '!=', '&&', '||', '++', '--',
      '+', '-', '*', '/', '&', '|', '^', '%', '(', ')', '[', ']', '{', '}', '.', ',',
      ';', '@', '?.', '*.', '*:', '..'
    ],

    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    // C# style strings
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

    // The main tokenizer for our languages
    tokenizer: {
      root: [
        [/[a-z_$][\w$]*/, {
          cases: {
            '@keywords': { token: 'keyword.$0' },
            '@default': 'identifier'
          }
        }],
        [/[A-Z][\w\$]*/, 'type.identifier'],  // to mark class names

        // whitespace
        { include: '@whitespace' },

        // delimiters and operators
        [/[{}()\[\]]/, '@brackets'],
        [/@symbols/, {
          cases: {
            '@operators': 'operator',
            '@default': ''
          }
        }],

        // numbers
        [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
        [/\d+/, 'number'],

        // delimiter: after number because of .\d floats
        [/[;,.]/, 'delimiter'],

        // strings
        [/"([^"\\]|\\.)*$/, 'string.invalid'],  // non-teminated string
        [/"/, 'string', '@string'],
        [/'''/, 'string', '@tripleString'],

        // characters
        [/'[^\\']'/, 'string'],
        [/(')(@escapes)(')/, ['string', 'string.escape', 'string']],
        [/'/, 'string.invalid']
      ],

      whitespace: [
        [/[ \t\r\n]+/, 'white'],
        [/\/\*/, 'comment', '@comment'],
        [/\/\/.*$/, 'comment'],
      ],

      comment: [
        [/[^\/*]+/, 'comment'],
        [/\/\*/, 'comment', '@push'],
        ['\\*/', 'comment', '@pop'],
        [/[\/*]/, 'comment']
      ],

      string: [
        [/[^\\"]+/, 'string'],
        [/@escapes/, 'string.escape'],
        [/\\./, 'string.escape.invalid'],
        [/"/, 'string', '@pop']
      ],

      tripleString: [
        [/'''/, 'string', '@pop'],
        [/[^\\']+/, 'string'],
        [/\\./, 'string.escape.invalid']
      ],
    },
  });

  // 配置语言特性
  monaco.languages.setLanguageConfiguration('groovy', {
    comments: {
      lineComment: '//',
      blockComment: ['/*', '*/']
    },
    brackets: [
      ['{', '}'],
      ['[', ']'],
      ['(', ')']
    ],
    autoClosingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"' },
      { open: "'", close: "'" },
      { open: '`', close: '`' },
    ],
    surroundingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"' },
      { open: "'", close: "'" },
      { open: '<', close: '>' },
    ],
    folding: {
      markers: {
        start: /^\s*\/\/\s*#region\b/,
        end: /^\s*\/\/\s*#endregion\b/
      }
    }
  });
};

// 初始化时注册 Groovy 语言
registerGroovyLanguage();

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
  const isUpdatingValueRef = useRef(false);  // 标记是否正在通过 setValue 更新内容

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
      // 如果正在通过 setValue 更新内容，不触发 onChange 回调
      if (isUpdatingValueRef.current) {
        isUpdatingValueRef.current = false;  // 重置标志
        return;
      }
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
      isUpdatingValueRef.current = true;  // 标记正在通过 setValue 更新
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
