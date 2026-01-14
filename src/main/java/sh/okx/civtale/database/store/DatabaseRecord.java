package sh.okx.civtale.database.store;

public record DatabaseRecord<T>(RecordState state, T value) {
    enum RecordState {
        DELETED,
        MODIFIED,
        UNMODIFIED,
        NEW,

        UPDATED_UNCONFIRMED,
        DELETED_UNCONFIRMED,
    }
}

