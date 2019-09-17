package io.jenkins.plugin.deploy.statistics.model;

public class Project {
    private String deploymentEnv;

    public Project(String deploymentEnv) {
        this.deploymentEnv = deploymentEnv;
    }

    public String getDeploymentEnv() {
        return deploymentEnv;
    }

    public void setDeploymentEnv(String deploymentEnv) {
        this.deploymentEnv = deploymentEnv;
    }
}
