package me.akhmetov.vxl.core.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tags a class to specify that a script may serialize it.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptMaySerialize {

}
