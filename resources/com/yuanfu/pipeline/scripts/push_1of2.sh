#!/bin/bash
#食用方法 sh push_1of2.sh ${REGISTRY_URL} ${TAG_VERSION}

if [ "$2" != "" ];
    then
    TAG_VERSION="$2"
fi

# build_push_tag
push_tag()
{
    sh $WORKSPACE/push.sh ${REGISTRY_URL} ${TAG_VERSION}
}

if [ "$1" = "local" ];
    then
    # login
    USERNAME=""
    PASSWORD=""
    REGISTRY_URL=""
    echo "${PASSWORD}" | docker login --username=${USERNAME} ${REGISTRY_URL} --password-stdin
    push_tag
    elif [ "$1" = "aliyun" ];
    then
    USERNAME=""
    PASSWORD=""
    REGISTRY_URL="registry.cn-shanghai.aliyuncs.com"
    echo "${PASSWORD}" | docker login --username=${USERNAME} ${REGISTRY_URL} --password-stdin
    push_tag
    elif [ "$1" = "huaweicloud" ];
    then
    USERNAME=""
    PASSWORD=""
    REGISTRY_URL="swr.cn-east-2.myhuaweicloud.com"
    echo "${PASSWORD}" | docker login --username=${USERNAME} ${REGISTRY_URL} --password-stdin
    push_tag
    else
    echo '本地仓库:push.sh local 1.0'
    echo '阿里云仓库:push.sh aliyun 1.0'
    echo '华为云仓库:push.sh huaweicloud 1.0'
fi
