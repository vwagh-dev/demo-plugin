package io.jenkins.plugins.sample.global_configuration;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;

@Extension
@Symbol("OnboardingPlugin")
public class OnboardingPluginGlobalConfiguration extends GlobalConfiguration {
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}