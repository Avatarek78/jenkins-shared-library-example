#!/usr/bin/env groovy

import java.util.concurrent.TimeUnit

/**
 * Sends email with all information about build to "developers@level.systems".
 * @param status string that contains sentence which is suitable into middle of the sentence "Build ${status} in Jenkins:".
 * @return
 */
void emaiBuildDev(status) {
    final String lastFailedBefore = getLastSuccessfulBefore()
    final String lastSucessBefore = getLastFailedBefore()
    final String buildLog = getBuildLog()
    final def subject = "Build ${status} in Jenkins: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    final def bodyText = "Build Number: ${env.BUILD_NUMBER} <br>" +
            "URL of build: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a> <br>" +
            "Last failed before: ${lastFailedBefore} <br>" +
            "Last sucess before: ${lastSucessBefore} <br>" +
            "Changes:  ${currentBuild.changeSets} <br>" +
            "Build log: <br>${buildLog}"
    email(subject, bodyText)
}

/**
 * Method for simplifying of email sending via Jenkins "mail" plugin.
 * mail doc: https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps/#mail-mail
 *           "E-mail Notification" in Jenkins must be well configured at https://ci.positrex.eu/manage/configure to get it work.
 * @param subject
 * @param bodyText
 * @param to optional recipient address, defaults to 'developers@level.systems'.
 */
void email(def subject, def bodyText, String to = 'developers@level.systems') {
    mail subject: "${subject}", body: "${bodyText}", charset: 'UTF-8', mimeType: 'text/html', to: "${to}"
}

/**
 * Method for simplifying of posting messages into #jenkins Slack channel.
 * slack doc: https://www.jenkins.io/doc/pipeline/steps/slack/
 * @param msg
 * @param color "good", "warning", "danger", or any hex color code (eg. #439FE0)
 */
void slack(msg, color) {
    slackSend(channel: "#jenkins", color: "${color}", message: "${msg}")
}

/**
 * Build the log suitable as content of html email, with useful links to files from build (e.g. screenshots form failed tests for quick recongnitioning of the problem).
 */
String getBuildLog() {
    def buildLogLines = currentBuild.rawBuild.getLog(500)
    // Replace links to local files via links to https://ci.positrex.eu/...
    // Beware that links to workspaces is separated by build numbers in pipeline jobs!!! So link to workspace can be e.g.:
    // "https://ci.positrex.eu/job/ptx%20selenium%20test%20(pipeline)/38/execution/node/3/ws/"
    // where 38 is the number of the build and 3 is the number of the number of pipeline step in Jenkinsfile.
    // Funny is, that in reality all links currently leads to same directory on server
    // "/var/lib/jenkins/workspace/ptx selenium test (pipeline)/.." :-D But this convention must be kept for building URL for links.
    def linkSubstitution = "<a href='https://ci.positrex.eu/job/\$1/${env.BUILD_NUMBER}/execution/node/3/ws/\$2' target='_blank'>https://ci.positrex.eu/job/\$1/${env.BUILD_NUMBER}/execution/node/3/ws/\$2</a>"
    def modifiedLogLines = buildLogLines.collect { line ->
        line.replaceAll('file:\\/var\\/lib\\/jenkins\\/workspace\\/([^\\/]+)\\/(.*)', linkSubstitution)
    }
    return modifiedLogLines.join('<br>')
}

/**
 * Gets human readable string containing time from last failed build. Returns "-" if no previous failed build is present.
 */
String getLastFailedBefore() {
    if (currentBuild.previousFailedBuild != null) {
        return convertMillisToString(currentBuild.timeInMillis - currentBuild.previousFailedBuild.timeInMillis)
    }
    return "-"
}

/**
 * Gets human readable string containing time from last successful build. Returns "-" if no previous successful build is present.
 */
String getLastSuccessfulBefore() {
    if (currentBuild.previousSuccessfulBuild != null) {
        return convertMillisToString(currentBuild.timeInMillis - currentBuild.previousSuccessfulBuild.timeInMillis)
    }
    return "-"
}

/**
 * Method that convert milliseconds to human readable string such as "2 days 5 hours 33 minutes".
 * @param millis time in milliseconds
 * @return
 */
@NonCPS
String convertMillisToString(long millis) {
    long days = TimeUnit.MILLISECONDS.toDays(millis)
    long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
    long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    StringBuilder result = new StringBuilder()

    if (days > 0) {
        result.append(days).append(" day").append(days > 1 ? "s" : "").append(" ")
    }

    if (hours > 0) {
        result.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ")
    }

    if (minutes > 0) {
        result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ")
    }

    if (seconds > 0) {
        result.append(seconds).append(" second").append(seconds > 1 ? "s" : "")
    }

    if (result.length() == 0) {
        result.append(millis).append(" millisecond").append(seconds > 1 ? "s" : "")
    }

    return result.toString().trim()
}