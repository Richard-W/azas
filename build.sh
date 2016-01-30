#!/bin/bash

which wget 2> /dev/null > /dev/null
wget_found=$?
if [ ${wget_found} != 0 ]; then
    echo "Failure: Application ‘wget“ not found"
    exit 1
fi

dir=$(dirname $0)
builddir=${dir}/.build
sbt_version=`cat ${dir}/project/build.properties | grep sbt.version | cut -d'=' -f2`
sbt_archive_path=${builddir}/sbt-${sbt_version}.tar.gz

if [ ! -d ${builddir} ]; then
    mkdir ${dir}/.build
fi

if [ ! -f ${sbt_archive_path} ]; then
    wget -O ${sbt_archive_path} https://dl.bintray.com/sbt/native-packages/sbt/${sbt_version}/sbt-${sbt_version}.tgz
fi

rm -rf ${builddir}/sbt
tar --directory ${builddir} -xzf ${sbt_archive_path}

# Remove old artifacts
rm -f ${dir}/target/scala-2.11/azas-assembly*

cd ${dir}
${builddir}/sbt/bin/sbt assembly
cd -

artifact_name=`ls ${dir}/target/scala-2.11/ | grep azas-assembly`
artifact_path="${dir}/target/scala-2.11/${artifact_name}"
artifact_destname=`echo ${artifact_name} | sed -e 's/\-assembly//'`
cp ${artifact_path} ${dir}/${artifact_destname}
