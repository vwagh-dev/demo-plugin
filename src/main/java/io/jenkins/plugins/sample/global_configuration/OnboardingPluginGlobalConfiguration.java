package io.jenkins.plugins.sample.global_configuration;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;


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

    public FormValidation doCheckName(@QueryParameter String name) {
        String regex = "^[a-zA-Z ]+$";
        if (!name.matches(regex)) {
            return FormValidation.warning("Name must contains characters & spaces");
        }
        return FormValidation.ok();
    }
}