import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ExamService } from '../../services/exam.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-teacher-dashboard',
  templateUrl: './teacher-dashboard.component.html',
  styleUrls: ['./teacher-dashboard.component.css']
})
export class TeacherDashboardComponent implements OnInit {
  examForm: FormGroup;
  createdExams: any[] = [];
  loading = false;
  showCreateForm = false;

  constructor(
    private fb: FormBuilder,
    private examService: ExamService,
    public authService: AuthService,
    private router: Router
  ) {
    this.examForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      duration: [60, [Validators.required, Validators.min(1)]],
      totalMarks: [100, [Validators.required, Validators.min(1)]],
      questions: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.loadCreatedExams();
    this.addQuestion();
  }

  get questions(): FormArray {
    return this.examForm.get('questions') as FormArray;
  }

  addQuestion(): void {
    const questionGroup = this.fb.group({
      questionText: ['', Validators.required],
      type: ['MULTIPLE_CHOICE', Validators.required],
      marks: [10, [Validators.required, Validators.min(1)]],
      options: this.fb.array([this.fb.control(''), this.fb.control(''), this.fb.control(''), this.fb.control('')]),
      correctAnswer: [''],
      language: ['java'],
      starterCode: [''],
      testCases: ['']
    });
    this.questions.push(questionGroup);
  }

  removeQuestion(index: number): void {
    this.questions.removeAt(index);
  }

  getOptions(questionIndex: number): FormArray {
    return this.questions.at(questionIndex).get('options') as FormArray;
  }

  createExam(): void {
    if (this.examForm.valid) {
      this.loading = true;
      const formValue = this.examForm.value;
      
      const examData = {
        title: formValue.title,
        description: formValue.description,
        duration: formValue.duration,
        totalMarks: formValue.totalMarks,
        createdBy: this.authService.getCurrentUserId(),
        questions: formValue.questions.map((q: any) => ({
          questionText: q.questionText,
          type: q.type,
          marks: q.marks,
          options: q.type === 'MULTIPLE_CHOICE' ? q.options.filter((opt: string) => opt.trim()) : [],
          correctAnswer: q.type !== 'CODING' ? q.correctAnswer : null,
          language: q.type === 'CODING' ? q.language : null,
          starterCode: q.type === 'CODING' ? q.starterCode : null,
          testCases: q.type === 'CODING' ? q.testCases : null
        }))
      };

      console.log('Sending exam data:', examData);
      
      this.examService.createExam(examData).subscribe({
        next: (response) => {
          console.log('Exam created successfully:', response);
          alert('Exam created successfully!');
          setTimeout(() => {
            this.loadCreatedExams();
          }, 500);
          this.resetForm();
          this.showCreateForm = false;
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to create exam', error);
          alert('Failed to create exam: ' + (error.error?.message || error.message));
          this.loading = false;
        }
      });
    }
  }

  loadCreatedExams(): void {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      console.error('No current user found');
      return;
    }
    
    console.log('Loading exams for teacher ID:', currentUser.id);
    this.examService.getTeacherExams().subscribe({
      next: (exams) => {
        console.log('Loaded exams:', exams);
        this.createdExams = exams || [];
      },
      error: (error) => {
        console.error('Failed to load exams', error);
        this.createdExams = [];
      }
    });
  }

  resetForm(): void {
    this.examForm.reset();
    this.questions.clear();
    this.addQuestion();
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.resetForm();
    }
  }

  onQuestionTypeChange(questionIndex: number, type: string): void {
    const question = this.questions.at(questionIndex);
    if (type === 'CODING') {
      question.get('correctAnswer')?.clearValidators();
      question.get('language')?.setValidators([Validators.required]);
      question.get('testCases')?.setValidators([Validators.required]);
    } else {
      question.get('correctAnswer')?.setValidators([Validators.required]);
      question.get('language')?.clearValidators();
      question.get('testCases')?.clearValidators();
    }
    question.get('correctAnswer')?.updateValueAndValidity();
    question.get('language')?.updateValueAndValidity();
    question.get('testCases')?.updateValueAndValidity();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}