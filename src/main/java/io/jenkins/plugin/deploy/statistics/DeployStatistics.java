package io.jenkins.plugin.deploy.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.plugin.deploy.statistics.model.CountryDeploymentStats;
import io.jenkins.plugin.deploy.statistics.model.Deployable;
import io.jenkins.plugin.deploy.statistics.model.DeploymentConfiguration;
import io.jenkins.plugin.deploy.statistics.model.DeploymentDetails;
import io.jenkins.plugin.deploy.statistics.model.Environment;
import io.jenkins.plugin.deploy.statistics.model.Project;
import jenkins.model.Jenkins;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class DeployStatistics implements RunAction2 {

    public static final String TMO = "TMO";
    public static final String TMW = "TMW";
    private WorkflowJob project;
    private String selectedEnv;
    private Run run;

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
        else if(this.project.getName().contains("prod") || this.project.getName().contains("PROD") ){
            project.setDeploymentEnv("PROD");
        } else if(this.project.getName().contains("PPE") || this.project.getName().contains("ppe")
                    || this.project.getName().contains("pre-prod") || this.project.getName().contains("PRE-PROD")){
            project.setDeploymentEnv("PRE");
        }  else {
            project.setDeploymentEnv("");
        }
        rsp.setContentType("application/json");
        rsp.getOutputStream().write(new ObjectMapper().writeValueAsString(project).getBytes());
        rsp.flushBuffer();
    }

    public void doGetDeploymentDetails(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        DeploymentDetails deploymentDetails = new DeploymentDetails();
        String buildJobName = "TMO MultiBranch/dev and sit jobs";

        if (this.project.getFullName().contains(TMO)){
            deploymentDetails.setAppName("tmo");
            buildJobName = "Trade Management Online (TMO)/TMO MultiBranch";
        } else if(this.project.getFullName().contains(TMW)) {
            deploymentDetails.setAppName("tmw");
            buildJobName = "Trade Management Workflow (TMW)/Trade Management Workflow";
        } else {
            deploymentDetails.setAppName("TRADEMANAGEMENT");
            buildJobName = "Trade Management Backend/TM Backend - Builds Latest";
        }



        /** Exxample names of jobs
         * I:sit-newpipe
         * Ii:sit-newpipe
         * j:sit-newpipe
         * I:TMO MultiBranch
         * Ii:TMO MultiBranch
         * j:TMO MultiBranch/dev and sit jobs/master
         * j:TMO MultiBranch/new sit deploy
         * j:TMO MultiBranch/dev and sit jobs/release%2Fshaka
         * I:dev and sit jobs
         * Ii:TMO MultiBranch/dev and sit jobs
         * j:TMO MultiBranch/dev and sit jobs/master
         * j:TMO MultiBranch/dev and sit jobs/release%2Fshaka
         * I:master
         * Ii:TMO MultiBranch/dev and sit jobs/master
         * j:TMO MultiBranch/dev and sit jobs/master
         * I:release%2Fshaka
         * Ii:TMO MultiBranch/dev and sit jobs/release%2Fshaka
         * j:TMO MultiBranch/dev and sit jobs/release%2Fshaka
         * I:new sit deploy
         * Ii:TMO MultiBranch/new sit deploy
         * j:TMO MultiBranch/new sit deploy
         * project name:TMO MultiBranch/new sit deploy
         */
        Item item = Jenkins.getInstance().getItemByFullName(buildJobName);

        HashMap<String, Set<String>> branchArtifactMap = new HashMap<>();
        List<Environment> environments = getEnvironments();
        environments.forEach(environment -> {
            environment.getCountries().forEach(country -> {
                country.getDeploymentStats().forEach(countryDeploymentStats -> {
                    Set<String> artifacts = branchArtifactMap.get(countryDeploymentStats.getBranchName());
                    if(artifacts != null && artifacts.size() > 0 ){
                        artifacts.add(countryDeploymentStats.getArtifactVersion());
                    } else {
                        TreeSet<String> artifactList = new TreeSet<>();
                        artifactList.add(countryDeploymentStats.getArtifactVersion());
                        branchArtifactMap.put(countryDeploymentStats.getBranchName(), artifactList);
                    }
                });
            });
        });
        List<Deployable>  deployables2 = new ArrayList<>();
        branchArtifactMap.keySet().forEach(branch -> {
            deployables2.add(new Deployable(branch, new ArrayList<>(), new ArrayList<>(branchArtifactMap.get(branch))));
        });


        List<Deployable> deployables = item.getAllJobs().stream()
                .filter(o -> o.getName().contains("master") || o.getName().contains("release"))
                .map(o -> {
                    List<Object> builds = new ArrayList<>();
                    String formatted = o.getName().replace("%2F", "/");
                    RunList runList = o.getBuilds();
                    runList.forEach(o1 ->  builds.add(o1));

                    List<String> collectBuild =  builds.stream().sorted(Comparator.comparingInt(o2 -> ((Run) (o2)).getNumber()))
                    .map(o2 -> ((Run) (o2)).getNumber() + "")
                    .collect(Collectors.toList());

                    return new Deployable(formatted,
                            collectBuild,
                            new ArrayList<>());
                }).collect(Collectors.toList());

        HashMap<String, Deployable> branchDeployableMap = new HashMap<>();

        deployables2.forEach(deployable -> {
                Deployable newDeployable = new Deployable(deployable.getBranchName(), new ArrayList<>(), deployable.getAvailableArtifacts());
                branchDeployableMap.put(deployable.getBranchName(), newDeployable);
        });


        deployables.forEach(deployable -> {
            Deployable existingDeployable = branchDeployableMap.get(deployable.getBranchName());
            if(existingDeployable != null){
                existingDeployable.setAvailableBuilds(deployable.getAvailableBuilds());
            } else {
                Deployable newDeployable = new Deployable(deployable.getBranchName(), deployable.getAvailableBuilds(), new ArrayList<>());
                branchDeployableMap.put(deployable.getBranchName(), newDeployable);
            }
        });


        deploymentDetails.setDeployable(new ArrayList<>(branchDeployableMap.values()));



        deploymentDetails.setProjectName(this.project.getFullName());
        deploymentDetails.setUrl(Jenkins.getInstance().getRootUrl() + this.project.getUrl());
        rsp.setContentType("application/json");
        rsp.getOutputStream().write(new ObjectMapper().writeValueAsString(deploymentDetails).getBytes());
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
        this.run = run;
        System.out.println("onAttached" + run.toString());
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
        System.out.println("onLoad" + run.toString());
    }

    public Run<?,?> getRun() {
        return this.run;
    }
}
