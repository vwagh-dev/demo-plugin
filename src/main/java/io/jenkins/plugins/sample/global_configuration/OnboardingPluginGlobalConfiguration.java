package io.jenkins.plugins.sample.global_configuration;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


@Extension
@Symbol("OnboardingPlugin")
public class OnboardingPluginGlobalConfiguration extends GlobalConfiguration {

    private String name;
    private String description;
    private String url;
    private String username;
    private Secret password;

    private List<Category> categories;

    public OnboardingPluginGlobalConfiguration(){
        load();
        if (categories == null) {
            categories = new ArrayList<>();
        }
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Secret getPassword() {
        return password;
    }

    public void setPassword(Secret password) {
        this.password = password;
        System.out.println("Password: " + password);
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        save();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public FormValidation doCheckName(@QueryParameter String name) {
        String regex = "^[a-zA-Z ]+$";
        if (!name.matches(regex)) {
            return FormValidation.warning("Name must contains characters & spaces");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckUsername(@QueryParameter String username) {
        String regex = "^[a-zA-Z]+$";
        if (!username.matches(regex)) {
            return FormValidation.warning("Username must contains letters only");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckPwd(@QueryParameter String pwd) {
        System.out.println("Check file system pwd:::::::: " + pwd);
        return FormValidation.ok();
    }

    @POST
    public FormValidation doTestConnection(@QueryParameter String url, @QueryParameter String username, @QueryParameter Secret password) throws IOException, InterruptedException {
        String credentials = String.join(":", username, password.getPlainText());
        String headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Authorization", headerValue)
                .GET().build();

        var responseFuture = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (responseFuture.statusCode() != 200) {
            return FormValidation.error("Connection Failed: Provided configuration details are not correct. Response Code: " + responseFuture.statusCode());
        }
        return FormValidation.ok("<>Connection Success!!! ");
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        List<Category> submittedCategories = req.bindJSONToList(Category.class, json.get("categories"));

        // Generate UUID for new categories
        for (Category category : submittedCategories) {
            if (category.getUuid() == null || category.getUuid().isEmpty()) {
                category.setUuid(UUID.randomUUID().toString());
            }
        }
        setCategories(submittedCategories);
        save();
        return true;
    }

    public static class Category {
        private String name;
        private String uuid;
        @DataBoundConstructor
        public Category(String name) {
            this.name = name;
            this.uuid = UUID.randomUUID().toString();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    /**
     * Using credentials plugin i.e SecretText
     *
     */
    @POST
    public FormValidation doTestPayload(@AncestorInPath Item item, @QueryParameter String url, @QueryParameter String username,
                                         @QueryParameter String credentialsId) throws IOException, InterruptedException {
        DomainRequirement domainRequirement = new DomainRequirement();
        List<StandardCredentials> credentials =
                CredentialsProvider.lookupCredentials(StandardCredentials.class, item, ACL.SYSTEM, domainRequirement);
        Optional<StringCredentials> optStandardCredentials = credentials.stream().filter(standardCredentials ->
                        standardCredentials instanceof StringCredentials && standardCredentials.getId().matches(credentialsId))
                .map(standardCredentials -> (StringCredentials) standardCredentials)
                .findFirst();
        if (optStandardCredentials.isPresent()) {
            Secret password = optStandardCredentials.get().getSecret();
            String headerValue = "Basic "+ username+":"+ password.getEncryptedValue();
            var client = HttpClient.newHttpClient();
            //https://vshal.free.beeceptor.com
            var request = HttpRequest.newBuilder().uri(URI.create(url))
                    .header("Authorization", headerValue)
                    .POST(HttpRequest.BodyPublishers.ofString(optStandardCredentials.get().getSecret().getPlainText())).build();
            var responseFuture = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseFuture.statusCode() != 200) {
                return FormValidation.error("Payload test Failed: Provided configuration details are not correct. Response Code: " + responseFuture.statusCode());
            }
        }

        return FormValidation.ok("Test connection successful");
    }

    //Fills the dropdown list when any credentials get added.
    @POST
    public ListBoxModel doFillCredentialsIdItems(
            @AncestorInPath Item item, @QueryParameter String credentialsId,
            @QueryParameter String url) {
        StandardListBoxModel result = new StandardListBoxModel();
        DomainRequirement domainRequirement = new DomainRequirement();
        List<StandardCredentials> credentials =
                CredentialsProvider.lookupCredentials(StandardCredentials.class, item, ACL.SYSTEM, domainRequirement);

        for (StandardCredentials c : credentials) {
            if (c instanceof StringCredentials) {
                result.add(c.getId(), c.getId());
            }
        }
        return result.includeCurrentValue(credentialsId); // (5)
    }
}