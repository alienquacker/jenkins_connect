    
pipeline {
    agent{node('master')}
    stages {
        stage('Download project') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop isng"
                            sh "echo '${password}' | sudo -S docker container rm isng"
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
                        }
                    }                    
                }
                script {
                    echo 'Start download project'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'AZGit', url: 'https://github.com/alienquacker/jenkins_connect.git']]])
                }
            }
        }
        stage ('Create docker image'){
            steps{
                script{
                    sh "docker build ${WORKSPACE}/auto -t webapp"
                    sh "docker run -d webapp"
                    sh "docker exec -it webapp "df -h > ~/proc""
                }
            }
        }
        
    }

    
}
