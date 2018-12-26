pipeline {
	parameters {        
		booleanParam(name: 'IsReleaseBuild', description: 'Check the box if you want to create a release build') 
		string(name: 'BranchName', defaultValue: 'master', description: 'Branch used by the job')  
	}
    agent {
        node { label 'Plugins' }
    }

    stages {
        stage('Pipeline Info') {
            steps {
                echo bat(returnStdout: true, script: 'set')
            }
        }
		
		//stage ('Clean Workspace') {
        //    steps {
        //        cleanWs()
        //    }        
        //}

        stage('Remove Snapshot From Build') {
            when {
                expression {
                    return params.IsReleaseBuild
                }
            }
            steps {
                echo " ----------------------------------------------------- "
                echo "|  SNAPSHOT DISABLED: Removing Snapshot Before Build  |"
                echo " ----------------------------------------------------- "

                dir("cli") {
                    powershell '''		If(Test-Path gradle.properties)
					{  
						$FileContent = Get-Content -Path gradle.properties
						Foreach($LineContent in $FileContent)
						{
							If($LineContent.Length -gt 9)
							{
								If($LineContent.Substring(0,9) -eq "version =")
								{
									$NewLineContent = $LineContent.Replace("-SNAPSHOT", "")
									(Get-Content gradle.properties) | ForEach-Object { $_ -replace "$LineContent", "$NewLineContent" } | Set-Content gradle.properties
								}
							}
						}
					}'''
					
					powershell '''		If(Test-Path build.gradle)
					{  
						$FileContent = Get-Content -Path build.gradle
						Foreach($LineContent in $FileContent)
						{
							If($LineContent.Length -gt 9)
							{
								If($LineContent.Substring(0,9) -eq "version =")
								{
									$NewLineContent = $LineContent.Replace("-SNAPSHOT", "")
									(Get-Content build.gradle) | ForEach-Object { $_ -replace "$LineContent", "$NewLineContent" } | Set-Content build.gradle
								}
							}
						}
					}'''
                }
            }
        }

        stage('Build') {
            steps {
				dir("cli") {
					bat "gradlew.bat -DIsReleaseBuild=${params.IsReleaseBuild} -DBranchName=master --stacktrace clean build && exit %%ERRORLEVEL%%"
				}
            }
        }
    }
}
