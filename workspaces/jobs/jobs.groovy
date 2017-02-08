// Constants
def platformToolsGitURL = "https://github.com/IrmantasM/adop-platform-management.git"

// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def workspaceFolder = folder(workspaceFolderName)

def projectManagementFolderName= workspaceFolderName + "/Project_Management"
def projectManagementFolder = folder(projectManagementFolderName) { displayName('Project Management') }

// Jobs
def generateProjectJob = freeStyleJob(projectManagementFolderName + "/Generate_Project")

// Setup Generate_Project
generateProjectJob.with {
    parameters
    {
        stringParam("PROJECT_NAME","","The name of the project to be generated.")
        booleanParam('CUSTOM_SCM_NAMESPACE', false, 'Enables the option to provide a custom project namespace for your SCM provider')
    }
    environmentVariables
    {
        env('WORKSPACE_NAME',workspaceFolderName)
    }
    wrappers
    {
        preBuildCleanup()
        injectPasswords()
        maskPasswords()
    }
    steps
    {
        shell('''#!/bin/bash -e
                # Validate Variables
                pattern=" |'"
                if [[ "${PROJECT_NAME}" =~ ${pattern} ]]; then
                    echo "PROJECT_NAME contains a space, please replace with an underscore - exiting..."
                    exit 1
                fi''')
        dsl
        {
            external("projects/jobs/**/*.groovy")
        }
    }
    scm
    {
        git
        {
            remote
            {
                name("origin")
                url("${platformToolsGitURL}")
                credentials("adop-jenkins-master")
            }
            branch("*/master")
        }
    }
}
