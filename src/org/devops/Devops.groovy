import Docker
import deployment
//devops驱动对象
def docker(String repo, String tag, String credentialsId, String Dockerfile="Dockerfile",String path="."){
    return new Docker().docker(repo, tag, credentialsId)
}

//部署deploy对象
def deploy(String resourcePath, Boolean wtach=true, String workloadFilePath){
    echo "我开始调用deploy对象+++++++++++++++++++++++++++++++++++"
    return new deployment().getObject(resourcePath,wtach,workloadFilePath)
}