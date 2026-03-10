export enum Priority {
  P1 = 'P1',
  P2 = 'P2',
  P3 = 'P3',
  P4 = 'P4'
}

export const PRIORITY_CONFIG: Record<Priority, { label: string; color: string }> = {
  [Priority.P1]: { label: 'Urgent', color: '#EF4444' },
  [Priority.P2]: { label: 'High', color: '#F97316' },
  [Priority.P3]: { label: 'Medium', color: '#3B82F6' },
  [Priority.P4]: { label: 'Low', color: '#9CA3AF' }
};
