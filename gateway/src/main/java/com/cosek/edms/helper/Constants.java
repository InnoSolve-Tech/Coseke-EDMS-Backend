package com.cosek.edms.helper;

public class Constants {
    public static final String GENERAL_ROUTE = "/api/v1";
    public static final String READ_USER = "READ_USER";
    public static final String CREATE_USER = "CREATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String READ_ROLE = "READ_ROLE";
    public static final String CREATE_ROLE = "CREATE_ROLE";
    public static final String DELETE_ROLE = "DELETE_ROLE";
    public static final String UPDATE_ROLE = "UPDATE_ROLE";
    public static final String READ_PERMISSION = "READ_PERMISSION";
    public static final String CREATE_PERMISSION = "CREATE_PERMISSION";
    public static final String DELETE_PERMISSION = "DELETE_PERMISSION";
    public static final String UPDATE_PERMISSION = "UPDATE_PERMISSION";
    public static final String USER_ROUTE = GENERAL_ROUTE + "/users/**";
    public static final String ROLE_ROUTE = GENERAL_ROUTE + "/roles/**";
    public static final String PERMISSION_ROUTE = GENERAL_ROUTE + "/permissions/**";
    public static final String AUTH_ROUTE = GENERAL_ROUTE + "/auth/**";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String ADMIN_PASSWORD = "password123";
    public static final String ADMIN_PHONE = "+256777338787";
    public static final String ADMIN_FIRST_NAME = "Admin";
    public static final String ADMIN_LAST_NAME = "Super";
    public static final String ADMIN_COUNTRY = "Uganda";
    public static final String SUCCESSFUL_DELETION = "Delete Successful";
    public static final String FAILED_DELETION = "Delete Failed";
}
