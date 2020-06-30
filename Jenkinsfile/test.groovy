pipeline {
    agent{node('master')}
    stages {
        stage('Clear workspace & download from git') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop az_git"
                            sh "echo '${password}' | sudo -S docker container rm az_git"
                        } catch (Exception e) {
                            print 'Container does not exist, skipping the cleanup'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
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
        stage ('Build & run docker image'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t az_nginx"
                        sh "echo '${password}' | sudo -S docker run -d -p 8157:80 --name az_git -v /home/adminci/is_mount_dir:/stat az_nginx"
                    }
                }
            }
        }
        stage ('Get stats & write to file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker exec -t az_git bash -c 'df -h > /stat/stats_az.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t az_git bash -c 'top -n 1 -b >> /stat/stats_az.txt'"
                    }
                }
            }
        }     
    }
}
