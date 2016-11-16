package me.akhmetov.vxl.api;

/**
 * Provides access to all of the documented plugin-accessible APIs.
 */
public interface Vxl {
    /**
     * Returns a reference to the node API, which can be used to interact with functions such as
     * node registration.
     * @return
     */
    NodeAPI getNodeApi();



}
