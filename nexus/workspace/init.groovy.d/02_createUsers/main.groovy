#!/usr/bin/env groovy
// id : firstname : lastname : email : active : password : role1 , role2 , roleN
def scriptDir = args.split("&")[0]
def USER_CFG_FILE = scriptDir + "/users.cfg"
def userFile = new File(USER_CFG_FILE)
log.info("----> Starting creating users")
if (!userFile.exists()) {
  throw new IllegalArgumentException("${USER_CFG_FILE} not exists, so stop users creation")
}

userFile.eachLine { line ->
  line = line.trim()
  if (line.startsWith("#")) {
    log.info("----> Skipping comment")
    return 
  }
  def elements = line.split(":")
  def length = elements.length
  if (length < 6) {
    throw new IllegalArgumentException("!!!!> FAILURE : 7 input parameters (id : firstname : lastname : email : password : role1 , role2 , roleN) are needed, but only ${length}, they are ${line}")
  }
  def id = elements[0].trim()
  def firstname = elements[1].trim()
  def lastname = elements[2].trim()
  def mail = elements[3].trim()
  def password = elements[4].trim()
  def roles = elements[5].trim().split(",").collect { it -> it.trim()}
  security.addUser(id, firstname, lastname, mail, true, password, roles)
  log.info("--> user '${id}' created with roles: ${roles}")
}
log.info("--> Script executed: create developer users")
