# Jenkins shared library example
Testing of the common library for Jenkinsfile

## Setup instructions

1. In Jenkins, go to Manage Jenkins &rarr; Configure System. Under _Global Pipeline Libraries_, add a library with the following settings:

    - Name: `jenkins-shared-library-example`
    - Default version: Specify a Git reference (branch or commit SHA), e.g. `main`
    - Retrieval method: _Modern SCM_
    - Select the _Git_ type
    - Project repository: `https://github.com/Avatarek78/jenkins-shared-library-example.git`
    - Credentials: (leave blank)

2. Then create a Jenkins job with the following pipeline (note that the space + underscore is not a typo, but necessary syntax):

    ```
    @Library('jenkins-shared-library-example') _

    stage('Demo') {   
      echo "jenkinsUtils.convertMillisToString(10000) = " jenkinsUtils.convertMillisToString(10000);
    }
    ```
