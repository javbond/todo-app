export type ListColor = 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE' | 'PURPLE' | 'PINK' | 'GRAY';

export interface TaskList {
  id: string;
  name: string;
  color: ListColor;
  colorHex: string;
  sortOrder: number;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TaskListWithCount extends TaskList {
  activeTaskCount: number;
}

export interface CreateListRequest {
  name: string;
  color?: ListColor;
}

export interface UpdateListRequest {
  name?: string;
  color?: ListColor;
}
