package io.jenkins.plugins.sample.global_configuration;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;

@Extension
@Symbol("OnboardingPlugin")
public class OnboardingPluginGlobalConfiguration extends GlobalConfiguration {

}