import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CodeRequest {
  code: string;
  language: string;
  input?: string;
}

export interface CodeResponse {
  stdout: string;
  stderr: string;
  exitCode: number;
  success: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CodeExecutionService {
  private apiUrl = 'http://localhost:8080/api/code';

  constructor(private http: HttpClient) {}

  executeCode(request: CodeRequest): Observable<CodeResponse> {
    return this.http.post<CodeResponse>(`${this.apiUrl}/run`, request);
  }

  getSupportedLanguages(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/languages`);
  }

  evaluateTestCases(questionId: number, request: CodeRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/evaluate/${questionId}`, request);
  }
}