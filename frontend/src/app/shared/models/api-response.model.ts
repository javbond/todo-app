export interface FieldError {
  field: string;
  message: string;
}

export type ErrorCode =
  | 'VALIDATION_ERROR'
  | 'INVALID_CREDENTIALS'
  | 'TOKEN_EXPIRED'
  | 'TOKEN_INVALID'
  | 'ACCESS_DENIED'
  | 'RESOURCE_NOT_FOUND'
  | 'EMAIL_ALREADY_EXISTS'
  | 'BUSINESS_RULE_VIOLATION'
  | 'ACCOUNT_LOCKED'
  | 'RATE_LIMIT_EXCEEDED'
  | 'INTERNAL_ERROR';

export interface ErrorResponse {
  status: number;
  code: ErrorCode;
  message: string;
  details: FieldError[] | null;
  timestamp: string;
  traceId: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
