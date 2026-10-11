import { Component, OnInit, ViewChild, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import loader from '@monaco-editor/loader';

declare const monaco: any;

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.css']
})
export class CodeEditorComponent implements OnInit {
  @ViewChild('editorContainer', { static: true }) editorContainer!: ElementRef;
  @Input() language: string = 'java';
  @Input() code: string = '';
  @Output() codeChange = new EventEmitter<string>();

  private editor!: any;

  ngOnInit() {
    this.initEditor();
  }

  private async initEditor() {
    const monacoInstance = await loader.init();
    const editorOptions: any = {
      value: this.code,
      language: this.getMonacoLanguage(this.language),
      theme: 'vs-dark',
      automaticLayout: true,
      minimap: { enabled: false },
      scrollBeyondLastLine: false,
      fontSize: 14
    };

    this.editor = monacoInstance.editor.create(this.editorContainer.nativeElement, editorOptions);
    
    this.editor.onDidChangeModelContent(() => {
      this.codeChange.emit(this.editor.getValue());
    });
  }

  private getMonacoLanguage(lang: string): string {
    const langMap: { [key: string]: string } = {
      'java': 'java',
      'python': 'python',
      'cpp': 'cpp',
      'c': 'c',
      'javascript': 'javascript'
    };
    return langMap[lang] || 'plaintext';
  }

  async updateLanguage(newLanguage: string) {
    this.language = newLanguage;
    const monacoInstance = await loader.init();
    monacoInstance.editor.setModelLanguage(this.editor.getModel()!, this.getMonacoLanguage(newLanguage));
  }

  setValue(value: string) {
    this.editor.setValue(value);
  }

  getValue(): string {
    return this.editor.getValue();
  }
}