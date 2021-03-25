#!/bin/bash
#食用方法:sh push2.sh ${REGISTRY_URL} ${TAG_VERSION}


REGISTRY_URL="192.168.0.2" #镜像仓库url
NAME_SPACE="public" #命名空间
TAG_NAME="api"
TAG_NAME2="api-job"
TAG_NAME3="api-db-update"
TAG_VERSION="v1.0" #镜像的版本

if [ "$1" != "" ];
    then
    REGISTRY_URL="$1"
fi

if [ "$2" != "" ];
    then
    TAG_VERSION="$2"
fi

# build_push_tag
set -e
# push api
cd $WORKSPACE/api-server
docker build -t ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION} .
docker push ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION}
docker rmi ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION}
# push job
cd $WORKSPACE/api-job
docker build -t ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME2}:${TAG_VERSION} .
docker push ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME2}:${TAG_VERSION}
docker rmi ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME2}:${TAG_VERSION}
# push db-update
cd $WORKSPACE/api-db-update
docker build -t ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME3}:${TAG_VERSION} .
docker push ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME3}:${TAG_VERSION}
docker rmi ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME3}:${TAG_VERSION}
