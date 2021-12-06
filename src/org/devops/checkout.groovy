package org.devops

def call(){
  checkout scm
}

def CheckOut(){
    checkout([
        $class: 'GitSCM',
        branches: scm.branches,
        doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
        // extensions: [[$class: 'CloneOption', shallow: true, noTags: false, reference: '', timeout: null, depth: 1, honorRefspec: false]],
        extensions: [[$class: 'CloneOption', depth: 1]],
        userRemoteConfigs: scm.userRemoteConfigs
    ])
}
