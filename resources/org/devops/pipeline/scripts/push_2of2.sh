#!/bin/bash
# 使用方法: push2.sh
# REGISTRY_URL、TAG_VERSION 通过父脚本执行时参数传入

 # 镜像仓库命名空间
NAME_SPACE="public"

# 镜像名
# 有几个子项目，添加几个，下面 docker 命令也要同样添加
TAG_NAME="test"
TAG_NAME2=""

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
# push test
cd $WORKSPACE
docker build -t ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION} .
docker push ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION}
docker rmi ${REGISTRY_URL}/${NAME_SPACE}/${TAG_NAME}:${TAG_VERSION}
