import { Routes } from '@angular/router';

export const TASK_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/task-list/task-list.component')
      .then(m => m.TaskListComponent)
  }
];
