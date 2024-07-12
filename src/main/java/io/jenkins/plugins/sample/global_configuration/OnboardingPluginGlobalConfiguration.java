package io.jenkins.plugins.sample.global_configuration;

import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.QueryParameterMap;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;


@Extension
@Symbol("OnboardingPlugin")
public class OnboardingPluginGlobalConfiguration extends GlobalConfiguration {

    public FormValidation doCheckName(@QueryParameter String name) {
        String regex = "^[a-zA-Z ]+$";
        if (!name.matches(regex)) {
            return FormValidation.warning("Name must contains characters & spaces");
        }
        return FormValidation.ok();
    }
}