import { Priority } from './priority.model';

export interface Task {
  id: string;
  title: string;
  description: string | null;
  priority: Priority;
  priorityLabel: string;
  priorityColor: string;
  dueDate: string | null;
  isCompleted: boolean;
  isToday: boolean;
  completedAt: string | null;
  listId: string;
  listName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  priority?: Priority;
  dueDate?: string | null;
  listId?: string | null;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string | null;
  priority?: Priority;
  dueDate?: string | null;
  listId?: string;
}

export interface TaskPageResponse {
  content: Task[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TodayViewResponse {
  today: Task[];
  overdue: Task[];
  completedToday: Task[];
  totalToday: number;
  completedCount: number;
}
