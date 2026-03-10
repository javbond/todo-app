CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    priority            VARCHAR(10) NOT NULL DEFAULT 'P4',
    due_date            DATE,
    reminder_interval   VARCHAR(30) NOT NULL DEFAULT 'NONE',
    is_completed        BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    is_today            BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    list_id             UUID NOT NULL,
    user_id             UUID NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_tasks_list FOREIGN KEY (list_id) REFERENCES task_lists(id),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_tasks_title_length CHECK (char_length(title) >= 1),
    CONSTRAINT ck_tasks_description_length CHECK (description IS NULL OR char_length(description) <= 2000),
    CONSTRAINT ck_tasks_priority CHECK (priority IN ('P1', 'P2', 'P3', 'P4')),
    CONSTRAINT ck_tasks_reminder CHECK (reminder_interval IN ('NONE', 'AT_DUE', 'FIFTEEN_MINUTES', 'THIRTY_MINUTES', 'ONE_HOUR', 'ONE_DAY')),
    CONSTRAINT ck_tasks_completed_at CHECK (
        (is_completed = TRUE AND completed_at IS NOT NULL) OR
        (is_completed = FALSE AND completed_at IS NULL)
    )
);

CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_tasks_user_active ON tasks (user_id, priority, sort_order)
    WHERE is_deleted = FALSE AND is_completed = FALSE;
CREATE INDEX idx_tasks_list_active ON tasks (list_id, priority, sort_order)
    WHERE is_deleted = FALSE AND is_completed = FALSE;
CREATE INDEX idx_tasks_today_view ON tasks (user_id, due_date)
    WHERE is_deleted = FALSE AND is_completed = FALSE AND (is_today = TRUE OR due_date IS NOT NULL);
CREATE INDEX idx_tasks_overdue ON tasks (user_id, due_date)
    WHERE is_deleted = FALSE AND is_completed = FALSE AND due_date IS NOT NULL;
CREATE INDEX idx_tasks_completed_today ON tasks (user_id, completed_at)
    WHERE is_deleted = FALSE AND is_completed = TRUE;
CREATE INDEX idx_tasks_count_by_list ON tasks (list_id, user_id)
    WHERE is_deleted = FALSE AND is_completed = FALSE;
