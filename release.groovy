#!/usr/bin/groovy
def stage(){
  return stageProject{
    project = 'fabric8-services/fabric8-tenant-che'
    useGitTagForNextVersion = true
  }
}

def approveRelease(project){
  def releaseVersion = project[1]
  approve{
    room = null
    version = releaseVersion
    console = null
    environment = 'fabric8'
  }
}

def release(project){
  releaseProject{
    stagedProject = project
    useGitTagForNextVersion = true
    helmPush = false
    groupId = 'io.fabric8.tenant'
    githubOrganisation = 'fabric8io'
    artifactIdToWatchInCentral = 'tenant-che'
    artifactExtensionToWatchInCentral = 'pom'
    promoteToDockerRegistry = 'docker.io'
    dockerOrganisation = 'fabric8'
    imagesToPromoteToDockerHub = []
    extraImagesToTag = null
  }
}

def updatefabric8Tenant(releaseVersion){
  ws{
    container(name: 'clients') {

      def flow = new io.fabric8.Fabric8Commands()
      flow.setupGitSSH()

      def uid = UUID.randomUUID().toString()
      def branch = "versionUpdate${uid}"
      def message = "Update fabric8-tenant-che version to ${releaseVersion}"

      sh """
         git clone git@github.com:fabric8-services/fabric8-tenant.git --depth 1
         cd fabric8-tenant
         echo ${releaseVersion} > CHE_VERSION
         git checkout -b ${branch}
         git commit -a -m "${message}"
         git push origin ${branch}
         """

      def prId = flow.createPullRequest(message,'fabric8-services/fabric8-tenant',"versionUpdate${uid}")
      flow.mergePR('fabric8-services/fabric8-tenant',prId)
    }
  }
}
return this;
