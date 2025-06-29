pipeline {
    agent any
    
    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        SONAR_PROJECT_KEY = 'sale-microservice'
        SONAR_PROJECT_NAME = 'Sale Microservice'
        SONAR_HOST_URL = 'https://sonarcloud.io'
        // Variables de entorno para tests
        DATABASE_URL = 'r2dbc:pool:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
        JWT_ISSUER_URI = 'https://securetoken.google.com/security-test'
        JWT_JWK_SET_URI = 'https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com'
    }
    
    tools {
        maven 'Maven-3.9.0'
        jdk 'JDK-17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Código fuente obtenido exitosamente'
            }
        }
        
        stage('Build & Test') {
            steps {
                echo 'Iniciando compilación y pruebas...'
                sh '''
                    mvn clean compile test-compile
                    mvn test -Dspring.profiles.active=test
                '''
            }
            post {
                always {
                    // Publicar resultados de pruebas
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    
                    // Archivar reportes de cobertura
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }
        
        stage('Code Quality Analysis') {
            parallel {
                stage('SonarQube Analysis') {
                    steps {
                        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                            sh '''
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                    -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
                                    -Dsonar.host.url=${SONAR_HOST_URL} \
                                    -Dsonar.login=${SONAR_TOKEN} \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                    -Dsonar.java.coveragePlugin=jacoco
                            '''
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        echo 'Ejecutando análisis de seguridad...'
                        sh 'mvn org.owasp:dependency-check-maven:check'
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency Check Report'
                            ])
                        }
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Empaquetando aplicación...'
                sh 'mvn package -DskipTests'
                
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build & Push') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def imageTag = "${BUILD_NUMBER}"
                    def imageName = "sale-microservice:${imageTag}"
                    
                    echo "Construyendo imagen Docker: ${imageName}"
                    
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                            docker build -t ${DOCKER_USER}/sale-microservice:${BUILD_NUMBER} .
                            docker build -t ${DOCKER_USER}/sale-microservice:latest .
                            
                            echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin
                            
                            docker push ${DOCKER_USER}/sale-microservice:${BUILD_NUMBER}
                            docker push ${DOCKER_USER}/sale-microservice:latest
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Desplegando a entorno de staging...'
                // Aquí irían los comandos de despliegue
                sh 'echo "Deployment to staging environment"'
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: '¿Desplegar a producción?', ok: 'Deploy'
                echo 'Desplegando a producción...'
                // Aquí irían los comandos de despliegue a producción
                sh 'echo "Deployment to production environment"'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finalizado'
            
            // Limpiar workspace
            cleanWs()
        }
        
        success {
            emailext (
                subject: "✅ Build Exitoso: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Exitoso</h2>
                    <p><strong>Proyecto:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                    <p><strong>Duración:</strong> ${currentBuild.durationString}</p>
                    <p><a href='${env.BUILD_URL}'>Ver detalles del build</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
        
        failure {
            emailext (
                subject: "❌ Build Fallido: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Fallido</h2>
                    <p><strong>Proyecto:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                    <p><strong>Duración:</strong> ${currentBuild.durationString}</p>
                    <p><a href='${env.BUILD_URL}'>Ver detalles del build</a></p>
                    <p><a href='${env.BUILD_URL}console'>Ver logs del build</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
        
        unstable {
            emailext (
                subject: "⚠️ Build Inestable: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Inestable</h2>
                    <p><strong>Proyecto:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                    <p><strong>Duración:</strong> ${currentBuild.durationString}</p>
                    <p><a href='${env.BUILD_URL}'>Ver detalles del build</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
    }
}