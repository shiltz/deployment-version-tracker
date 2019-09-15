package io.jenkins.plugin.deploy.statistics.model;

import java.util.*;

public class CountryDeploymentStats implements Comparable<CountryDeploymentStats> {
    private String artifactVersion;
    private String commit;
    private String status;
    private Date date;

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
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(CountryDeploymentStats o) {
        return this.date.compareTo(o.date);
    }
}
