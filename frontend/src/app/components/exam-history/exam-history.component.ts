import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ExamPortalService } from '../../services/exam-portal.service';
import { ExamService } from '../../services/exam.service';

@Component({
  selector: 'app-exam-history',
  templateUrl: './exam-history.component.html',
  styleUrls: ['./exam-history.component.css']
})
export class ExamHistoryComponent implements OnInit {
  examHistory: any[] = [];
  loading = true;
  selectedExam: any = null;
  showDetailedReport = false;

  constructor(
    public authService: AuthService,
    private examPortalService: ExamPortalService,
    private examService: ExamService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadExamHistory();
  }

  ionViewWillEnter(): void {
    // Refresh data when navigating to this page
    this.loadExamHistory();
  }

  loadExamHistory(): void {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      this.loading = false;
      return;
    }

    console.log('Loading exam results for user:', currentUser.id);
    this.examService.getUserExamResults(currentUser.id).subscribe({
      next: (results) => {
        console.log('Received exam results:', results);
        this.examHistory = results.map(result => ({
          id: result.id,
          examTitle: result.exam?.title || 'Unknown Exam',
          dateTaken: new Date(result.submittedAt || result.startedAt),
          score: result.score || 0,
          totalMarks: result.exam?.totalMarks || 0,
          percentage: result.score && result.exam?.totalMarks ? 
            Math.round((result.score / result.exam.totalMarks) * 100) : 0,
          duration: this.calculateDuration(result.startedAt?.toString(), result.submittedAt?.toString()),
          status: result.isCompleted ? 'Completed' : 'In Progress',
          proctoringAlerts: 0,
          grade: this.calculateGrade(result.score, result.exam?.totalMarks),
          questions: [],
          aiRecommendations: result.proctoringReport || 'No AI recommendations available',
          startedAt: result.startedAt,
          submittedAt: result.submittedAt
        }));
        
        console.log('Processed exam history:', this.examHistory);
        
        // Sort by date (newest first)
        this.examHistory.sort((a, b) => new Date(b.dateTaken).getTime() - new Date(a.dateTaken).getTime());
        
        // Add mock data if no real results exist
        if (this.examHistory.length === 0) {
          console.log('No real exam results found, using mock data');
          this.examHistory = this.getMockExamHistory();
        }
        
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load exam history', error);
        this.examHistory = this.getMockExamHistory();
        this.loading = false;
      }
    });
  }

  private calculateDuration(startTime?: string, endTime?: string): string {
    if (!startTime || !endTime) return 'N/A';
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end.getTime() - start.getTime();
    const diffMins = Math.round(diffMs / (1000 * 60));
    return `${diffMins} minutes`;
  }

  private calculateGrade(score: number, totalMarks: number): string {
    if (!score || !totalMarks) return 'N/A';
    const percentage = (score / totalMarks) * 100;
    if (percentage >= 95) return 'A+';
    if (percentage >= 90) return 'A';
    if (percentage >= 85) return 'A-';
    if (percentage >= 80) return 'B+';
    if (percentage >= 75) return 'B';
    if (percentage >= 70) return 'B-';
    if (percentage >= 65) return 'C+';
    if (percentage >= 60) return 'C';
    return 'F';
  }

  private getMockExamHistory(): any[] {
    return [
      {
        id: 1,
        examTitle: 'Java Programming Basics',
        dateTaken: new Date('2024-11-10T10:30:00'),
        score: 42,
        totalMarks: 50,
        percentage: 84,
        duration: '38 minutes',
        status: 'Completed',
        proctoringAlerts: 1,
        grade: 'A',
        questions: []
      }
    ];
  }

  viewDetailedReport(exam: any): void {
    this.selectedExam = exam;
    this.showDetailedReport = true;
  }

  closeDetailedReport(): void {
    this.showDetailedReport = false;
    this.selectedExam = null;
  }

  getGradeColor(grade: string): string {
    switch (grade) {
      case 'A': case 'A+': return '#4caf50';
      case 'A-': case 'B+': return '#8bc34a';
      case 'B': case 'B-': return '#ff9800';
      case 'C': case 'C+': return '#ff5722';
      default: return '#f44336';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'Completed': return '#4caf50';
      case 'In Progress': return '#ff9800';
      case 'Failed': return '#f44336';
      default: return '#9e9e9e';
    }
  }

  downloadReport(exam: any): void {
    // Generate and download PDF report
    console.log('Downloading report for exam:', exam.examTitle);
  }

  retakeExam(exam: any): void {
    // Navigate to exam retake
    console.log('Retaking exam:', exam.examTitle);
  }

  getAverageScore(): number {
    if (this.examHistory.length === 0) return 0;
    const total = this.examHistory.reduce((sum, exam) => sum + exam.percentage, 0);
    return Math.round(total / this.examHistory.length);
  }

  getBestScore(): number {
    if (this.examHistory.length === 0) return 0;
    return Math.max(...this.examHistory.map(exam => exam.percentage));
  }

  refreshHistory(): void {
    this.loading = true;
    this.examHistory = [];
    this.loadExamHistory();
  }

  formatAIRecommendations(recommendations: string): string {
    if (!recommendations) return 'No AI recommendations available';
    
    try {
      const parsed = JSON.parse(recommendations);
      if (parsed.error) {
        return this.formatMarkdown(recommendations);
      }
      
      let formatted = '<div class="ai-insights">';
      
      if (parsed.examType) {
        formatted += `<h3>📚 Subject: ${parsed.examType}</h3>`;
      }
      
      if (parsed.detectedTopics && parsed.detectedTopics.length > 0) {
        formatted += '<h4>🎯 Topics Covered:</h4><ul>';
        parsed.detectedTopics.forEach((topic: string) => {
          formatted += `<li>${topic}</li>`;
        });
        formatted += '</ul>';
      }
      
      if (parsed.weakTopics && parsed.weakTopics.length > 0) {
        formatted += '<h4>⚠️ Areas for Improvement:</h4><ul>';
        parsed.weakTopics.forEach((topic: string) => {
          formatted += `<li><strong>${topic}</strong></li>`;
        });
        formatted += '</ul>';
      }
      
      if (parsed.studyTips) {
        formatted += `<h4>💡 Study Recommendations:</h4><p>${parsed.studyTips}</p>`;
      }
      
      formatted += '</div>';
      return formatted;
    } catch (e) {
      return this.formatMarkdown(recommendations);
    }
  }
  
  private formatMarkdown(text: string): string {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>')
      .replace(/•/g, '&bull;')
      .replace(/#{1,3}\s+(.*?)(<br>|$)/g, '<h4>$1</h4>')
      .replace(/- (.*?)(<br>|$)/g, '<li>$1</li>');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}