#!/bin/bash
# 使用方法 sh push_1of2.sh "$registry" "$TAG_VERSION"

# 设置默认的TAG_VERSION为当前日期时间
DATE=$(date +"%Y%m%d%H%M%S")
TAG_VERSION=${DATE} #镜像的版本

# 如果提供了第二个参数，则使用该参数作为TAG_VERSION
if [ "$2" != "" ];
    then
    TAG_VERSION="$2"
fi

set -e

# build_push_tag
push_tag()
{
    sh $WORKSPACE/push.sh ${REGISTRY_URL} ${TAG_VERSION}
    echo -e "\e[1;34mBuild finished!\e[0m"
    echo -e "\e[1;32mDocker image tag: ${TAG_VERSION}\e[0m"
}

if [ "$1" = "local" ] || [ -z "$1" ]; then
    # login
    USERNAME="admin"
    PASSWORD="Yuanfu1211"
    REGISTRY_URL="192.168.1.60"
    echo "${PASSWORD}" | docker login --username=${USERNAME} ${REGISTRY_URL} --password-stdin
    push_tag
elif [ "$1" = "aliyun" ]; then
    USERNAME="远孚集团"
    PASSWORD="wzygymtijus9"
    REGISTRY_URL="registry.cn-shanghai.aliyuncs.com"
    echo "${PASSWORD}" | docker login --username=${USERNAME} ${REGISTRY_URL} --password-stdin
    push_tag
elif [ "$1" = "huaweicloud" ]; then
    USERNAME="cn-east-2@VMKGNWR86FUNMDMKNPKJ"
    PASSWORD="eefd5bebe0e349a5ed2ffb22bf0a488ec8dba5d2a3075272f8f0d79e0f09616e"
    REGISTRY_URL="swr.cn-east-2.myhuaweicloud.com"
    echo "${PASSWORD}" | docker login --username=${USERNAME} ${REGISTRY_URL} --password-stdin
    push_tag
else
    echo '本地仓库:push.sh local 1.0'
    echo '阿里云仓库:push.sh aliyun 1.0'
    echo '华为云仓库:push.sh huaweicloud 1.0'
fi
