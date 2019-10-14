ko.observableArray.fn.find = function(prop, data) {
    var valueToMatch = data[prop];
    return ko.utils.arrayFirst(this(), function(item) {
        return item[prop] === valueToMatch;
    });
};

var appModel;
function AppViewModel() {
    appModel = this;
    this.environments = ko.observableArray([
        {
            deploymentEnv: undefined
        },
        {
            deploymentEnv: "SIT"
        },
        {
            deploymentEnv: "UAT"
        },
        {
            deploymentEnv: "PRE"
        },
        {
            deploymentEnv: "PROD"
        }
    ]);
    this.env =  ko.observable({});
    this.env.subscribe(function(event){
        if(event.deploymentEnv !== undefined)
            getDeploymentStatisticsForEnvironment(event.deploymentEnv)
    });

    this.selectedCountryStats = ko.observableArray([])
    this.selectedCountryName = ko.observable('');

    this.environment = ko.observableArray([])

    this.selectedBranch = ko.observable('');
    this.availableBuilds = ko.observable('');
    this.availableArtifacts = ko.observable('');
    this.selectedBranch.subscribe(function(event){
        if(event){
            if(appModel.releaseType() === 'new'){
                appModel.availableBuilds(event.availableBuilds);
                appModel.selectedBuild(undefined);
            } else {
                appModel.availableArtifacts(event.availableArtifacts);
                appModel.selectedArtifact(undefined);
            }
            appModel.isDeployable(appModel.canDeploy());
        }
    });
    this.releaseType = ko.observable('');
    this.releaseType.subscribe(function(event){
        if(event){
            appModel.availableBuilds('');
            appModel.availableArtifacts('');
            appModel.selectedBranch('');
            appModel.isDeployable(appModel.canDeploy());
        }
    });

    this.selectedDeployableCountries = ko.observableArray([]);
    this.majorVersion = ko.observable('');
    this.majorVersion.subscribe(function(event){
        appModel.isDeployable(appModel.canDeploy());
    });
    this.selectedBuild = ko.observable('');
    this.selectedBuild.subscribe(function(event){
        appModel.isDeployable(appModel.canDeploy());
    });
    this.selectedArtifact = ko.observable('');
    this.selectedArtifact.subscribe(function(event){
        appModel.isDeployable(appModel.canDeploy());
    });

    this.isAllSelected = false;
    this.selectedDeployableCountries.subscribe(function(selected){
        if(selected && selected.length > 0){
            var isAllChecked = selected.find(function(country){
                return country === 'All'
            });
            if(isAllChecked && !appModel.isAllSelected){
                appModel.isAllSelected = true;
                while (selected.length > 0) {
                    appModel.selectedDeployableCountries().pop()
                }

                appModel.deploymentDetail().deployableCountriesList.forEach(function (stat) {
                    appModel.selectedDeployableCountries().push(stat)
                })
            } else if(!isAllChecked && appModel.isAllSelected) {
                appModel.isAllSelected = false;
                while (selected.length > 0) {
                    appModel.selectedDeployableCountries().pop()
                }
            } else if(isAllChecked && selected.length !== appModel.deploymentDetail().deployableCountriesList.length) {
                var index = appModel.selectedDeployableCountries().findIndex(function(country){
                    return country === 'All'
                });
                appModel.selectedDeployableCountries().splice(index, 1);
                appModel.isAllSelected = false;

            }
        }
        appModel.isDeployable(appModel.canDeploy());
    });

    this.isDeployable = ko.observable(false);

    this.deploymentDetail = ko.observable('');

    this.deployButtonDisable = ko.observable(false);

    this.shouldBeEnabled = ko.computed(function() {
        return !this.deployButtonDisable() && this.isDeployable();
    }, this);

    this.response = ko.observable();

    this.canDeploy = function () {
        if(appModel.releaseType() === 'promote'){
            return (appModel.selectedArtifact() && appModel.selectedArtifact() !== '')
                && (appModel.selectedBranch() && appModel.selectedBranch() !== '')
                && (appModel.selectedDeployableCountries() && appModel.selectedDeployableCountries().length > 0)

        } else if(appModel.releaseType() === 'new'){
            return (appModel.selectedBuild() && appModel.selectedBuild() !== '')
                && (appModel.selectedBranch() && appModel.selectedBranch() !== '')
                && (appModel.majorVersion() && appModel.majorVersion() !== '')
                && (appModel.selectedDeployableCountries() && appModel.selectedDeployableCountries().length > 0)

        } else{
            return false;
        }
    };

    this.onCountryClick = function(click) {
        document.getElementById('id01').style.display='block'
        if(click.deploymentStats) {
            appModel.selectedCountryName(click.name);
            while(appModel.selectedCountryStats().length > 0) {
                appModel.selectedCountryStats.pop()
            }

            click.deploymentStats.forEach(function(stat){
                appModel.selectedCountryStats.push(stat)
            })

        }

    }

    this.deploy = function () {
        var deploymentCountryParam = '';
        var buildNumberParam = '';
        var branchNameParam = '';
        var majorVersionParam = '';
        var appNameParam = '&APP_NAME=' + appModel.deploymentDetail().appName;
        var urlParams = appModel.deploymentDetail().url + 'buildWithParameters?token=D6B973260A7C44E7AE4622EADC959B19&';
        if(appModel.releaseType() === 'new') {
            buildNumberParam = 'BUILD_NUMBER=' + appModel.selectedBuild();
            branchNameParam = '&BRANCH_NAME=' + encodeURIComponent(appModel.selectedBranch().branchName);
            majorVersionParam = '&MAJOR_VERSION=' + appModel.majorVersion();

        } else if(appModel.releaseType() === 'promote') {
            var splitArtifact = appModel.selectedArtifact().split(".");
            buildNumberParam = 'BUILD_NUMBER=' + splitArtifact[1];
            branchNameParam = '&BRANCH_NAME=' + encodeURIComponent(appModel.selectedBranch().branchName);
            majorVersionParam = '&MAJOR_VERSION=' + splitArtifact[0];

        } else {
            throw new Error('Unknown release type')
        }


        if(appModel.selectedDeployableCountries().find(function(country){
            return country === 'All'
        })){
            appModel.deploymentDetail().deployableCountriesList.forEach(function (country) {
                if(country !== 'All') {
                    deploymentCountryParam = deploymentCountryParam + 'SKIP_'+country.replace(' ','_').toUpperCase()+'_INPUT=true&';
                }
            })

        } else {
            appModel.selectedDeployableCountries().forEach(function(country){
                deploymentCountryParam = deploymentCountryParam + 'SKIP_'+country.replace(' ','_').toUpperCase()+'_INPUT=true&';
            });
        }
        urlParams = urlParams + deploymentCountryParam + buildNumberParam + branchNameParam + majorVersionParam + appNameParam;
        console.log(encodeURI(urlParams));
        appModel.deployButtonDisable(true);
        new Ajax.Request(urlParams, {
            method: 'POST',
            parameters: {},
            onSuccess: function(response) {
                if(response.status === 201){
                    appModel.deployButtonDisable(false);
                }
                appModel.response(response)
            },
            onFailure: function(response) {
                appModel.deployButtonDisable(false);
                appModel.response(response)
            }
        });
    }

}

function getDeploymentStatisticsForEnvironment(env){
    new Ajax.Request('deploymentStatisticsForEnvironment', {
        method: 'GET',
        parameters: {selectedEnv: env},
        onSuccess: function(response) {
            while(appModel.environment().length > 0) {
                appModel.environment.pop()
            }
            if(response.responseJSON){
                response.responseJSON.countries.forEach(function(country){
                    appModel.environment.push(country)
                })
            }
        }
    });
}

function getProjectDetails(){
    new Ajax.Request('getProjectDetails', {
        method: 'GET',
        parameters: {},
        onSuccess: function(response) {
            if(response.responseJSON){
                var found = appModel.environments.find("deploymentEnv", response.responseJSON)
                appModel.env(found)
            }
        }
    });
}

function getDeploymentDetails(){
    new Ajax.Request('getDeploymentDetails', {
        method: 'GET',
        parameters: {},
        onSuccess: function(response) {
            if(response.responseJSON){
                appModel.deploymentDetail(response.responseJSON);
            }
        }
    });
}

function openTab(evt, id) {
    var x = document.getElementsByClassName('tab')

    for(var i = 0; i < x.length; i++){
        x[i].style.display = 'none'
    }


    var links = document.getElementsByClassName('w3-bar-item')

    for(var i = 0; i < links.length; i++){
        links[i].className = links[i].className.replace(' nav_bar_selected', '')
    }


    document.getElementById(id).style.display = 'block'
    evt.currentTarget.className = evt.currentTarget.className + ' nav_bar_selected'
}

ko.applyBindings(new AppViewModel());
getProjectDetails();
getDeploymentDetails();