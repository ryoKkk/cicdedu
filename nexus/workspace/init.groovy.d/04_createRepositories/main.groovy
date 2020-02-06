#!/usr/bin/env groovy
// id : type(PROXY, HOSTED or GROUP) : routing rule : url (if type is PROXY) : members (if type is GROUP)
import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.common.entity.DetachedEntityId
import org.sonatype.nexus.repository.maven.LayoutPolicy
import org.sonatype.nexus.repository.maven.VersionPolicy
import org.sonatype.nexus.repository.routing.RoutingMode
import org.sonatype.nexus.repository.routing.RoutingRule
import org.sonatype.nexus.repository.storage.WritePolicy

class Global {
  static int POTENTIAL_PROTOCOL_INDEX = 3
  static int POTENTIAL_PART_URL_INDEX = 4
  static String HOSTED = "HOSTED"
  static String PROXY = "PROXY"
  static String GROUP = "GROUP"
  static String MAVEN = "MAVEN"
  static String NPM = "NPM"
}

def scriptDir = args.split("&")[0]
def REPO_CFG_FILE = scriptDir + "/repos.cfg"
def reposFile = new File(REPO_CFG_FILE)
reposFile.eachLine { line -> 
  line = line.trim()
  if (line.startsWith("#")) {
    log.info("----> Skipping comment")
    return
  }
  elements = line.split(":").collect {it -> it.trim()}
  elements = peudoEscape(elements)
  def id = elements.get(0)
  def type = elements.get(1)
  def repo = elements.get(2)
  log.info("----> Will create ${repo} repository based on config : $elements")
  if (Global.MAVEN.equals(repo)) {
    // maven repository
    if (Global.HOSTED.equals(type)) {
      repository.createMavenHosted(
        id,
        BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
        false,                  // to allow nexus to download non-jar files
        VersionPolicy.RELEASE,
        WritePolicy.ALLOW,      // to allow nexus to download non-jar files
        LayoutPolicy.STRICT
      )
      log.info("----> Maven hosted repository ($id) created")
    } else if (Global.PROXY.equals(type)) {
      def url = elements.get(3)
      repository.createMavenProxy(
        id,
        url,
        BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
        false,
        VersionPolicy.RELEASE,
        LayoutPolicy.STRICT
      )
      log.info("----> Maven proxy repository ($id) created")
    } else if (Global.GROUP.equals(type)) {
      def groups = elements.get(4).split(",").collect { it -> it.trim() }
      repository.createMavenGroup(
        id,
        groups,
        BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
      )
      log.info("----> Maven group repository ($id) created")
    } else {
      log.info("!!!!> FAILURE : $type is illegal, choose one of ${Global.HOSTED}, ${Global.GROUP}, ${Global.PROXY}")
      return 
    }
  } else if (Global.NPM.equals(repo)) {
    // npm repository
    if (Global.HOSTED.equals(type)) {
      repository.createNpmHosted(
        id,
        BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
        false,
        WritePolicy.ALLOW
      )
      log.info("----> Npm hosted repository ($id) created")
    } else if (Global.PROXY.equals(type)) {
      def url = elements.get(3)
      repository.createNpmProxy(
        id,
        url,
        BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
        false
      )
      log.info("----> Npm proxy repository ($id) created")
    } else if (Global.GROUP.equals(type)) {
      def groups = elements.get(4).split(",").collect { it -> it.trim() }
      repository.createNpmGroup(
        id,
        groups,
        BlobStoreManager.DEFAULT_BLOBSTORE_NAME
      )
      log.info("----> Npm group repository ($id) created")
    } else {
      log.info("!!!!> FAILURE : $type is illegal, choose one of ${Global.HOSTED}, ${Global.GROUP}, ${Global.PROXY}")
      return 
    }
  }
}


/** BELOW ARE HELPER FUNCTIIONS TO DO COLON ESCAPING */
def peudoEscape(list) {
  def newList = []
  def isUrlContained = containsUrl(list)
  def newUrl = ""
  for (def i = 0; i < list.size(); i ++) {
    def ele = list[i]
    if (isUrlContained) {
      if (i == Global.POTENTIAL_PROTOCOL_INDEX) {
        // remove slash from protocol
        newUrl += ele.substring(0, ele.length() - 1)
      } else if (isUrlContained && i == Global.POTENTIAL_PART_URL_INDEX) {
        // adding colon and part url
        newUrl += ":"
        newUrl += ele
        newList.add(newUrl)
      } else {
        newList.add(ele)
      }
    } else {
      newList.add(ele)
    }
  }
  return newList
}

def containsUrl(list) {
  if (list.size() < 4) {
    return false
  }
  def potentialProtocol = list.get(Global.POTENTIAL_PROTOCOL_INDEX)
  def potentialPartUrl = list[Global.POTENTIAL_PART_URL_INDEX]
  return isProtocol(potentialProtocol) && isPartUrl(potentialPartUrl)
}

def isProtocol(string) {
  return string.equalsIgnoreCase("https\\") || string.equalsIgnoreCase("http\\")
}

def isPartUrl(string) {
  return string.startsWith("//")
}
