package sh.okx.civtale.structure;

import com.hypixel.hytale.math.vector.Vector3i;

import java.util.Comparator;

public record BlockPos(int x, int y, int z) implements Comparable<BlockPos> {
    @Override
    public int compareTo(BlockPos blockPos) {
        return Comparator.comparingInt(BlockPos::y)
            .thenComparing(BlockPos::x)
            .thenComparing(BlockPos::z)
            .compare(this, blockPos);
    }

    public static  BlockPos fromVec(Vector3i vector) {
        return new BlockPos(vector.x, vector.y, vector.z);
    }
}
