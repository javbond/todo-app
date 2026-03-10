CREATE TABLE task_lists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL,
    color           VARCHAR(20) NOT NULL DEFAULT 'GRAY',
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    user_id         UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_task_lists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_task_lists_name_user UNIQUE (user_id, name),
    CONSTRAINT ck_task_lists_name_length CHECK (char_length(name) >= 1),
    CONSTRAINT ck_task_lists_color CHECK (color IN ('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'PURPLE', 'PINK', 'GRAY'))
);

CREATE TRIGGER trg_task_lists_updated_at
    BEFORE UPDATE ON task_lists
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_task_lists_user_id ON task_lists (user_id, sort_order);
CREATE INDEX idx_task_lists_user_default ON task_lists (user_id)
    WHERE is_default = TRUE;
