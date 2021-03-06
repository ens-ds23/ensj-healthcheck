#!/bin/sh
# Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Usage note: If you use -config FILE you must fully specify the path to FILE otherwise the program won't find the file.
# e.g. ~/dev/ensj-healthcheck/run-healthcheck.sh  -config `pwd`/db.properties -d my_database SOME_TEST

home=`dirname $0`
. $home/setup.sh
jar
classpath

$JAVA_HOME/bin/java -Duser.dir=$home -Xmx1500m org.ensembl.healthcheck.TextTestRunner $*
