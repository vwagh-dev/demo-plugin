# demo

## Introduction

https://engineering.beescloud.com/docs/engineering-cbci/latest/onboarding/practical-work-plugin#_level_7b
depends on previous steps i.e. https://engineering.beescloud.com/docs/engineering-cbci/latest/onboarding/practical-work-plugin#_level_7

## Getting started

1. Create  a pipeline
   New Item -> Select Pipeline -> Goto pipeline section & select the 'Pipeline script' option & wrote the below code in the script section.
   ```
   pipeline {
       agent any
       
       environment{
           userInput = null
           GLOBAL_FILE_PATH = 'build_data.txt'
       }
      
       stages {
           stage('Category Selection') {
               steps {
                   
                   script {
                       
                       def dynamicChoices = []
                   
                       // Extracting serializable data from non-serializable object
                       def myConfig = jenkins.model.GlobalConfiguration.all().get(io.jenkins.plugins.sample.global_configuration.OnboardingPluginGlobalConfiguration.class) 
                       //First try to read the categories
                       def categoryNames = myConfig.getCategories().collect { it.name.toString() }
                       
                       dynamicChoices.addAll(categoryNames)
                       
                       println "Preparing dynamic choice list: ${categoryNames}"
                       myConfig = null
                       userInput = input message: 'Please select an Onboarding Task category', ok: 'Proceed',
                                             parameters: [choice(name: 'CATEGORY', choices: dynamicChoices.join('\n'), description: 'Select the onboarding category')]
   
                       echo "User selected category: ${userInput}"
                       
                   }
               }
           }
   
           stage('Perform Onboarding Task') {
               steps {
                   script {
                       def selectedCategory = userInput
                       echo "Performing task for selected category: ${userInput}, selectedCategory: ${selectedCategory}"
                       // Update global file with the latest build data
                       updateGlobalFile(selectedCategory)
   
                   }
               }
           }
       }
   }


   // Function to update global file with the latest build data
   def updateGlobalFile(String selectedCategory) {
      def globalFile = new File(env.GLOBAL_FILE_PATH)
      //Fetching the build url
      echo "Fetching current build url"
      def currentBuildLink = getCurrentBuildLink()
      echo "currentBuildLink link is : ${currentBuildLink}"
   
       def buildData = "Build #${env.BUILD_NUMBER}, Category: ${selectedCategory}, Link: ${currentBuildLink}"
   
       // Create the file if it doesn't exist
       if (!globalFile.exists()) {
           globalFile.createNewFile()
       }
   
       // Read existing file content
       def lines = globalFile.readLines()
   
       // Add the latest build data at the beginning
       lines = [buildData] + lines
   
       // Keep only the latest 5 builds
       if (lines.size() > 5) {
           lines = lines.take(5)
       }
   
       // Write the updated list back to the file
       globalFile.text = lines.join('\n')
   
       echo "Global file updated with latest build data"
   }
   
   def getCurrentBuildLink() {
      def jenkinsUrl = env.JENKINS_URL
      def jobName = java.net.URLEncoder.encode(env.JOB_NAME, "UTF-8").replace("+", "%20")
      def buildNumber = env.BUILD_NUMBER
      return "${jenkinsUrl}job/${jobName}/${buildNumber}/"
   }
   

   ```
2. Run the pipeline



## Issues - Level7-B
1. Pipeline was failing due to approval issue. Static method & the getter methods of the class were not accessible.
Solution: Whenever there is approval error go to the Dashboard -> Manage Jenkins -> In-process Script Approval -> Approve the method.

2. Got java.io.NotSerializableException: io.jenkins.plugins.sample.global_configuration.OnboardingPluginGlobalConfiguration
solution: https://stackoverflow.com/questions/37388837/java-io-notserializableexception-hudson-model-freestyleproject
Basically, since pipeline was getting the paused for input, it was trying to serialize the object which was not serializable. Hence, before I reference I set the variable to null.

### Level9
This thread https://community.jenkins.io/t/fetch-real-time-pipeline-status-data-from-jenkins has more details like there are various ways to fetch the build details

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

