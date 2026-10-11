import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LearningService {
  private apiUrl = 'http://localhost:8080/api/learning';

  constructor(private http: HttpClient) {}

  getRecommendations(userId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/recommendations/${userId}`);
  }

  getUserProgress(userId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/progress/${userId}`);
  }

  getLeaderboard(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/leaderboard`);
  }

  updateLearningProfile(userId: number, learningStyle: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/profile/${userId}`, { learningStyle });
  }
}