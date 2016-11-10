package me.akhmetov.vxl.core;

public interface IDatabaseEngine {
    byte[] getValueByKey(byte[] key);
    void storeValue(byte[] key, byte[] value);



}
