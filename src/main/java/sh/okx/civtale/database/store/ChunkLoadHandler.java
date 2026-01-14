package sh.okx.civtale.database.store;

import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import sh.okx.civtale.structure.ChunkPos;

import java.util.function.Consumer;

public class ChunkLoadHandler<T extends PositionStoreable> implements Consumer<ChunkPreLoadProcessEvent> {

    private final ChunkPositionDatabaseStore<T> store;

    public ChunkLoadHandler(ChunkPositionDatabaseStore<T> store) {
        this.store = store;
    }

    @Override
    public void accept(ChunkPreLoadProcessEvent chunkPreLoadProcessEvent) {
        WorldChunk c = chunkPreLoadProcessEvent.getChunk();
        BlockChunk chunk = c.getBlockChunk();
        store.loadChunk(new ChunkPos(c.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }
}
