package sh.okx.civtale.reinforcement;

import sh.okx.civtale.database.store.PositionStoreable;
import sh.okx.civtale.structure.BlockPos;

public record Reinforcement(String world, int x, int y, int z, String type, double health, long created) implements PositionStoreable {
    @Override
    public BlockPos blockPos() {
        return new BlockPos(x, y, z);
    }
}
