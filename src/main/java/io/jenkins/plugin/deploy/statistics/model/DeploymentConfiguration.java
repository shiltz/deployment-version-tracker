package io.jenkins.plugin.deploy.statistics.model;

import java.util.*;

public class DeploymentConfiguration {

    private List<Environment> environments;

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
}
