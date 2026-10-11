import { Component, OnInit } from '@angular/core';
import { LearningService } from '../../services/learning.service';

@Component({
  selector: 'app-leaderboard',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.css']
})
export class LeaderboardComponent implements OnInit {
  leaderboardData: any = {};
  loading = true;
  selectedTab = 0;
  displayedColumns = ['rank', 'name', 'points', 'averageScore', 'exams'];

  constructor(private learningService: LearningService) {}

  ngOnInit(): void {
    this.loadLeaderboard();
  }

  loadLeaderboard(): void {
    this.learningService.getLeaderboard().subscribe({
      next: (data) => {
        this.leaderboardData = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load leaderboard', error);
        this.loading = false;
      }
    });
  }

  getRankIcon(index: number): string {
    switch (index) {
      case 0: return '🥇';
      case 1: return '🥈';
      case 2: return '🥉';
      default: return `#${index + 1}`;
    }
  }
  
  getRankClass(index: number): string {
    switch (index) {
      case 0: return 'gold';
      case 1: return 'silver';
      case 2: return 'bronze';
      default: return '';
    }
  }
  
  getProgressColor(score: number): string {
    if (score >= 90) return 'primary';
    if (score >= 80) return 'accent';
    if (score >= 70) return 'warn';
    return '';
  }
}