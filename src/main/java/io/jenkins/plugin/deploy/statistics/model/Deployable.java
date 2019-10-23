package io.jenkins.plugin.deploy.statistics.model;

import java.util.List;

public class Deployable {
    private String branchName;
    private List<BuildInfo> availableBuilds;
    private List<String> availableArtifacts;

    public Deployable(String branchName, List<BuildInfo> availableBuilds, List<String> availableArtifacts) {
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

    public List<BuildInfo> getAvailableBuilds() {
        return availableBuilds;
    }

    public void setAvailableBuilds(List<BuildInfo> availableBuilds) {
        this.availableBuilds = availableBuilds;
    }

    public List<String> getAvailableArtifacts() {
        return availableArtifacts;
    }

    public void setAvailableArtifacts(List<String> availableArtifacts) {
        this.availableArtifacts = availableArtifacts;
    }
}
