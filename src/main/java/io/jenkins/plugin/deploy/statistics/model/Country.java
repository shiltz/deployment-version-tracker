package io.jenkins.plugin.deploy.statistics.model;


import java.util.*;

public class Country {

    private String name;
    private List<CountryDeploymentStats> deploymentStats;
    private CountryDeploymentStats previousDeploymentStats;
    private CountryDeploymentStats currentDeploymentStats;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CountryDeploymentStats> getDeploymentStats() {
        return deploymentStats;
    }

    public void setDeploymentStats(List<CountryDeploymentStats> deploymentStats) {
        this.deploymentStats = deploymentStats;
    }

    public CountryDeploymentStats getPreviousDeploymentStats() {
        return previousDeploymentStats;
    }

    public void setPreviousDeploymentStats(CountryDeploymentStats previousDeploymentStats) {
        this.previousDeploymentStats = previousDeploymentStats;
    }

    public CountryDeploymentStats getCurrentDeploymentStats() {
        return currentDeploymentStats;
    }

    public void setCurrentDeploymentStats(CountryDeploymentStats currentDeploymentStats) {
        this.currentDeploymentStats = currentDeploymentStats;
    }
}
