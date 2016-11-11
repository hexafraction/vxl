package me.akhmetov.vxl.core.security;

import java.security.BasicPermission;

public class VxlPermission extends BasicPermission {
    public VxlPermission(String name) {
        super(name);
    }

    public VxlPermission(String name, String actions) {
        super(name, actions);
    }
}
