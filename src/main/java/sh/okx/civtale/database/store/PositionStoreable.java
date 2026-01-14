package sh.okx.civtale.database.store;

import sh.okx.civtale.structure.BlockPos;

public interface PositionStoreable {
    String world();
    BlockPos blockPos();
}
