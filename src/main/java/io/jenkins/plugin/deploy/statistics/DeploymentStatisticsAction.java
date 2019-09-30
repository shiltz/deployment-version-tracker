package io.jenkins.plugin.deploy.statistics;

import hudson.*;
import hudson.model.*;
import jenkins.model.*;

import javax.annotation.*;
import java.util.*;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;


@Extension
public class DeploymentStatisticsAction extends TransientActionFactory<WorkflowJob> {

    public DeploymentStatisticsAction() {
        System.out.println("Loaded .....");
    }

    @Override
    public Class<WorkflowJob> type() {
        return WorkflowJob.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull WorkflowJob project) {
        if(project.getName().contains("deployment") ||
                project.getName().contains("Deployment")) {
            return Collections.singleton(new DeployStatistics(project));
        }
        else {
            return Collections.EMPTY_SET;
        }
    }
}