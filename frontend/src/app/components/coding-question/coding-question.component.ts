import { Component, Input, OnInit } from '@angular/core';
import { CodeExecutionService } from '../../services/code-execution.service';

@Component({
  selector: 'app-coding-question',
  templateUrl: './coding-question.component.html',
  styleUrls: ['./coding-question.component.css']
})
export class CodingQuestionComponent implements OnInit {
  @Input() question: any;
  
  selectedLanguage = 'java';
  code = '';
  input = '';
  output = '';
  isRunning = false;
  isEvaluating = false;
  evaluationResult: any = null;
  storedEvaluationResults: { [questionId: number]: any } = {};
  
  languages = [
    { value: 'java', label: 'Java' },
    { value: 'python', label: 'Python' },
    { value: 'cpp', label: 'C++' },
    { value: 'c', label: 'C' },
    { value: 'javascript', label: 'JavaScript' }
  ];

  starterCodes: { [key: string]: string } = {
    java: 'import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Your code here\n    }\n}',
    python: '# Your code here\n',
    cpp: '#include <iostream>\nusing namespace std;\n\nint main() {\n    // Your code here\n    return 0;\n}',
    c: '#include <stdio.h>\n\nint main() {\n    // Your code here\n    return 0;\n}',
    javascript: '// Your code here\nconsole.log("Hello World");'
  };

  constructor(private codeExecutionService: CodeExecutionService) {}

  ngOnInit() {
    if (this.question?.language) {
      this.selectedLanguage = this.question.language;
    }
    if (this.question?.starterCode) {
      this.code = this.question.starterCode;
    } else {
      this.code = this.starterCodes[this.selectedLanguage];
    }
  }

  onLanguageChange() {
    // Only use default starter code if question doesn't have custom starter code
    if (!this.question?.starterCode) {
      this.code = this.starterCodes[this.selectedLanguage];
    }
    this.output = '';
  }

  onCodeChange(newCode: string) {
    this.code = newCode;
  }

  runCode() {
    this.isRunning = true;
    this.output = 'Running...';
    
    this.codeExecutionService.executeCode({
      code: this.code,
      language: this.selectedLanguage,
      input: this.input
    }).subscribe({
      next: (response) => {
        this.output = response.stdout || response.stderr || 'No output';
        this.isRunning = false;
      },
      error: (error) => {
        this.output = 'Error: ' + error.message;
        this.isRunning = false;
      }
    });
  }

  evaluateCode() {
    this.isEvaluating = true;
    this.evaluationResult = null;
    
    console.log('Question object:', this.question);
    console.log('Question ID:', this.question?.id);
    
    if (!this.question?.id) {
      this.output = 'Error: Question ID not found';
      this.isEvaluating = false;
      return;
    }
    
    this.codeExecutionService.evaluateTestCases(this.question.id, {
      code: this.code,
      language: this.selectedLanguage
    }).subscribe({
      next: (result) => {
        this.evaluationResult = result;
        // Store the evaluation result globally
        const globalWindow = window as any;
        if (globalWindow.codingEvaluationResults) {
          globalWindow.codingEvaluationResults[this.question.id] = result;
        } else {
          globalWindow.codingEvaluationResults = { [this.question.id]: result };
        }
        this.isEvaluating = false;
      },
      error: (error) => {
        this.output = 'Evaluation failed: ' + error.message;
        this.isEvaluating = false;
      }
    });
  }
}