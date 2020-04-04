package com.apple.sdk.constant;

/**
 * Created by lipeishen on 2020/02/12.
 */
public interface ApiConstant {

    public final String QUERY_USERS_FILTERS_USERNAME =
            "/v1/users?fields[users]=username&filter[username]=";
    public final String USERS_ID = "/v1/users/{id}";
    public final String READ_BUNDLE_ID_INFORMATION = "/v1/bundleIds/{id}";
    public final String PRE_RELEASE_VERSIONS = "/v1/preReleaseVersions";
    public final String APPS = "/v1/apps";

    public final String LIST_BUILDS = "/v1/builds";
    public final String READ_BUILD_INFORMATION = "/v1/builds/{id}";
    public final String LIST_ALL_INDIVIDUAL_TESTERS_FOR_A_BUILD = "/v1/builds/{id}/individualTesters";
    public final String LIST_BETA_TESTERS = "/v1/betaTesters";
    public final String LIST_BETA_GROUPS = "/v1/betaGroups";
    public final String LIST_BETA_GROUPS_BY_BUILD_ID = "/v1/betaGroups?filter[builds]=";
    public final String LIST_BETA_GROUPS_BY_NAME = "/v1/betaGroups?filter[name]=";
    public final String READ_BETA_TESTER_INFORMATION = "/v1/betaTesters/{id}";
    public final String LIST_BETA_TESTER_IN_BETA_GROUPS = "/v1/betaGroups/{id}/betaTesters";
    public final String CREATE_BETA_GROUP = "/v1/betaGroups";
    public final String DELETE_BETA_GROUP = "/v1/betaGroups/{id}";
    public final String ADD_BUILDS_TO_BETA_GROUP = "/v1/betaGroups/{id}/relationships/builds";
}
