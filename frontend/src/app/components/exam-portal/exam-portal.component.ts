import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExamService } from '../../services/exam.service';
import { AuthService } from '../../services/auth.service';
import { ExamPortalService } from '../../services/exam-portal.service';
import { FaceDetectionService } from '../../services/face-detection.service';
import { Exam, Question } from '../../models/exam.model';

@Component({
  selector: 'app-exam-portal',
  templateUrl: './exam-portal.component.html',
  styleUrls: ['./exam-portal.component.css']
})
export class ExamPortalComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement', { static: false }) videoElement!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvas', { static: false }) canvasElement!: ElementRef<HTMLCanvasElement>;
  
  exam: Exam | null = null;
  questions: Question[] = [];
  currentQuestionIndex = 0;
  answers: { [key: number]: string } = {};
  timeRemaining = 0;
  timerInterval: any;
  
  // Proctoring
  mediaStream: MediaStream | null = null;
  mediaRecorder: MediaRecorder | null = null;
  recordedChunks: Blob[] = [];
  proctoringActive = false;
  suspiciousActivities: string[] = [];
  
  // Event listeners for cleanup
  private visibilityListener: any;
  private keydownListener: any;
  private mouseleaveListener: any;
  private blurListener: any;
  private contextmenuListener: any;
  private copyListener: any;
  private pasteListener: any;
  private faceDetectionInterval: any;
  
  // UI State
  examStarted = false;
  examSubmitted = false;
  loading = true;
  submitting = false;
  
  // Results
  examResult: any = null;
  examResultId: number | null = null;
  sessionId = Date.now();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private examService: ExamService,
    private examPortalService: ExamPortalService,
    private faceDetectionService: FaceDetectionService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const examId = this.route.snapshot.params['id'];
    this.loadExam(examId);
  }

  ngOnDestroy(): void {
    this.stopTimer();
    this.stopProctoring();
    this.removeEventListeners();
  }

  loadExam(examId: number): void {
    this.examService.getExamById(examId).subscribe({
      next: (exam) => {
        this.exam = exam;
        this.questions = exam.questions || [];
        this.timeRemaining = exam.duration * 60;
        this.loading = false;
        console.log('Loaded exam:', exam);
        console.log('Questions:', this.questions);
      },
      error: (error) => {
        console.error('Failed to load exam:', error);
        this.loading = false;
      }
    });
  }



  startExam(): void {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser || !this.exam) return;

    if (!this.questions || this.questions.length === 0) {
      alert('No questions available for this exam. Please contact your instructor.');
      return;
    }

    // Start exam in backend
    this.examService.startExam(this.exam.id, currentUser.id).subscribe({
      next: (result) => {
        this.examResultId = result.id;
        this.examStarted = true;
        this.proctoringActive = true; // Force proctoring to show
        this.startTimer();
        this.startProctoring(); // Always start proctoring
      },
      error: (error) => {
        console.error('Failed to start exam:', error);
        alert('Failed to start exam. Please try again.');
      }
    });
  }

  startTimer(): void {
    this.timerInterval = setInterval(() => {
      this.timeRemaining--;
      if (this.timeRemaining <= 0) {
        this.autoSubmitExam();
      }
    }, 1000);
  }

  stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  async startProctoring(): Promise<void> {
    console.log('🎥 Starting proctoring...');
    this.proctoringActive = true;
    
    // Start monitoring FIRST (keyboard, mouse, tab switching, etc.)
    this.startMonitoring();
    console.log('✅ Monitoring started');
    
    // Load face detection models
    try {
      await this.faceDetectionService.loadModels();
      console.log('✅ Face detection models loaded');
    } catch (error) {
      console.error('❌ Failed to load face detection models:', error);
    }
    
    // Try to start camera
    try {
      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { width: 200, height: 150 },
        audio: false
      });
      
      console.log('✅ Camera access granted');
      
      setTimeout(() => {
        if (this.videoElement && this.videoElement.nativeElement) {
          const video = this.videoElement.nativeElement;
          video.srcObject = this.mediaStream;
          video.onloadedmetadata = () => {
            video.play().then(() => {
              console.log('✅ Video playing');
              this.startFaceDetection();
            }).catch(e => {
              console.error('❌ Video play error:', e);
            });
          };
        }
      }, 500);
      
    } catch (error) {
      console.error('❌ Camera access denied:', error);
      this.recordSuspiciousActivity('Camera access denied');
    }
  }

  startMonitoring(): void {
    console.log('🔍 Setting up monitoring...');
    
    // Tab visibility monitoring
    this.visibilityListener = () => {
      if (document.hidden) {
        console.log('⚠️ Tab switched');
        this.recordSuspiciousActivity('Tab switched or window minimized');
      }
    };
    document.addEventListener('visibilitychange', this.visibilityListener);

    // Enhanced keyboard monitoring
    this.keydownListener = (event: KeyboardEvent) => {
      if (event.ctrlKey && event.key !== 'Control') {
        console.log('⚠️ Ctrl+' + event.key);
        this.recordSuspiciousActivity(`Suspicious key: Ctrl+${event.key}`);
        event.preventDefault();
      }
      if (event.altKey && event.key !== 'Alt') {
        console.log('⚠️ Alt+' + event.key);
        this.recordSuspiciousActivity(`Suspicious key: Alt+${event.key}`);
        event.preventDefault();
      }
      if (event.metaKey && event.key !== 'Meta') {
        console.log('⚠️ Cmd+' + event.key);
        this.recordSuspiciousActivity(`Suspicious key: Cmd+${event.key}`);
        event.preventDefault();
      }
      if (event.key.startsWith('F') && event.key.length <= 3) {
        console.log('⚠️ Function key: ' + event.key);
        this.recordSuspiciousActivity(`Function key pressed: ${event.key}`);
        event.preventDefault();
      }
    };
    document.addEventListener('keydown', this.keydownListener);

    // Mouse leave monitoring
    this.mouseleaveListener = () => {
      console.log('⚠️ Mouse left window');
      this.recordSuspiciousActivity('Mouse cursor left exam window');
    };
    document.addEventListener('mouseleave', this.mouseleaveListener);

    // Window focus monitoring
    this.blurListener = () => {
      console.log('⚠️ Window lost focus');
      this.recordSuspiciousActivity('Window lost focus - switched to another application');
    };
    window.addEventListener('blur', this.blurListener);

    // Right-click prevention
    this.contextmenuListener = (event: Event) => {
      console.log('⚠️ Right-click attempted');
      this.recordSuspiciousActivity('Right-click attempted');
      event.preventDefault();
    };
    document.addEventListener('contextmenu', this.contextmenuListener);

    // Copy/paste monitoring
    this.copyListener = () => {
      console.log('⚠️ Copy detected');
      this.recordSuspiciousActivity('Copy operation detected');
    };
    document.addEventListener('copy', this.copyListener);
    
    this.pasteListener = () => {
      console.log('⚠️ Paste detected');
      this.recordSuspiciousActivity('Paste operation detected');
    };
    document.addEventListener('paste', this.pasteListener);
    
    console.log('✅ All monitoring listeners active');
  }

  startFaceDetection(): void {
    console.log('👤 Starting face detection...');
    setTimeout(() => {
      if (this.videoElement && this.videoElement.nativeElement) {
        this.faceDetectionInterval = setInterval(() => {
          this.detectFaces();
        }, 3000);
        console.log('✅ Face detection interval started');
      } else {
        console.log('❌ Video element not available for face detection');
      }
    }, 2000);
  }

  async detectFaces(): Promise<void> {
    if (!this.videoElement || !this.videoElement.nativeElement) return;
    
    const canvas = this.canvasElement?.nativeElement;
    const detections = await this.faceDetectionService.detectFaces(this.videoElement.nativeElement, canvas);
    const analysis = this.faceDetectionService.analyzeFaceData(detections);
    
    console.log(`👤 Face detection result: ${analysis.faceCount} face(s) detected`);
    
    if (analysis.faceCount === 0) {
      console.log('❌ No face detected');
      this.recordSuspiciousActivity('No face detected in camera');
    } else if (analysis.faceCount > 1) {
      console.log(`⚠️ Multiple faces detected: ${analysis.faceCount} faces`);
      this.recordSuspiciousActivity(`Multiple faces detected: ${analysis.faceCount} faces`);
    } else {
      console.log('✅ Exactly 1 face detected - OK');
    }
  }

  recordSuspiciousActivity(activity: string): void {
    const timestamp = new Date().toLocaleTimeString();
    const logEntry = `${timestamp}: ${activity}`;
    this.suspiciousActivities.push(logEntry);
    console.log('🚨 ALERT:', logEntry);
  }
  
  removeEventListeners(): void {
    if (this.visibilityListener) document.removeEventListener('visibilitychange', this.visibilityListener);
    if (this.keydownListener) document.removeEventListener('keydown', this.keydownListener);
    if (this.mouseleaveListener) document.removeEventListener('mouseleave', this.mouseleaveListener);
    if (this.blurListener) window.removeEventListener('blur', this.blurListener);
    if (this.contextmenuListener) document.removeEventListener('contextmenu', this.contextmenuListener);
    if (this.copyListener) document.removeEventListener('copy', this.copyListener);
    if (this.pasteListener) document.removeEventListener('paste', this.pasteListener);
    if (this.faceDetectionInterval) clearInterval(this.faceDetectionInterval);
  }

  stopProctoring(): void {
    console.log('🛑 Stopping proctoring...');
    if (this.mediaRecorder && this.proctoringActive) {
      this.mediaRecorder.stop();
    }
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
    }
    this.proctoringActive = false;
    this.removeEventListeners();
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  selectAnswer(questionId: number, answer: string): void {
    this.answers[questionId] = answer;
  }

  onInputChange(event: any, questionId: number): void {
    this.answers[questionId] = event.target.value;
  }

  getCurrentDate(): string {
    return new Date().toLocaleString();
  }

  getAnsweredCount(): number {
    return Object.keys(this.answers).length;
  }

  calculateScore(): number {
    let score = 0;
    this.questions.forEach(question => {
      if (question.type === 'CODING') {
        // For coding questions, score is calculated by test case evaluation
        // This will be handled separately in submitExam()
        score += 0; // Placeholder, actual score calculated by backend
      } else {
        const userAnswer = this.answers[question.id];
        if (userAnswer && question.correctAnswer && 
            userAnswer.trim().toLowerCase() === question.correctAnswer.trim().toLowerCase()) {
          score += question.marks || 10;
        }
      }
    });
    console.log('Calculated score:', score, 'out of', this.questions.length * 10);
    return score;
  }



  submitExam(): void {
    if (this.submitting) return;
    
    if (!confirm('Are you sure you want to submit the exam?')) return;
    
    this.submitting = true;
    this.stopTimer();
    this.stopProctoring();
    
    // Check if exam has coding questions
    const hasCodingQuestions = this.questions.some(q => q.type === 'CODING');
    
    if (hasCodingQuestions) {
      this.submitCodingExam();
    } else {
      this.submitRegularExam();
    }
  }
  
  submitCodingExam(): void {
    // Get stored evaluation results
    const evaluationResults = (window as any).codingEvaluationResults || {};
    
    const codingAnswers = this.questions
      .filter(q => q.type === 'CODING')
      .map(q => {
        const evalResult = evaluationResults[q.id];
        return {
          questionId: q.id,
          marks: q.marks || 10,
          passedTests: evalResult ? evalResult.passedTests : 0,
          totalTests: evalResult ? evalResult.totalTests : 1
        };
      });
    
    const submissionData = {
      codingAnswers: codingAnswers
    };
    
    this.examService.submitCodingExam(this.examResultId!, submissionData).subscribe({
      next: (result) => {
        console.log('Coding exam submitted successfully:', result);
        this.examResult = {
          score: result.score,
          totalMarks: result.totalMarks,
          percentage: (result.score / result.totalMarks) * 100,
          proctoringReport: 'Coding exam completed',
          suspiciousActivityCount: this.suspiciousActivities.length
        };
        this.examSubmitted = true;
        this.submitting = false;
      },
      error: (error) => {
        console.error('Failed to submit coding exam:', error);
        this.showMockResult(0);
      }
    });
  }
  
  submitRegularExam(): void {
    const score = this.calculateScore();
    
    if (this.examResultId) {
      const submissionData = {
        score: score,
        answers: this.getAnswerAnalysis(),
        suspiciousActivities: this.suspiciousActivities
      };
      
      console.log('Submitting exam with data:', submissionData);
      
      this.examService.submitExamWithAnswers(this.examResultId, submissionData).subscribe({
        next: (result) => {
          console.log('Exam submitted successfully:', result);
          const totalMarks = result.totalMarks || this.questions.length * 10;
          this.examResult = {
            score: result.score,
            totalMarks: totalMarks,
            percentage: (result.score / totalMarks) * 100,
            proctoringReport: result.aiRecommendations || 'AI recommendations unavailable',
            suspiciousActivityCount: this.suspiciousActivities.length
          };
          this.examSubmitted = true;
          this.submitting = false;
        },
        error: (error) => {
          console.error('Failed to submit exam:', error);
          this.showMockResult(score);
        }
      });
    } else {
      this.showMockResult(score);
    }
  }

  private showMockResult(score: number): void {
    const totalMarks = this.questions.length * 10;
    this.examResult = {
      score: score,
      totalMarks: totalMarks,
      percentage: (score / totalMarks) * 100,
      proctoringReport: 'AI recommendations unavailable',
      suspiciousActivityCount: this.suspiciousActivities.length
    };
    this.examSubmitted = true;
    this.submitting = false;
  }

  private getAnswerAnalysis(): any[] {
    const analysis = [];
    for (let i = 0; i < this.questions.length; i++) {
      const question = this.questions[i];
      const userAnswer = this.answers[question.id] || '';
      const isCorrect = question.correctAnswer && userAnswer && 
                        question.correctAnswer.trim().toLowerCase() === userAnswer.trim().toLowerCase();
      
      analysis.push({
        questionId: question.id,
        questionText: question.questionText,
        userAnswer: userAnswer,
        correctAnswer: question.correctAnswer || '',
        isCorrect: isCorrect,
        marks: 10, // Each question is worth 10 marks
        topic: question.topic || 'General'
      });
      
      console.log(`Question ${question.id}: User='${userAnswer}', Correct='${question.correctAnswer}', Match=${isCorrect}`);
    }
    console.log('Answer analysis:', analysis);
    return analysis;
  }

  formatAIRecommendations(recommendations: string): string {
    if (!recommendations) return 'No AI recommendations available';
    
    // Handle JSON response from Bedrock
    try {
      const parsed = JSON.parse(recommendations);
      if (parsed.error) {
        return this.formatMarkdown(recommendations);
      }
      
      // Format structured AI insights
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
      // Fallback to markdown formatting
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





  autoSubmitExam(): void {
    this.recordSuspiciousActivity('Exam auto-submitted due to time expiry');
    this.submitExam();
  }

  formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  get currentQuestion(): Question | null {
    return this.questions[this.currentQuestionIndex] || null;
  }

  get progressPercentage(): number {
    return ((this.currentQuestionIndex + 1) / this.questions.length) * 100;
  }

  viewDetailedReport(): void {
    // Navigate to detailed report or show modal
    console.log('Viewing detailed report for session:', this.sessionId);
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }


}