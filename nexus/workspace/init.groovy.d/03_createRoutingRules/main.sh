#/bin/bash
# name : description : mode(ALLOW or BLOCK) : matcher1 , macher2 , matcherN
SCRIPT_PATH="$(realpath $0)"
SCRIPT_DIR="$(dirname ${SCRIPT_PATH})"
SCRIPT_FOLDER_NAME="$(basename ${SCRIPT_DIR})"
ROUTING_CFG_FILE="${SCRIPT_DIR}/routings.cfg"
HOST="$1"
USERNAME="$2"
PASSWORD="$3"

trim() {
  result=$(echo $1 | sed 's/^\s+//g' | sed 's/\s+$//g')
  echo $result
}

echo "----> Starting creating routing rules"

while read -r line || [ -n "$line" ]; do
  if [[ "$line" =~ ^[[:space:]]*\#+ ]]; then
    echo "----> Skipping comment"
    continue
  fi
  IFS=':' read -a elements <<< "$line"
  size=${#elements[@]}
  if [[ $size -lt 4 ]]; then
    echo "!!!!> FAILURE, 4 input parameters needed, but only $size, they are \"$line\", stop skip this line"
    continue
  fi
  name="$(trim "${elements[0]}")"
  description="$(trim "${elements[1]}")"
  mode="$(trim "${elements[2]}")"
  if [[ "$mode" != "ALLOW" ]] && [[ "$mode" != "BLOCK" ]]; then
    echo "----> \"$mode\" is illegal, please use either ALLOW or BLOCK"
  fi
  rawMatchers="$(trim "${elements[3]}")"
  matchers="$(echo $rawMatchers | sed -E 's/([^,[:space:]]+)/"\1"/g')"
  parameters="{\"name\":\"$name\", \"description\":\"$description\", \"mode\": \"$mode\", \"matchers\": [$matchers]}"
  echo "----> Request parameters : $parameters"
  response=$(curl -v -u $USERNAME:$PASSWORD -H "Content-Type: application/json" "$HOST/service/rest/beta/routing-rules"  -w '\ncode : %{response_code}' -d "$parameters")

  if [[ "$response" =~ 2[0-9]{2}$ ]]; then
    printf "\n+--------------------Success----------------------+\n"
    printf "[ response : ${response}, ${SCRIPT_FOLDER_NAME} ]\n"
    printf "+-------------------------------------------------+\n"
  else
    printf "\n+--------------------Failure!!!-------------------+\n"
    printf "[ code: ${response}, ${SCRIPT_FOLDER_NAME} ]\n"
    printf "+-------------------------------------------------+\n"
  fi

done < ${ROUTING_CFG_FILE}
