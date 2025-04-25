#!/bin/bash
set -ex

# 定义全局变量（根据实际需求调整）
ALI_OSS_ENDPOINT="oss-cn-shanghai.aliyuncs.com"
ALI_OSS_AK="YOUR_AK"
ALI_OSS_SK="YOUR_SK"
TENCENT_SID="SecretId"
TENCENT_SKEY="SecretKey"
KEYSTORE_PASS="android"

# ------------------------- 功能函数定义 -------------------------

# 腾讯乐固加固
tencent_shield() {
    echo "---------------- tencent加固开始 ----------------"
    java -Dfile.encoding=utf-8 \
      -jar ms-shield.jar \
      -sid "$TENCENT_SID" \
      -skey "$TENCENT_SKEY" \
      -uploadPath "${WORKSPACE}/app/build/outputs/apk/release/app-release.apk" \
      -downloadPath "${WORKSPACE}/app/build/outputs/apk/release/"
    echo "---------------- tencent加固结束 ----------------"
}

# 使用jarsigner签名
sign_with_jarsigner() {
    echo "---------------- 开始签名(jarsigner) ----------------"
    jarsigner -verbose \
      -keystore "${WORKSPACE}/app/app.jks" \
      -storepass "$KEYSTORE_PASS" \
      -keypass "$KEYSTORE_PASS" \
      -signedjar "${WORKSPACE}/app/build/outputs/apk/release/app-release_legu_signed.apk" \
      "${WORKSPACE}/app/build/outputs/apk/release/app-release_legu.apk" \
      key0 \
      -tsa http://sha256timestamp.ws.symantec.com/sha256/timestamp
}

# 使用apksigner签名 (Android官方推荐)
sign_with_apksigner() {
    echo "---------------- 开始签名(apksigner) ----------------"
    sh /var/jenkins_home/android/sdk/build-tools/28.0.3/apksigner sign \
      --ks "${WORKSPACE}/app/app.jks" \
      --ks-key-alias key0 \
      --ks-pass pass:"$KEYSTORE_PASS" \
      --key-pass pass:"$KEYSTORE_PASS" \
      --out "${WORKSPACE}/app/build/outputs/apk/release/app-release_legu_signed.apk" \
      "${WORKSPACE}/app/build/outputs/apk/release/app-release_legu.apk"
}

# 360加固
shield_360() {
    echo "---------------- 360加固 ----------------"
    java -jar Android/360firm_mac/jiagu/jiagu.jar \
      -jiagu "${WORKSPACE}/app/build/outputs/apk/upload.apk" \
      "${WORKSPACE}/app/jiagu" \
      -autosign \
      -pkgparam "${WORKSPACE}/app/channels.txt"
}

# 上传到阿里云OSS
upload_to_oss() {
    echo "---------------- 上传到OSS ----------------"
    local BUILD_TIME=$(date "+%Y%m%d_%H_%M_%S")
    local UPLOAD_MONTH=$(date "+%Y%m%d")
    
    # 压缩加固后的文件
    cd "${WORKSPACE}/app" && tar -zcvf jiaguAPK.gz jiagu
    
    # 配置并上传
    cd "${WORKSPACE}/ossuploadconfig"
    ossutil config -e "$ALI_OSS_ENDPOINT" -i "$ALI_OSS_AK" -k "$ALI_OSS_SK"
    ossutil cp "${WORKSPACE}/app/jiaguAPK.gz" \
      "oss://bucket/app/android/jaiguapk/${UPLOAD_MONTH}/jiaguAPK_${BUILD_TIME}.gz"
    
    local DOWNLOAD_URL="http://bucket.oss-cn-shanghai.aliyuncs.com/app/android/jaiguapk/${UPLOAD_MONTH}/jiaguAPK_${BUILD_TIME}.gz"
    echo "下载地址：${DOWNLOAD_URL}"
}

# ------------------------- 主逻辑 -------------------------
if [ "$SIGN_SHIELD" = true ]; then
    # 默认执行腾讯加固+jarsigner签名
    tencent_shield
    sign_with_jarsigner
    
    # 如果需要其他功能，可以取消下方注释：
    # sign_with_apksigner   # 使用apksigner签名
    # shield_360           # 360加固
    # upload_to_oss        # 上传到OSS
fi
