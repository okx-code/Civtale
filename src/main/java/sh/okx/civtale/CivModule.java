package sh.okx.civtale;

public interface CivModule {
    /**
     * Initialize the module.
     */
    void init();

    /**
     * Start the module.
     */
    void start();

    /**
     * Shutdown the module.
     */
    void shutdown();
}
