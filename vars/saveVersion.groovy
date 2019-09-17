import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import hudson.model.Hudson
import jenkins.model.Jenkins

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
  configuration.environments.each{ value, key -> 
  		if(value.name.equals(environmentName)){
  		  environment1 = value;
	 	}
	}
  //Environment environment1;
  //for(Environment environment : configuration.environments){
  //  if(environment.name.equals(environmentName)){
  //    environment1 = environment;
  //    break;
  //  }
 // }
  if(environment1 != null){
    environment1.countries.each{ value, key -> 
  		if(value.name.equals(countryName)){
  		  country1 = value;
	 	}
	}
//    for(Country country : environment.countries){
//      if(country.name.equals(countryName)){
//          return country;
//      } 
//    }
//    return new Country(countryName, new ArrayList<>());
    return country1;
  } else {
 //   return new Country(countryName, new ArrayList<>());
  }
}

DeploymentConfiguration readDeploymentConfiguration(filename) {
    return new JsonSlurper().parse(new FileReader(filename));
}

CountryDeploymentStatsModel generateCountryDeploymentStatsModel() {
  def countryUpdate = new CountryDeploymentStatsModel()
    countryUpdate.artifactVersion = "${MAJOR_VERSION}.${BUILD_NUMBER}"
    countryUpdate.commit = "-"
    countryUpdate.date = new Date().getTime()
    countryUpdate.status = "SUCCESSFUL"
  	countryUpdate.branchName = "${BRANCH_NAME}"
  	
  
  def items = new LinkedHashSet();
  name = "newpipe"
		def job = Hudson.getInstance().getJob(name)
		items.add(job);
    
      items.each { item ->
        def job_data = Jenkins.getInstance().getItemByFullName(item.fullName)
        
        //Check if job had atleast one build done
        if (job_data.getLastBuild()) {
            last_job_num = job_data.getLastBuild().getNumber()
            def upStreamBuild = Jenkins.getInstance().getItemByFullName(item.fullName).getBuildByNumber(last_job_num)
          
          echo 'last_job_num' + last_job_num
          echo 'upStreamBuild' + upStreamBuild.getNumber()
          countryUpdate.buildNumber = last_job_num

          //countryUpdate.status = upStreamBuild.result
          echo 'countryUpdate.status' + upStreamBuild.result
          echo 'countryUpdate.status1' + job_data.getLastBuild().result
          
        } else {
            echo 'LastBuildNumer: Null'
        }
        
      }
  
    
    return countryUpdate
}

def addNewCountryDeploymentStat(env, country, input, countryUpdate) {
    def foundCountry = getCountry(env, country, input)
    foundCountry.deploymentStats.add(countryUpdate)
}

def writeNewDeploymentConfig(input, filename){
    def writer = new BufferedWriter(new FileWriter(filename));
    writer.write(JsonOutput.toJson(input))
    writer.flush()
    writer.close()
}


def process(String country, String env) {
    final String FILE_NAME = "/var/lib/jenkins/jobs/newpipe/output.json"
    try {
        // Read File
        DeploymentConfiguration input = readDeploymentConfiguration(FILE_NAME)

        // Update
        addNewCountryDeploymentStat(env, country, input, generateCountryDeploymentStatsModel())

        // Write
        writeNewDeploymentConfig(input, FILE_NAME);

    } catch (Exception e){
        e.printStackTrace()
    }
}


def call(String country, String env){
    process(country, env)
}
