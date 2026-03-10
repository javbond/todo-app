> **Conditional Rule**: This rule applies ONLY when Angular is the selected frontend.
> Check `.claude/rules/06-tech-stack-context.md` and `.sdlc/state.json` → `techStack.primary.frontend`.
> If the project does not use Angular, IGNORE this entire file.

# Angular Conventions

## Project Structure
```
src/app/
├── core/                    # Singleton services, guards, interceptors
│   ├── auth/
│   │   ├── auth.guard.ts
│   │   ├── auth.interceptor.ts
│   │   └── auth.service.ts
│   ├── services/            # App-wide services
│   ├── interceptors/        # HTTP interceptors
│   └── guards/              # Route guards
├── shared/                  # Reusable components, directives, pipes
│   ├── components/
│   ├── directives/
│   ├── pipes/
│   └── models/              # Shared interfaces/types
├── features/                # Feature modules (lazy loaded)
│   ├── feature-name/
│   │   ├── components/      # Feature-specific components
│   │   ├── services/        # Feature-specific services
│   │   ├── models/          # Feature-specific interfaces
│   │   ├── store/           # NgRx state (actions, reducers, selectors, effects)
│   │   └── feature-name.routes.ts
├── layouts/                 # Layout components
│   ├── main-layout/
│   └── auth-layout/
└── app.routes.ts            # Root routing
```

## Component Patterns

### Standalone Components (Angular 17+)
```typescript
@Component({
  selector: 'app-feature-name',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './feature-name.component.html'
})
```

### Smart vs Presentational Components
| Smart (Container) | Presentational (Dumb) |
|-------------------|----------------------|
| Injects services | Only `@Input()` / `@Output()` |
| Manages state | Stateless, pure rendering |
| Calls APIs | Emits events via `@Output()` |
| Lives in `features/` | Lives in `shared/components/` |

### Signals (Angular 17+)
- Prefer signals over BehaviorSubject for component state
- Use `computed()` for derived state
- Use `effect()` for side effects

## NgRx State Management
```
store/
├── feature.actions.ts      # Action definitions
├── feature.reducer.ts      # State mutations
├── feature.selectors.ts    # State queries
├── feature.effects.ts      # Side effects (API calls)
└── feature.state.ts        # State interface
```

### Naming Conventions
- Actions: `[Feature] Action Name` — e.g., `[User] Load Users`
- Selectors: `selectFeatureProp` — e.g., `selectUserList`
- Effects: `loadFeature$` — e.g., `loadUsers$`

## RxJS Best Practices
- Always unsubscribe (use `takeUntilDestroyed()` or `async` pipe)
- Prefer `async` pipe in templates over manual subscriptions
- Use `switchMap` for search/navigation, `mergeMap` for parallel, `concatMap` for sequential
- Avoid nested subscribes — use higher-order operators

## Forms
- Use Reactive Forms for complex forms
- Use typed forms (`FormGroup<UserForm>`)
- Centralize validation messages
- Use `updateOn: 'blur'` for performance on large forms

## Lazy Loading
```typescript
// app.routes.ts
export const routes: Routes = [
  {
    path: 'users',
    loadChildren: () => import('./features/users/users.routes')
      .then(m => m.USER_ROUTES)
  }
];
```

## HTTP Interceptors
- Auth interceptor: attach JWT token to all API requests
- Error interceptor: handle 401 (redirect to login), 500 (show toast)
- Loading interceptor: show/hide global spinner
- CSRF interceptor: attach XSRF token

## Testing
- Use Angular Testing Library over TestBed where possible
- Test components in isolation (mock services)
- Test services with HttpClientTestingModule
- Snapshot testing for presentational components
