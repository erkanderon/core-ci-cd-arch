def call(Map config) {

    def generatedPipelineRunName;

    // Configure repository settings
    config.scm_global_config = [url: config.scm_address]
    if ( config.scm_security ) {
        config.scm_global_config.credentialsId = "${config.scm_credentials_id}"
    }

    pipeline {
        agent {
            label "management-cluster"
        }

        parameters {
            extendedChoice(
                name: 'IMAGE',
                type: 'PT_SINGLE_SELECT', // Or 'PT_MULTI_SELECT', depending on your need
                description: 'Choose a commit ID to deploy',
                groovyScript: '''
                    import hudson.model.*

                    try{
                        def items = []
                        hudson.model.Hudson.instance.getItem("ZIP").getItem("CORE").getItem("stream-hub").getItem("1.Build").buildsAsMap.each {
                            if(it.value.result != null && it.value.result.name == 'SUCCESS') {
                                it.value.artifacts.find { it.fileName =~ /commit_id.txt/ }.each{
                                    items << it.file.getText('UTF-8').take(40)
                                }
                            }
                        }
                        items = items.unique()
                        return items
                    }
                    catch (Exception e) {
                        print "There was a problem fetching the versions" + e
                    }
                    '''
            )
        }

        stages {
            stage("Configure Init") {
                steps {
                    script {
                        lib_helper.configureInit(
                            config
                        )
                    }
                }
            }

            stage("Read Project Config") {
                steps {
                    script {
                        buildDescription("Container ID: ${IMAGE}")

                        echo "${config.workspace}"
                        echo "${config.project}"
                        echo "${config.service}"
                        echo "${config.environment}"
                        echo "${config.scm_address}"
                        echo "${config.branch}"
                    }
                }
            }

            stage('Deploy New Code') {
                steps {
                    script {
                        def pipelineName = "generic-cd-pipeline"
                        def tknCommand = "tkn pipeline start ${pipelineName} " +
                                    "--namespace tekton-executions " +
                                    "--param branch=${config.branch} " +
                                    "--param repo=${config.gitops_repo_address} " +
                                    "--param workspace=${config.workspace} " +
                                    "--param project=${config.project} " +
                                    "--param namespace=${config.project}-${config.environment} " +
                                    "--param service=${config.service} " +
                                    "--param registry=${config.container_artifact_repo_address} " +
                                    "--param environment=${config.environment} " +
                                    "--param version='tekton-test' " +
                                    "--workspace name=codebase,claimName=tekton-cd-pvc " +
                                    "--output name"

                        generatedPipelineRunName = sh(script: tknCommand, returnStdout: true).trim()
                        echo "Tekton Pipeline '${pipelineName}' triggered in namespace 'tekton-executions'"
                        sh "tkn pr logs ${generatedPipelineRunName} -n tekton-executions -f"
                    }
                }
            }
            stage('Checking Results of the CD') {
                steps {
                    script {
                        // MONITORING RESULTS
                        def status = sh(
                            script: "tkn pr describe ${generatedPipelineRunName} -n tekton-executions --output jsonpath='{.status.conditions[?(@.type==\"Succeeded\")].status}'",
                            returnStdout: true
                        ).trim()

                        echo "Tekton PipelineRun '${generatedPipelineRunName}' status: ${status}"

                        if (status == 'False') {
                            error "Tekton PipelineRun '${generatedPipelineRunName}' failed."
                        } else if (status == 'True') {
                            echo "Tekton PipelineRun '${generatedPipelineRunName}' succeeded."
                        } else {
                            echo "Tekton PipelineRun '${generatedPipelineRunName}' is still running or in an unknown state: ${status}"
                            // You might want to handle running/unknown states differently
                        }
                    }
                }
            }

        }

    }
}
