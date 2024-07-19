package io.jenkins.plugins.sample.global_configuration;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;


@Extension
@Symbol("OnboardingPlugin")
public class OnboardingPluginGlobalConfiguration extends GlobalConfiguration {

    private String name;
    private String description;
    private String url;
    private String username;
    private Secret password;

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
            return FormValidation.error("Connection Failed: Provided configuration details are not correct. Response Code: "+ responseFuture.statusCode());
        }
        return FormValidation.ok("<>Connection Success!!! ");
    }
}