import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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

    CountryDeploymentStatsModel(CountryDeploymentStatsModel copyCountryDeploymentStatsModel){
        this.artifactVersion = copyCountryDeploymentStatsModel.artifactVersion;
        this.commit = copyCountryDeploymentStatsModel.commit;
        this.status = copyCountryDeploymentStatsModel.status;
        this.date = copyCountryDeploymentStatsModel.date;
    }

    CountryDeploymentStatsModel(){

    }
}

Country getCountry(String environmentName, String countryName, DeploymentConfiguration configuration){
  Optional<Environment> environment1 = configuration.environments.stream().filter({environment -> environment.name.equals(environmentName)}).findFirst()
  return environment1.orElse(new Environment(environmentName)).countries.stream().filter({country -> country.name.equals(countryName)}).findFirst().orElse(new Country(countryName, new ArrayList<>()))
}

DeploymentConfiguration readDeploymentConfiguration(filename) {
    return new JsonSlurper().parse(new FileReader(filename));
}

CountryDeploymentStatsModel generateCountryDeploymentStatsModel() {
    def countryUpdate = new CountryDeploymentStatsModel()
    countryUpdate.artifactVersion = "${MAJOR_VERSION}.${BUILD_NUMBER}"
    countryUpdate.commit = "${MAJOR_VERSION}.${BUILD_NUMBER}"
    countryUpdate.date = "1568558436"
    countryUpdate.status = "SUCCESSFUL"
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
