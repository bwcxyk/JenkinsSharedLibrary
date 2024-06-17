package org.devops

def call(){
  checkout scm
}

def checkoutWithDepth(){
    checkout([
        $class: 'GitSCM',
        branches: scm.branches,
        doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
        extensions: [
            // 设置 clone 参数
            [$class: 'CloneOption', depth: 1, noTags: false],
            // 添加 GitLFSPull 插件
            [$class: 'GitLFSPull']
        ],
        userRemoteConfigs: scm.userRemoteConfigs
    ])
}
