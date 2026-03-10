%% Clarity (todo-app) -- Create Task Sequence Diagram
%% Generated: 2026-03-07

sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Store as NgRx Store
    participant Effect as NgRx Effect
    participant TkSvc as TaskService
    participant Intc as AuthInterceptor
    participant Ctrl as TaskController
    participant Svc as TaskApplicationService
    participant Dom as Task (Domain)
    participant Repo as TaskRepository
    participant Cache as Redis Cache

    User->>SPA: Type title + press Enter
    SPA->>Store: dispatch(createTask({title, listId}))

    Note over Store: Optimistic update:<br/>Add temp task with tempId

    Store-->>SPA: State updated (task appears in UI)
    SPA-->>User: Task visible instantly

    Store->>Effect: createTask$ effect triggered
    Effect->>TkSvc: createTask(request)
    TkSvc->>Intc: POST /api/v1/tasks {title, listId}
    Intc->>Intc: Attach Authorization: Bearer JWT
    Intc->>Ctrl: POST /api/v1/tasks

    Ctrl->>Ctrl: @Valid(CreateTaskRequest)
    Ctrl->>Svc: createTask(command)
    Svc->>Svc: Resolve listId (default to Inbox if null)
    Svc->>Dom: Task.create(title, listId, userId)
    Dom-->>Svc: Task + TaskCreatedEvent
    Svc->>Repo: save(task)
    Repo-->>Svc: persisted Task

    Note over Svc,Cache: @EventListener TaskCreatedEvent<br/>invalidates cache keys

    Svc-)Cache: DEL cache:tasks:user:{uid}:list:{lid}<br/>DEL cache:counts:user:{uid}<br/>DEL cache:today:user:{uid}

    Svc-->>Ctrl: TaskResponse
    Ctrl-->>TkSvc: 201 Created {TaskResponse}
    TkSvc-->>Effect: TaskResponse
    Effect->>Store: dispatch(createTaskSuccess(response))

    Note over Store: Replace tempId with real ID<br/>from server response

    Store-->>SPA: State reconciled
    SPA-->>User: No visible change (already displayed)
