import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ExamPortalService {
  private apiUrl = 'http://localhost:8080/api/exam-portal';

  constructor(private http: HttpClient) {}

  submitExam(sessionId: number, answers: any, suspiciousActivities: string[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/sessions/${sessionId}/submit`, {
      answers,
      suspiciousActivities
    });
  }

  getExamReport(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/sessions/${sessionId}/report`);
  }
}