#!/bin/bash

DATE=$(date +"%Y%m%d%H%M%S")
REGISTRY_URL="registry.cn-shanghai.aliyuncs.com" #镜像仓库url
NAME_SPACE="yuanfu-tms" #命名空间
TAG_NAME="oms-api"
TAG_NAME2="oms-web"
TAG_VERSION=${DATE} #镜像的版本

if [ "$2" != "" ];
    then
    TAG_VERSION="$2"
fi

# build_push_tag
push_tag()
{
    set -e
    # push api
    cd $WORKSPACE/oms-api
    docker build -t ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION} .
    docker push ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION}
    docker rmi ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION}
    # push web
    cd $WORKSPACE/oms-web
    docker build -t ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME2}:${TAG_VERSION} .
    docker push ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME2}:${TAG_VERSION}
    docker rmi ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME2}:${TAG_VERSION}
}

if [ "$1" = "dev" ];
    then
    # login
    REGISTRY_URL="192.168.0.2"
    NAME_SPACE="yuanfu-tms"
    docker login --username=admin ${REGISTRY_URL} --password=Yuanfu1211
    push_tag
    elif [ "$1" = "test" ];
    then
    REGISTRY_URL="192.168.0.2"
    NAME_SPACE="yuanfu-tms"
    docker login --username=admin ${REGISTRY_URL} --password=Yuanfu1211
    push_tag
    elif [ "$1" = "prod" ];
    then
    REGISTRY_URL="registry.cn-shanghai.aliyuncs.com"
    NAME_SPACE="yuanfu-tms"
    docker login --username=远孚集团 ${REGISTRY_URL} --password=wzygymtijus9
    push_tag
    else
    echo '测试环境参考命令:push.sh dev 1.0'
    echo 'UAT环境参考命令:push.sh test 1.0'
    echo '生产环境参考命令:push.sh prod 1.0'
fi