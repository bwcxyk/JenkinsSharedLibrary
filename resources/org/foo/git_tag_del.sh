#!/bin/bash
# 删除旧的tag，保留最近三个月。
# author YaoKun
set -ex

month=$(date +%Y%m)
month1=$(date -d "${month}01 last month" +%Y%m)
month2=$(date -d "${month1}01 last month" +%Y%m)

# 清理历史tag
echo "保留以下月份tag:${month},${month1},${month2}"

#git show-ref --tag | grep -v ${month} | grep -v ${month1} | grep -v ${month2} | awk '{print ":" $2}' > tmp.txt

git show-ref --tag | \
grep -v ${month} |   \
grep -v ${month1} |  \
grep -v ${month2} |  \
awk '{print ":" $2}' > tmp.txt

file=tmp.txt

for i in `cat ${file}`
do
git push origin $i;
done;
rm -f tmp.txt

# 同步远程tag到本地
#git fetch --prune --tags
