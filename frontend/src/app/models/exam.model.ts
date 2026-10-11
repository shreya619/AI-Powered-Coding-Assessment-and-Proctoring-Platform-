export interface Exam {
  id: number;
  title: string;
  description: string;
  duration: number;
  totalMarks: number;
  startTime?: Date;
  endTime?: Date;
  isActive: boolean;
  proctoringEnabled: boolean;
  questions?: Question[];
}

export interface Question {
  id: number;
  questionText: string;
  type: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'ESSAY' | 'CODING';
  marks: number;
  options?: string[];
  correctAnswer?: string;
  topic?: string;
  language?: string;
  starterCode?: string;
  testCases?: string;
}

export interface ExamResult {
  id: number;
  score: number;
  totalMarks: number;
  startedAt: Date;
  submittedAt?: Date;
  isCompleted: boolean;
  exam: Exam;
  proctoringReport?: string;
}