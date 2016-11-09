package me.akhmetov.vxl.core;

public abstract class ISettingsProvider {
    static ISettingsProvider provider;
    public abstract String getValue(String key);
}
