import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly loading$ = new BehaviorSubject<boolean>(false);

  isLoading(): Observable<boolean> {
    return this.loading$.asObservable();
  }

  show(): void {
    this.loading$.next(true);
  }

  hide(): void {
    this.loading$.next(false);
  }
}
