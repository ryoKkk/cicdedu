// set default anonymous access through selenium.
@Grab('org.seleniumhq.selenium:selenium-firefox-driver:3.14.0')
@Grab('org.seleniumhq.selenium:selenium-support:3.14.0')
@Grab('org.seleniumhq.selenium:selenium-api:3.14.0')
@Grab('org.seleniumhq.selenium:selenium-remote-driver:3.14.0')
@Grab('io.github.bonigarcia:webdrivermanager:3.4.0')

import io.github.bonigarcia.wdm.FirefoxDriverManager
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.WebDriver
import org.openqa.selenium.By
import java.util.concurrent.TimeUnit

def sleep(int seconds) {
  TimeUnit.SECONDS.sleep(seconds)
}

/*****************
 anonymous access
 *****************/
def scriptDir = args[0]
def host = args[1]
def admin = args[2]
def newPassword = args[3]
FirefoxDriverManager.getInstance(FirefoxDriver.class).setup()
def firefoxOptions = new FirefoxOptions()
/**
firefoxOptions.addPreference("network.proxy.type", 1)
.addPreference("network.proxy.http", "user:pass@localhost")
.addPreference("network.proxy.http_port", 3128)
*/
firefoxOptions.setHeadless(true)
def driver = new FirefoxDriver(firefoxOptions)
def wait = new FluentWait<WebDriver>(driver)
       .withTimeout(10, TimeUnit.SECONDS)
       .pollingEvery(2, TimeUnit.SECONDS)
driver.get(host)
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-mask")))
// first "Sign in"
def loginButton = driver.findElements(By.xpath("//span[text()='Sign in']"))[0]
loginButton.click()
sleep(1)
// input username, password and click "Sign in" 
driver.findElements(By.xpath("//input[@placeholder='Username']"))[0].sendKeys(admin)
driver.findElements(By.xpath("//input[@placeholder='Password']"))[0].sendKeys(newPassword)
sleep(1)
def signInButton = driver.findElements(By.xpath("//span[text()='Sign in']"))[1]
signInButton.click()
sleep(1)

// 1/3: first "Next" button in "Setup"
def nextButtons = driver.findElements(By.xpath("//span[text()='Next']"))
nextButtons[0].click()
sleep(1)
// 2/3: click "Enable anonymous access" then click last "Next" button.
driver.findElements(By.xpath("//label[text()='Enable anonymous access']"))[0].click();
sleep(1)
nextButtons[1].click()
sleep(1)
// 3/3: click "Finish" to finish
driver.findElements(By.xpath("//span[text()='Finish']"))[0].click()
sleep(2)


/*********************
 routing rules setting
 *********************/
def mappingsFile = new File(scriptDir + "/routing_mappings.cfg")
def lines = mappingsFile.readLines()
println("----> Starting set routing rules")
for (def i = 0; i < lines.size(); i ++) {
  // set one by one
  def line = lines.get(i).trim()
  if (line.startsWith("#")) {
    println("----> Skipping comment")
    continue
  }
  elements = line.split(":")
  repo = elements[0].trim()
  routing = elements[1].trim()

  // go to repository configuration page
  def repoHost = "$host/#$admin/repository/repositories:$repo"
  driver.get(repoHost)
  sleep(2)
  // set routing rule
  def input = driver.findElements(By.xpath("//input[@name='routingRuleId']"))[0]
  input.clear()
  input.sendKeys(routing)
  sleep(2)
  // save
  driver.findElements(By.xpath("//span[text()='Save']"))[0].click()
  sleep(2)
  println("----> Set rule($routing) to repository($repo")
}
println("----> Routing rules setting completed")

driver.close()