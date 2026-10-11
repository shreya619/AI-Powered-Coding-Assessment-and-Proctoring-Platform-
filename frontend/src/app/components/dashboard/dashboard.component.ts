import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LearningService } from '../../services/learning.service';
import { ExamService } from '../../services/exam.service';
import { User } from '../../models/user.model';
import { Exam } from '../../models/exam.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  recommendations: any = {};
  progress: any = {};
  activeExams: Exam[] = [];
  loading = true;

  constructor(
    public authService: AuthService,
    private learningService: LearningService,
    private examService: ExamService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (this.currentUser) {
      if (this.authService.isTeacher()) {
        this.router.navigate(['/teacher-dashboard']);
        return;
      }
      this.loadDashboardData();
    }
  }

  loadDashboardData(): void {
    if (!this.currentUser) return;

    // Load recommendations for students
    if (this.authService.isStudent()) {
      this.learningService.getRecommendations(this.currentUser.id).subscribe({
        next: (data) => this.recommendations = data,
        error: (error) => console.error('Failed to load recommendations', error)
      });

      this.learningService.getUserProgress(this.currentUser.id).subscribe({
        next: (data) => this.progress = data,
        error: (error) => console.error('Failed to load progress', error)
      });
    }

    // Load active exams from backend
    this.examService.getActiveExams().subscribe({
      next: (exams) => {
        this.activeExams = exams;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load exams', error);
        this.activeExams = [];
        this.loading = false;
      }
    });
  }

  startExam(exam: Exam): void {
    this.router.navigate(['/exam', exam.id]);
  }



  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}