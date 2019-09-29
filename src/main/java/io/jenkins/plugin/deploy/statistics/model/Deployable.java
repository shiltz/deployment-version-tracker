package io.jenkins.plugin.deploy.statistics.model;

import java.util.List;

public class Deployable {
    private String branchName;
    private List<String> availableBuilds;
    private List<String> availableArtifacts;

    public Deployable(String branchName, List<String> availableBuilds, List<String> availableArtifacts) {
        this.branchName = branchName;
        this.availableBuilds = availableBuilds;
        this.availableArtifacts = availableArtifacts;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public List<String> getAvailableBuilds() {
        return availableBuilds;
    }

    public void setAvailableBuilds(List<String> availableBuilds) {
        this.availableBuilds = availableBuilds;
    }

    public List<String> getAvailableArtifacts() {
        return availableArtifacts;
    }

    public void setAvailableArtifacts(List<String> availableArtifacts) {
        this.availableArtifacts = availableArtifacts;
    }
}
