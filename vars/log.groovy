/**
@Library('jenkinslibrary@master') _

pipeline {
    agent none
    stage ('Example') {
        steps {
             script {
                 // log是文件名 info是文件中定义的方法
                 log.info 'Starting'
                 log.warning 'Nothing to do!'
             }
        }
    }
}
**/

def info(message) {
    echo "INFO: ${message}"
}

def warning(message) {
    echo "WARNING: ${message}"
}
