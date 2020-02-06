#!/bin/bash
BASEDIR=$(dirname "$0")
source ${BASEDIR}/env.sh

# fail if anything errors
set -e
# fail if a function call is missing an argument
set -u

# add a script to the repository manager and run it
function addAndRunScript {
  file=$1
  host=$2
  adminUser=$3
  initialPassword=$4
  newPassword=$5
  scriptDir=$(dirname $file)
  name=$(basename $scriptDir)
  # using grape config that points to local Maven repo and Central Repository , default grape config fails on some downloads although artifacts are in Central
  # change the grapeConfig file to point to your repository manager, if you are already running one in your organization
  groovy -Dgroovy.grape.report.downloads=true -Dgrape.config=grapeConfig.xml $BASEDIR/addUpdateScript.groovy -u "$adminUser" -p "$initialPassword" -n "$name" -f "$file" -h "$host"
  response=$(curl -v -u $adminUser:$initialPassword -H "Content-Type: text/plain" "$host/service/rest/v1/script/$name/run" -d "$scriptDir" -d "$adminUser" -d "$newPassword" -w '\ncode : %{response_code}')
  if [[ "$response" =~ 2[0-9]{2}$ ]]; then
    printf "\n+--------------------Success----------------------+\n"
    printf "[ response : ${response}, $name ]\n"
    printf "+-------------------------------------------------+\n"
  else
    printf "\n+--------------------Failure!!!-------------------+\n"
    printf "[ code: ${response}, $name ]\n"
    printf "+-------------------------------------------------+\n"
  fi
}

echo "--> Provisioning scripts starting, it will take several minutes, please wait." 
echo "--> Publishing and executing on $DEFAULT_HOST"
# waiting for initial passwor file creation
times=0
while [[ ! -f "${INITIAL_PASSWORD_FILE}" ]]; do
  times=$((times+1))
  echo "--> waiting for inital password to be created ($times)"
  sleep 5s
  if [[ $times -eq 24 ]]; then
    echo "--> initial password is not created in 120 seconds, so abort it"
    exit 1  
  fi
done
echo "--> As initial password file created, provisioning scripts will be executed."

# initialPassword=$DEFAULT_ADMIN_PASSWORD
initialPassword=$(cat ${INITIAL_PASSWORD_FILE})
# execute both groovy and shell scripts to provision
for dir in $(ls $BASEDIR); do
  scriptDir=$(realpath $BASEDIR/$dir)
  echo "----> Scanning ${scriptDir}"
  if [[ -d $scriptDir ]] && [[ "$dir" =~ ^[0-9]{2} ]]; then
    echo "----> will execute scripts under ${scriptDir}"
    for file in $(ls $scriptDir); do
      if [[ "$file" =~ \.sh$ ]]; then
        # passing directory to scripts so that they can get configuration files themselves
        # bash $scriptDir/$file $scriptDir $DEFAULT_HOST $DEFAULT_ADMIN_USERNAME $initialPassword
        bash $scriptDir/$file $DEFAULT_HOST $DEFAULT_ADMIN_USERNAME $initialPassword
      elif [[ "$file" =~ \.groovy$ ]]; then
        # addAndRunScript $scriptDir $DEFAULT_HOST $dir "$scriptDir/$file" $DEFAULT_ADMIN_USERNAME $initialPassword $DEFAULT_ADMIN_PASSWORD
        addAndRunScript "$scriptDir/$file" $DEFAULT_HOST $DEFAULT_ADMIN_USERNAME $initialPassword $DEFAULT_ADMIN_PASSWORD
      fi
    done
  else
    echo "----> ${scriptDir} is not a target, skip it"
  fi
done

# post scripts
echo "--> Enabling anonymous access through selenium"
# groovy "${BASEDIR}/post/seleniumWorker.groovy" "$(realpath ${BASEDIR})/post" $DEFAULT_HOST $DEFAULT_ADMIN_USERNAME $DEFAULT_ADMIN_PASSWORD
echo "--> Provisioning scripts completed"