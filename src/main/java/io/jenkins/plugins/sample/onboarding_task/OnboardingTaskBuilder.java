package io.jenkins.plugins.sample.onboarding_task;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.sample.global_configuration.OnboardingPluginGlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;


public class OnboardingTaskBuilder extends Builder implements SimpleBuildStep {

    private final String selectedCategoryUuid;

    @DataBoundConstructor
    public OnboardingTaskBuilder(String selectedCategoryUuid) {
        this.selectedCategoryUuid = selectedCategoryUuid;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        OnboardingPluginGlobalConfiguration globalConfig = OnboardingPluginGlobalConfiguration.get();
        if (globalConfig != null) {
            List<OnboardingPluginGlobalConfiguration.Category> selectedCategory = globalConfig.getCategories();
            for (OnboardingPluginGlobalConfiguration.Category category : selectedCategory) {
                if (category != null && selectedCategoryUuid.equals(category.getUuid())) {
                    listener.getLogger().println("Selected Category: Uuid = " + category.getUuid()+", name= " + category.getName());
                }
            }
        } else {
            listener.getLogger().println("Global Configuration not found.");
        }
    }


    @Symbol("Category")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Onboarding Task";
        }

        public ListBoxModel doFillSelectedCategoryUuidItems() {
            ListBoxModel items = new ListBoxModel();
            OnboardingPluginGlobalConfiguration config = OnboardingPluginGlobalConfiguration.get();
            if (config != null) {
                for (OnboardingPluginGlobalConfiguration.Category category : config.getCategories()) {
                    items.add(category.getName(), category.getUuid());
                }
            }
            return items;
        }
    }

}
