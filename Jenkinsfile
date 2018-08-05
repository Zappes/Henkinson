node {
    checkout scm

    stage('Maven Build') {
        withMaven(
                maven: 'maven',
                jdk: 'jdk8',
                mavenLocalRepo: '.repository',
                mavenSettingsConfig: env.MAVEN_SETTINGS
        ) {
            sh 'mvn -U -Dmaven.test.failure.ignore=true clean install'
        }
    }
}