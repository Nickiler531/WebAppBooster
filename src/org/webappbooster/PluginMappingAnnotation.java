package org.webappbooster;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PluginMappingAnnotation {
	String actions();
	String permission();	
}