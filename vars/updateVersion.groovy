import groovy.json.JsonOutput

class DeploymentConfiguration {
    def countryDeploymentStatsModel

    DeploymentConfiguration(CountryDeploymentStatsModel countryDeploymentStatsModel){
        this.countryDeploymentStatsModel = countryDeploymentStatsModel
    }
}

class CountryDeploymentStatsModel {
    def previousArtifactVersion
    def previousDeploymentDate
    def previousDeploymentStatus
    def currentArtifactVersion
    def currentDeploymentDate
    def currentDeploymentStatus
    def country

    CountryDeploymentStatsModel(CountryDeploymentStatsModel copyCountryDeploymentStatsModel){
        this.country = copyCountryDeploymentStatsModel.country;
        this.previousDeploymentStatus = copyCountryDeploymentStatsModel.currentDeploymentStatus;
        this.previousDeploymentDate = copyCountryDeploymentStatsModel.currentDeploymentDate;
        this.previousArtifactVersion = copyCountryDeploymentStatsModel.currentArtifactVersion;
    }

    CountryDeploymentStatsModel(){

    }
}

CountryDeploymentStatsModel getLastDeploymentForCountry(country){
    def lastDeployment = new CountryDeploymentStatsModel()
    lastDeployment.currentArtifactVersion = "1.0"
    return lastDeployment
}


def process(country) {
    def lastCountryDeploymentStat = getLastDeploymentForCountry(country)
    countryDeploymentStatsModel = new CountryDeploymentStatsModel(lastCountryDeploymentStat)

    def jsonOutput = JsonOutput.toJson(new DeploymentConfiguration(countryDeploymentStatsModel))
//    println jsonOutput
    def historyFile = new File("../jenkins/output.json");
    historyFile.write(jsonOutput)
}

def call(String country){
    process(country)
}
