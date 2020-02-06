#!/usr/bin/env groovy
def user = security.securitySystem.getUser('admin')
// parameters set with -d in curl is concated by "&": "aaa&bbb&ccc"
def parameters = args.split("&")
def scriptDir = parameters[0]
def adminUser = parameters[1]
def newPassword = parameters[2]
log.info("--> changing user's password, ${adminUser}:********")
user.setEmailAddress('admin@foobar.com')
security.securitySystem.updateUser(user)
security.securitySystem.changePassword(adminUser,newPassword)
log.info("--> Script executed: admin user password changed")