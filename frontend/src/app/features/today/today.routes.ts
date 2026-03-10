import { Routes } from '@angular/router';

export const TODAY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/today-view/today-view.component')
      .then(m => m.TodayViewComponent)
  }
];
