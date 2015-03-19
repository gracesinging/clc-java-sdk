package com.centurylink.cloud.sdk.servers.client.domain.server;

public class CreateServerRequest {


    private String name;

    private String groupId;

    private String sourceServerId;

    private Integer cpu;

    private Integer memoryGB;

    private String type;

    private String password;



    /**
     * Name of the server to create. Alphanumeric characters and dashes only. Must be between 1-7 characters depending on the length of the account alias. (This name will be appended with a two digit number and prepended with the dataCenter code and account alias to make up the final server name.)
     */
    public CreateServerRequest name(String name) {
        this.name = name;
        return this;
    }

    /**
     * ID of the parent group. Retrieved from query to parent group, or by looking at the URL on the UI pages in the Control Portal.
     */
    public CreateServerRequest groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * ID of the server to use a source. May be the ID of a template, or when cloning, an existing server ID. The list of available templates for a given account in a data center can be retrieved from the Get Data Center Deployment Capabilities API operation.
     */
    public CreateServerRequest sourceServerId(String sourceServerId) {
        this.sourceServerId = sourceServerId;
        return this;
    }

    /**
     * Number of processors to configure the server with (1-16)
     */
    public CreateServerRequest cpu(Integer cpu) {
        this.cpu = cpu;
        return this;
    }

    /**
     * Number of GB of memory to configure the server with (1-128)
     */
    public CreateServerRequest memoryGB(Integer memoryGB) {
        this.memoryGB = memoryGB;
        return this;
    }

    /**
     * Whether to create standard or hyperscale server
     */
    public CreateServerRequest type(String type) {
        this.type = type;
        return this;
    }



    /**
     * Name of the server to create. Alphanumeric characters and dashes only. Must be between 1-7 characters depending on the length of the account alias. (This name will be appended with a two digit number and prepended with the dataCenter code and account alias to make up the final server name.)
     */
    public String getName() {
        return name;
    }

    /**
     * ID of the parent group. Retrieved from query to parent group, or by looking at the URL on the UI pages in the Control Portal.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * ID of the server to use a source. May be the ID of a template, or when cloning, an existing server ID. The list of available templates for a given account in a data center can be retrieved from the Get Data Center Deployment Capabilities API operation.
     */
    public String getSourceServerId() {
        return sourceServerId;
    }

    /**
     * Number of processors to configure the server with (1-16)
     */
    public Integer getCpu() {
        return cpu;
    }

    /**
     * Number of GB of memory to configure the server with (1-128)
     */
    public Integer getMemoryGB() {
        return memoryGB;
    }

    /**
     * Whether to create standard or hyperscale server
     */
    public String getType() {
        return type;
    }

    public String getPassword() {
        return password;
    }

    public CreateServerRequest password(String password) {
        this.password = password;
        return this;
    }

}