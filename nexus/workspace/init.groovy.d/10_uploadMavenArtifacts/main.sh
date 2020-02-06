#!/bin/bash
# read index.cfg file then upload corresponding jar files onto nexus

BASEDIR="$(dirname $(realpath $0))"
HOST=$1
USER=$2
PASS=$3
M2_PKGS_DIR="$BASEDIR/packages"
CFG_FILE="${BASEDIR}/index.cfg"


trim() {
  result=$(echo $1 | sed 's/^\s+//g' | sed 's/\s+$//g')
  echo $result
}

deployToNexus() {
  group_id=$1
  artifact_id=$2
  version=$3
  file=$4
  repository=$5
  result=$(curl -v "${HOST}/service/rest/v1/components?repository=$repository" \
                    -w '%{response_code}' \
                    -H "accept: application/json" \
                    -H "Content-Type: multipart/form-data" \
                    -u $USER:$PASS \
                    -F "maven2.generate-pom=true" \
                    -F "maven2.groupId=${group_id}" \
                    -F "maven2.artifactId=${artifact_id}" \
                    -F "maven2.packaging=jar" \
                    -F "version=${version}" \
                    -F "maven2.asset1=@$file;type=application/x-java/archive" \
                    -F "maven2.asset1.extension=jar")
  if [[ "$result" =~ ^2[0-9]+$ ]]; then
    echo "+----------------Success-------------------+"
    echo "+ code: ${result}, $file +"
    echo "+------------------------------------------+"
    return 0
  else
    echo "+----------------Failure!!!----------------+"
    echo "+ code: ${result}, $file +"
    echo "+------------------------------------------+"
    return 1
  fi
}

count=0
while read -r line || [ -n "$line" ]; do
  # skip comment
  if [[ $line = \#* ]]; then
    continue
  fi
  # TODO: adding tab
  IFS=':' read -a elements <<< "$line"
  # file_name="$(cut -d' ' -f2 <<< "$line")"
  file_name="$(trim ${elements[1]})"
  repository="$(trim ${elements[2]})"
  file="${M2_PKGS_DIR}/${file_name}"
  if [[ ! -f "$file" ]]; then
    echo "WARNING: skip $file as it does NOT exist, make sure space is used to separate maven GAV info."
    continue
  fi

  # extract GAV info
  # gav="$(cut -d' ' -f1 <<< "$line")"
  IFS="," read -a gav <<< ${elements[0]}
  # group_id="$(cut -d':' -f1 <<< "$gav")"
  group_id="$(trim ${gav[0]})"
  # artifact_id="$(cut -d':' -f2 <<< "$gav")"
  artifact_id="$(trim ${gav[1]})"
  # version="$(cut -d':' -f3 <<< "$gav")"
  version="$(trim ${gav[2]})"

  # upload
  echo "will deploy (${file}) with information ("$gav") to nexus3"
  if deployToNexus ${group_id} ${artifact_id} ${version} ${file} ${repository}; then
    ((count++))
  fi
done < ${CFG_FILE}
echo "$count artifacts uploaded"
