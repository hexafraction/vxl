package me.akhmetov.vxl.core;

interface IDatabaseEngine {
    byte[] getValueByKey(byte[] key);
    void storeValue(byte[] key, byte[] value);



}
