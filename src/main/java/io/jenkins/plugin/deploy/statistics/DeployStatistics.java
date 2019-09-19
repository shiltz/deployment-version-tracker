package io.jenkins.plugin.deploy.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Run;
import io.jenkins.plugin.deploy.statistics.model.CountryDeploymentStats;
import io.jenkins.plugin.deploy.statistics.model.DeploymentConfiguration;
import io.jenkins.plugin.deploy.statistics.model.Environment;
import io.jenkins.plugin.deploy.statistics.model.Project;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class DeployStatistics implements RunAction2 {

    private WorkflowJob project;
    private String selectedEnv;

    public DeployStatistics(WorkflowJob project) {
        this.project = project;
    }

    private String readDeploymentHistory() throws IOException {
        System.out.println("project name:" + project.getFullName());
        String projectName = project.getFullName().replace(" ", "-");
        return new String(Files.readAllBytes(project.getRootDir().toPath().resolve("/var/lib/jenkins/jobs/"+projectName+"/output.json")),
                Charset.defaultCharset());
    }
    private List<Environment> getEnvironments() throws IOException {
        String history = this.readDeploymentHistory();
        System.out.println(history);
        return new ObjectMapper().readValue(history, DeploymentConfiguration.class)
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
                    .map(countryDeployment -> {
                        if(countryDeployment.getDate() != null){
                            try {
                                countryDeployment.setDateDisplay(DateFormat.getDateTimeInstance().format(countryDeployment.getDate()));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                countryDeployment.setDateDisplay(String.valueOf(countryDeployment.getDate()));
                            }
                        }
                        return countryDeployment;
                    })
                    .collect(Collectors.toList());

            Collections.sort(countryDeploymentStats);


            if(countryDeploymentStats.size() > 1){
                country.setCurrentDeploymentStats(countryDeploymentStats.get(countryDeploymentStats.size() - 1));
                country.setPreviousDeploymentStats(countryDeploymentStats.get(countryDeploymentStats.size() - 2));
            } else if (countryDeploymentStats.size() == 1){
                country.setCurrentDeploymentStats(countryDeploymentStats.get(0));
                country.setPreviousDeploymentStats(new CountryDeploymentStats());
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

    public void doDeploymentStatisticsForEnvironment(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        this.selectedEnv = req.getParameter("selectedEnv");
        Environment environment = this.getEnvironment(this.selectedEnv);
        this.addDeploymentSummary(environment);
        rsp.setContentType("application/json");
        rsp.getOutputStream().write(new ObjectMapper().writeValueAsString(environment).getBytes());
        rsp.flushBuffer();
    }

    public void doGetProjectDetails(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        Project project = new Project("");
        if(this.project.getName().contains("sit") || this.project.getName().contains("SIT") ){
            project.setDeploymentEnv("SIT");
        }
        else if(this.project.getName().contains("uat") || this.project.getName().contains("UAT") ){
            project.setDeploymentEnv("UAT");
        }
        if(this.project.getName().contains("prod") || this.project.getName().contains("PROD") ){
            project.setDeploymentEnv("PROD");
        }
        rsp.setContentType("application/json");
        rsp.getOutputStream().write(new ObjectMapper().writeValueAsString(project).getBytes());
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
