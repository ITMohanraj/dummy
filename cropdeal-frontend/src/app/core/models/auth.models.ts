export type Role = 'FARMER' | 'DEALER' | 'DELIVERY_PARTNER' | 'ADMIN';

export interface RegisterRequest {
  fullName: string;
  email: string;
  password?: string;
  phoneNumber: string;
  role: Role;
  state?: string;
  district?: string;
  address?: string;
}

export interface LoginRequest {
  email: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  role: Role;
  fullName: string;
  expiresIn: number;
}

export interface UserProfile {
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  role: Role;
  state?: string;
  district?: string;
  address?: string;
  kycStatus?: 'PENDING' | 'VERIFIED' | 'REJECTED';
  reputationScore?: number;
  avatarUrl?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  otp: string;
  newPassword: string;
}

export interface MessageResponse {
  message: string;
  status?: string;
}
