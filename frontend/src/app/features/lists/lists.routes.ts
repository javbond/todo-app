import { Routes } from '@angular/router';

export const LIST_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/list-detail/list-detail.component')
      .then(m => m.ListDetailComponent)
  }
];
