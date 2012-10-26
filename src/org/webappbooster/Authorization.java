package org.webappbooster;

import java.util.HashMap;
import java.util.Map;

public class Authorization {

	private Map<String, String[]> permissions;
	
	public Authorization() {
		permissions = new HashMap<String, String[]>();
	}
	
	public void setPermissions(String origin, String[] permissions) {
		this.permissions.put(origin, permissions);
	}
	
	public boolean checkPermission(String origin, String permission) {
		return false;
	}
}
