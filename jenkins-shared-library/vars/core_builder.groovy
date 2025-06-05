def call(Map config) {

    def pipelineNames = [
        go: "go-ci-pipeline"
    ]
    config.pipelineName = pipelineNames[config.application_type]
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
            string(name: 'BRANCH', description: 'Branch to build', defaultValue: '')
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
            stage("Checkout Project Code") {
                steps {
                    checkout scm: [
                        $class: "GitSCM",
                        branches: [[name: "refs/heads/${config.target_branch}"]],
                        submoduleCfg: [],
                        userRemoteConfigs: [
                            config.scm_global_config
                        ]
                    ]
                }
            }

            stage("Read Project Config") {
                steps {
                    script {
                        commitID = sh(
                            script: """
                            git log --pretty=format:"%h" | head -1
                            """,
                            returnStdout: true
                        ).trim()
                        writeFile file: 'commit_id.txt', text: commitID
                        archiveArtifacts artifacts: 'commit_id.txt'
                        buildDescription("Container ID: ${commitID}")

                        echo "${config.workspace}"
                        echo "${config.project}"
                        echo "${config.service}"
                        echo "${config.environment}"
                        echo "${config.scm_address}"
                        echo "${config.branch}"
                    }
                }
            }
            stage('Trigger Tekton Pipeline') {
                steps {
                    script {
                        def pipelineName = "generic-ci-pipeline"
                        def tknCommand = "tkn pipeline start ${pipelineName} " +
                                    "--namespace tekton-executions " +
                                    "--param branch=${config.branch} " +
                                    "--param repo=${config.scm_address} " +
                                    "--param workspace=${config.workspace} " +
                                    "--param namespace=${config.project}-${config.environment} " +
                                    "--param project=${config.project} " +
                                    "--param service=${config.service} " +
                                    "--param registry=${config.container_artifact_repo_address} " +
                                    "--param builder=${config.builder} " +
                                    "--param application_type=${config.application_type} " +
                                    "--param version='tekton-test' " +
                                    "--workspace name=codebase,claimName=tekton-pvc " +
                                    "--output name"

                        generatedPipelineRunName = sh(script: tknCommand, returnStdout: true).trim()
                        echo "Tekton Pipeline '${config.pipelineName}' triggered in namespace 'tekton-executions'"
                        sh "tkn pr logs ${generatedPipelineRunName} -n tekton-executions -f"
                    }
                }
            }
            stage('Checking Results of the CI') {
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
