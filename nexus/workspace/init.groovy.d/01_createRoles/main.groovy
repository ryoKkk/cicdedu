#!/usr/bin/env groovy
// TODO: developer's privilege, currently same as anonymous user
def scriptDir = args.split("&")[0]
def ROLE_CFG_FILE = scriptDir + "/roles.cfg"
def roleFile = new File(ROLE_CFG_FILE)
log.info("----> Starting creating roles")
if (!roleFile.exists()) {
  throw new IllegalArgumentException("${ROLE_CFG_FILE} not exists, so stop roles creation")
}

roleFile.eachLine {line ->
  line = line.trim()
  if (line.startsWith("#")) {
    log.info("----> Skipping comment")
    return 
  }
  def elements = line.split(":")
  def length = elements.length
  if (length < 4) {
    throw new IllegalArgumentException("!!!!> FAILURE : 4 input parameters (id : name : description : privilege1,privilege2,privilegeN) are needed, but only ${length}, they are ${line}")
  }
  def id = elements[0].trim()
  def name = elements[1].trim()
  def description = elements[2].trim()
  def privileges = elements[3].trim().split(",").collect { it -> it.trim() }
  def roles = []
  if (length > 4) {
    roles = elements[4].trim().split(",").collect { it -> it.trim() }
  }
  log.info("----> Creating role: ${id}")
  security.addRole(id, name, description, privileges, roles)
  log.info("----> Role(${id}) created")
}
