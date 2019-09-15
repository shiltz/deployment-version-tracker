package io.jenkins.plugin.deploy.statistics.model;

import java.util.*;

public class Environment {
    String name;
    List<Country> countries = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }
}
