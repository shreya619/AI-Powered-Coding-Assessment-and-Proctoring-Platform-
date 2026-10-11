import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Exam, ExamResult } from '../models/exam.model';

@Injectable({
  providedIn: 'root'
})
export class ExamService {
  private apiUrl = 'http://localhost:8080/api/exams';

  constructor(private http: HttpClient) {}

  getActiveExams(): Observable<Exam[]> {
    return this.http.get<Exam[]>(this.apiUrl);
  }
  
  getExamById(examId: number): Observable<Exam> {
    return this.http.get<Exam>(`${this.apiUrl}/${examId}`);
  }

  createExam(exam: any): Observable<Exam> {
    return this.http.post<Exam>('http://localhost:8080/api/exam-management/create', exam);
  }
  
  getTeacherExams(): Observable<Exam[]> {
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    const teacherId = currentUser.id;
    return this.http.get<Exam[]>(`http://localhost:8080/api/exam-management/teacher/${teacherId}`);
  }

  startExam(examId: number, userId: number): Observable<ExamResult> {
    return this.http.post<ExamResult>(`${this.apiUrl}/${examId}/start`, { userId });
  }

  submitExam(resultId: number, score: number): Observable<ExamResult> {
    return this.http.post<ExamResult>(`${this.apiUrl}/results/${resultId}/submit`, { score });
  }

  submitExamWithAnswers(resultId: number, submissionData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/results/${resultId}/submit-detailed`, submissionData);
  }

  getUserExamResults(userId: number): Observable<ExamResult[]> {
    return this.http.get<ExamResult[]>(`${this.apiUrl}/user/${userId}/results`);
  }

  submitCodingExam(resultId: number, submissionData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/results/${resultId}/submit-coding`, submissionData);
  }
}