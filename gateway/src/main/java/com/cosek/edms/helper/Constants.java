package com.cosek.edms.helper;

public class Constants {
    public static final String GENERAL_ROUTE = "/api/v1";

    // User permissions
    public static final String READ_USER = "READ_USER";
    public static final String CREATE_USER = "CREATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";

    // Role permissions
    public static final String READ_ROLE = "READ_ROLE";
    public static final String CREATE_ROLE = "CREATE_ROLE";
    public static final String DELETE_ROLE = "DELETE_ROLE";
    public static final String UPDATE_ROLE = "UPDATE_ROLE";

    // Permission permissions
    public static final String READ_PERMISSION = "READ_PERMISSION";
    public static final String CREATE_PERMISSION = "CREATE_PERMISSION";
    public static final String DELETE_PERMISSION = "DELETE_PERMISSION";
    public static final String UPDATE_PERMISSION = "UPDATE_PERMISSION";

    // Workflow permissions
    public static final String READ_WORKFLOW = "READ_WORKFLOW";
    public static final String CREATE_WORKFLOW = "CREATE_WORKFLOW";
    public static final String DELETE_WORKFLOW = "DELETE_WORKFLOW";
    public static final String UPDATE_WORKFLOW = "UPDATE_WORKFLOW";

    // Form permissions
    public static final String READ_FORM = "READ_FORM";
    public static final String CREATE_FORM = "CREATE_FORM";
    public static final String DELETE_FORM = "DELETE_FORM";
    public static final String UPDATE_FORM = "UPDATE_FORM";

    // Log permissions
    public static final String READ_LOG = "READ_LOG";
    public static final String CREATE_LOG = "CREATE_LOG";
    public static final String DELETE_LOG = "DELETE_LOG";
    public static final String UPDATE_LOG = "UPDATE_LOG";

    //
    public static final String CREATE_FOLDER = "CREATE_FOLDER";
    public static final String DELETE_FOLDER = "DELETE_FOLDER";
    public static final String UPDATE_FOLDER = "UPDATE_FOLDER";

    // Routes
    public static final String USER_ROUTE = GENERAL_ROUTE + "/users/**";
    public static final String ROLE_ROUTE = GENERAL_ROUTE + "/roles/**";
    public static final String PERMISSION_ROUTE = GENERAL_ROUTE + "/permissions/**";
    public static final String WORKFLOW_ROUTE =  "/workflows/**";
    public static final String FORM_ROUTE =  "/forms/**";
    public static final String FILES_MANAGEMENT_ROUTE =  "/file-management/**";
    public static final String LOG_ROUTE = GENERAL_ROUTE + "/logs/**";
    public static final String AUTH_ROUTE = GENERAL_ROUTE + "/auth/**";

    // Admin configuration
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String ADMIN_PASSWORD = "password123";
    public static final String ADMIN_PHONE = "+256777338787";
    public static final String ADMIN_FIRST_NAME = "Admin";
    public static final String ADMIN_LAST_NAME = "Super";
    public static final String ADMIN_COUNTRY = "Uganda";

    // Response messages
    public static final String SUCCESSFUL_DELETION = "Delete Successful";
    public static final String FAILED_DELETION = "Delete Failed";
}