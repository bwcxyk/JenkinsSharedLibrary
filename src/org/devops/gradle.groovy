package org.devops

def test(additionalArgs = "") {
    def gradleCommand = "gradle test ${additionalArgs}"
    def exitCode = sh(script: gradleCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Gradle test failed with exit code: $exitCode"
    }
}

def build(additionalArgs = "") {
    def gradleCommand = "gradle build ${additionalArgs}"
    def exitCode = sh(script: gradleCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Gradle build failed with exit code: $exitCode"
    }
}
