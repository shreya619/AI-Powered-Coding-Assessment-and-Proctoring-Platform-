export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  learningStyle?: 'VISUAL' | 'AUDITORY' | 'KINESTHETIC' | 'READING_WRITING';
  totalPoints: number;
  averageScore: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: string;
}