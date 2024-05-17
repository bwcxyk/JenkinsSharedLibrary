#!/bin/bash

# 脚本说明：
# 本脚本提供Git标签的创建与管理功能，包括：
# 1. 创建新的Git标签，并可选地将其推送到远程仓库。
# 2. 自动删除本地超过6个月的旧标签。

# 函数定义开始

# 创建Git标签的函数
# 参数:
#   NAME: 要创建的标签名称
#   MESSAGE: 标签的描述信息
#   FORCE: 可选参数，默认为空。如果提供，则在标签已存在时强制覆盖
create_git_tag() {
    local NAME="$1"
    local MESSAGE="$2"
    local FORCE="${3:-}"

    if [ -z "$NAME" ]; then
        echo "Error: Tag name must be specified."
        exit 1
    fi

    git tag $FORCE -a "$NAME" -m "$MESSAGE"
    git push origin "$NAME"
    echo "Tag '$NAME' created and pushed to remote."
}

# 删除过期Git标签的函数
# 此函数会查找并删除所有创建时间超过6个月的本地Git标签。
delete_old_git_tags() {
    local HALF_YEAR_AGO=$(date -d "6 months ago" +%Y-%m-%d)
    git tag --sort=-creatordate | awk -v date="$HALF_YEAR_AGO" '$0 < date' | xargs git tag -d
    echo "Old tags have been deleted."
}

# 主体逻辑

ACTION=$1
shift
# 根据传入的ACTION参数执行不同操作
case $ACTION in
    create)
        create_git_tag "$@"
        ;;
    delete)
        delete_old_git_tags
        ;;
    *)
        echo "Usage: $0 [create|delete]"
        echo "  create: Creates a new Git tag with optional force overwrite."
        echo "  delete: Deletes tags that are older than 6 months."
        exit 1
        ;;
esac

echo "Operation completed."