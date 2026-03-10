import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layouts/main-layout/main-layout.component')
      .then(m => m.MainLayoutComponent),
    children: [
      { path: '', redirectTo: 'today', pathMatch: 'full' },
      {
        path: 'today',
        loadChildren: () => import('./features/today/today.routes')
          .then(m => m.TODAY_ROUTES)
      },
      {
        path: 'tasks',
        loadChildren: () => import('./features/tasks/tasks.routes')
          .then(m => m.TASK_ROUTES)
      },
      {
        path: 'lists',
        loadChildren: () => import('./features/lists/lists.routes')
          .then(m => m.LIST_ROUTES)
      }
    ]
  },
  {
    path: 'auth',
    loadComponent: () => import('./layouts/auth-layout/auth-layout.component')
      .then(m => m.AuthLayoutComponent),
    children: [
      {
        path: '',
        loadChildren: () => import('./features/auth/auth.routes')
          .then(m => m.AUTH_ROUTES)
      }
    ]
  },
  { path: '**', redirectTo: 'today' }
];
