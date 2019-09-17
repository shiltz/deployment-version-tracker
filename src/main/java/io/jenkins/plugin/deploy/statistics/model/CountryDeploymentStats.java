package io.jenkins.plugin.deploy.statistics.model;

import java.util.Date;

public class CountryDeploymentStats implements Comparable<CountryDeploymentStats>{
    private String artifactVersion;
    private String commit;
    private String status;
    private Date date;
    private String dateDisplay;
    private String branchName;
    private String buildNumber;

    public String getArtifactVersion() {
        return artifactVersion != null ? this.artifactVersion : "-";
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public String getCommit() {
        return commit  != null ? this.commit : "-";
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getStatus() {
        return status  != null ? this.status : "-";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return this.date != null ? this.date : new Date(1990, 07, 26);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateDisplay() {
        return dateDisplay;
    }

    public void setDateDisplay(String dateDisplay) {
        this.dateDisplay = dateDisplay;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    @Override
    public int compareTo(CountryDeploymentStats o) {
        return this.getDate().compareTo(o.getDate());
    }
}
