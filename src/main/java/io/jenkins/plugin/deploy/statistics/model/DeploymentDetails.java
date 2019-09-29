package io.jenkins.plugin.deploy.statistics.model;

import java.util.Arrays;
import java.util.List;

public class DeploymentDetails {
    private String projectName;
    private String appName;
    private List<Deployable> deployable;
    private List<String> deployableCountriesList = Arrays.asList("All", "Kenya", "Mauritius", "South Africa");

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<Deployable> getDeployable() {
        return deployable;
    }

    public void setDeployable(List<Deployable> deployable) {
        this.deployable = deployable;
    }

    public List<String> getDeployableCountriesList() {
        return deployableCountriesList;
    }

    public void setDeployableCountriesList(List<String> deployableCountriesList) {
        this.deployableCountriesList = deployableCountriesList;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
