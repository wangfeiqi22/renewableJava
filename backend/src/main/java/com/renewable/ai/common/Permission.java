package com.renewable.ai.common;

public final class Permission {
    
    public static final String DRIVER_VIEW = "driver:view";
    public static final String DRIVER_CREATE = "driver:create";
    public static final String DRIVER_UPDATE = "driver:update";
    public static final String DRIVER_DELETE = "driver:delete";
    
    public static final String ORDER_VIEW = "order:view";
    public static final String ORDER_CREATE = "order:create";
    public static final String ORDER_UPDATE = "order:update";
    public static final String ORDER_DELETE = "order:delete";
    public static final String ORDER_ASSIGN = "order:assign";
    public static final String ORDER_REASSIGN = "order:reassign";
    
    public static final String PROJECT_VIEW = "project:view";
    public static final String PROJECT_CREATE = "project:create";
    public static final String PROJECT_UPDATE = "project:update";
    public static final String PROJECT_DELETE = "project:delete";
    
    public static final String MONITORING_VIEW = "monitoring:view";
    public static final String MONITORING_DISPATCH = "monitoring:dispatch";
    
    public static final String GRAB_VIEW = "grab:view";
    public static final String GRAB_ORDER = "grab:order";
    public static final String GRAB_RULE_MANAGE = "grab:rule:manage";
    public static final String GRAB_POOL_MANAGE = "grab:pool:manage";
    
    public static final String ROLE_MANAGE = "role:manage";
    public static final String USER_MANAGE = "user:manage";
    
    public static final String EXPORT_DATA = "export:data";
    public static final String IMPORT_DATA = "import:data";
    
    public static final String FLEET_VIEW = "fleet:view";
    public static final String FLEET_UPDATE = "fleet:update";
    
    private Permission() {}
}
