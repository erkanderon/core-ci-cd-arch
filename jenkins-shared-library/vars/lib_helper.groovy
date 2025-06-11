def configureInit(Map config) {

    // Configure branch from params
    if ( params.containsKey("BRANCH") && params.BRANCH != "" ) {
        config.target_branch = params.BRANCH
    }

    if ( config.scm_security ) {
        config.scm_global_config.credentialsId = "${config.scm_credentials_id}"
    }

    config.container_artifact_repo_address = "registry.adress.com"
    config.artifact_repo_address = "nexus.address.com"

}