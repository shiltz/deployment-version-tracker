package io.jenkins.plugin.deploy.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Run;
import io.jenkins.plugin.deploy.statistics.model.CountryDeploymentStats;
import io.jenkins.plugin.deploy.statistics.model.DeploymentConfiguration;
import io.jenkins.plugin.deploy.statistics.model.Environment;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;


public class DeployStatistics implements RunAction2 {

    private WorkflowJob project;
    private String selectedEnv;

    public DeployStatistics(WorkflowJob project) {
        this.project = project;
    }

    private String readDeploymentHistory() throws IOException {
        return new String(Files.readAllBytes(project.getRootDir().toPath().resolve("workspace@libs/deployer/work/workspace/"+project.getName()+"/output.json")),
                Charset.defaultCharset());
    }
    private List<Environment> getEnvironments() throws IOException {
        return new ObjectMapper().readValue(this.readDeploymentHistory(), DeploymentConfiguration.class)
                    .getEnvironments();
    }

    private Environment getEnvironment(String name) throws IOException {
        return this.getEnvironments().stream()
                .filter(environment -> environment.getName().equals(name))
                .findFirst().orElse(new Environment());
    }

    private void addDeploymentSummary(final Environment environment) {
        environment.getCountries().forEach((country) -> {
            List<CountryDeploymentStats> countryDeploymentStats = country.getDeploymentStats()
                    .stream()
                    .sorted()
                    .limit(2)
                    .collect(Collectors.toList());

            if(countryDeploymentStats.size() == 2){
                country.setCurrentDeploymentStats(countryDeploymentStats.get(0));
                country.setPreviousDeploymentStats(countryDeploymentStats.get(1));
            } else if (countryDeploymentStats.size() == 1){
                country.setCurrentDeploymentStats(countryDeploymentStats.get(0));
            } else {
                System.out.println("No history:" + countryDeploymentStats.size());
            }
        });
    }


    public WorkflowJob getProject() {
        return project;
    }

    public void setProject(WorkflowJob project) {
        this.project = project;
    }

    public String getSelectedEnv() {
        return this.selectedEnv;
    }

    public void setSelectedEnv(String selectedEnv) {
        this.selectedEnv = selectedEnv;
    }

    public String doTestConnection(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        this.selectedEnv = req.getParameter("selectedEnv");
        return this.selectedEnv;
    }

    public void doDeploymentStatisticsForEnvironment(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        this.selectedEnv = req.getParameter("selectedEnv");
        Environment environment = this.getEnvironment(this.selectedEnv);
        this.addDeploymentSummary(environment);
        rsp.setContentType("application/json");
        rsp.getOutputStream().write(new ObjectMapper().writeValueAsString(environment).getBytes());
        rsp.flushBuffer();
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "clipboard.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Deploy History";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "stats";
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        System.out.println("onAttached" + run.toString());
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        System.out.println("onLoad" + run.toString());
    }
}
