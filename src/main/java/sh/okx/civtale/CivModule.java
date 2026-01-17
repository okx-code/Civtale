package sh.okx.civtale;

public interface CivModule {
    /**
     * Setup the module.
     */
    void setup();

    /**
     * Start the module.
     */
    void start();

    /**
     * Shutdown the module.
     */
    void shutdown();
}
