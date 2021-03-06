import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import hudson.model.Hudson
import jenkins.model.Jenkins
import java.nio.file.Paths

class DeploymentConfiguration {
    List<Environment> environments  
}

class Environment {
    String name
    List<Country> countries

    Environment(String name, List<Country> countryList) {
        this.name = name
        this.countries = countryList
    }

    Environment(String name){
        this.name = name
        this.countries = new ArrayList<>()
    }
  
  Environment(){}
}

class Country {
    String name
    List<CountryDeploymentStatsModel> deploymentStats;

    Country(String name, List<CountryDeploymentStatsModel> countryDeploymentStatsModel){
        this.deploymentStats = countryDeploymentStatsModel
        this.name = name
    }

    Country() {

    }
}

class CountryDeploymentStatsModel {
    String artifactVersion
    String commit
    String status
    String date
  	String branchName
  	String buildNumber

    CountryDeploymentStatsModel(CountryDeploymentStatsModel copyCountryDeploymentStatsModel){
      	this.artifactVersion = copyCountryDeploymentStatsModel.artifactVersion;
        this.commit = copyCountryDeploymentStatsModel.commit;
        this.status = copyCountryDeploymentStatsModel.status;
        this.date = copyCountryDeploymentStatsModel.date;
      	this.branchName = copyCountryDeploymentStatsModel.branchName;
      	this.buildNumber = copyCountryDeploymentStatsModel.buildNumber;
    }

    CountryDeploymentStatsModel(){

    }
}

Country getCountry(String environmentName, String countryName, DeploymentConfiguration configuration){
  Environment environment1;
  Country country1;
  
  if(configuration.environments != null && configuration.environments.size() > 0){
    configuration.environments.each{ value, key -> 
  		if(value.name.equals(environmentName)){
  		  environment1 = value;
	 	}
	}
  } else {
    configuration.environments = new ArrayList<>()
    Environment newEnvironment = new Environment(environmentName)
    configuration.environments.add(newEnvironment)
    Country newCountry = new Country(countryName, new ArrayList<>())
    newEnvironment.countries.add(newCountry);
    return newCountry
  }
  

  if(environment1 != null){
    environment1.countries.each{ value, key -> 
  		if(value.name.equals(countryName)){
  		  country1 = value
	 	}
	}
    if(country1 != null){
      return country1
    } else {
      Country newCountry = new Country(countryName, new ArrayList<>())
      environment1.countries.add(newCountry)
      return newCountry
    }

    
  } else {
    Country newCountry = new Country(countryName, new ArrayList<>())
    Environment newEnvironment = new Environment(environmentName)
    newEnvironment.countries.add(newCountry);
    configuration.environments.add(newEnvironment);
    return newCountry;
  }
}

DeploymentConfiguration readDeploymentConfiguration(filename) {
  try{
    FileReader reader = new FileReader(filename)
    return new JsonSlurper().parse(reader)
  } catch(java.io.FileNotFoundException v){
    System.out.println("Create a new file")
    
    File f = new File(Paths.get(filename).getParent().toString())
    f.mkdirs()
    
    def writer = new BufferedWriter(new FileWriter(filename))
    writer.write(JsonOutput.toJson(new DeploymentConfiguration()))
    writer.flush()
    writer.close()
    return new JsonSlurper().parse(new FileReader(filename))
  }
    
}

CountryDeploymentStatsModel generateCountryDeploymentStatsModel(String projectName) {
  def countryUpdate = new CountryDeploymentStatsModel()
    countryUpdate.artifactVersion = "${MAJOR_VERSION}.${BUILD_NUMBER}"
    countryUpdate.commit = "-"
    countryUpdate.date = new Date().getTime()
    countryUpdate.status = "SUCCESSFUL"
  	countryUpdate.branchName = "${BRANCH_NAME}"
  	
  	int lastBuildNumber = Jenkins.getInstance().getItemByFullName(projectName, Job.class).getLastBuild().getNumber();
  	countryUpdate.buildNumber = lastBuildNumber.toString()
 
    return countryUpdate
}

def addNewCountryDeploymentStat(env, country, input, countryUpdate) {
    def foundCountry = getCountry(env, country, input)
    foundCountry.deploymentStats.add(countryUpdate)
}

def writeNewDeploymentConfig(input, filename){
    def writer = new BufferedWriter(new FileWriter(filename))
    writer.write(JsonOutput.toJson(input))
    writer.flush()
    writer.close()
}


def process(String country, String env, String projectName) {
  	String formattedProjectName = projectName.replace(" ", "-")
  	
    final String FILE_NAME = "/var/lib/jenkins/jobs/" + formattedProjectName + "/output.json"
    try {
        // Read File
        DeploymentConfiguration input = readDeploymentConfiguration(FILE_NAME)

        // Update
        addNewCountryDeploymentStat(env, country, input, generateCountryDeploymentStatsModel(projectName))

        // Write
        writeNewDeploymentConfig(input, FILE_NAME);

    } catch (Exception e){
        e.printStackTrace()
    }
}


def call(String country, String env, String projectName){
    process(country, env, projectName)
}
