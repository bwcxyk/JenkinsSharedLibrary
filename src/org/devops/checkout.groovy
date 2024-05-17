package org.devops

def call(){
  checkout scm
}

def checkoutWithDepth {
    checkout([
        $class: 'GitSCM',
        branches: scm.branches,
        doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
        extensions: [[$class: 'CloneOption', depth: 1, noTags: false]],
        userRemoteConfigs: scm.userRemoteConfigs
    ])
}
