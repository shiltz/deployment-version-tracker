package io.jenkins.plugin.deploy.statistics.model;

public class BuildInfo {

    private String buildNumber;
    private String associatedArtifact;

    public BuildInfo(String buildNumber, String associatedArtifact) {
        this.buildNumber = buildNumber;
        this.associatedArtifact = associatedArtifact;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getAssociatedArtifact() {
        return associatedArtifact;
    }

    public void setAssociatedArtifact(String associatedArtifact) {
        this.associatedArtifact = associatedArtifact;
    }
}

