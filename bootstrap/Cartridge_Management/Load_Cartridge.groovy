// Jobs
def generateLoadCartridgeJob = workflowJob("/Load_Cartridge")

generateLoadCartridgeJob.with {
    parameters
    {
        stringParam("workspaceName","ExampleWorkspace","Name of the workspace to load cartridge in (either existing or new).")
        stringParam("projectName","ExampleProject","Name of the project to load cartridge in (either existing or new).")
        extensibleChoiceParameterDefinition {
            name('CARTRIDGE_CLONE_URL')
            choiceListProvider {
                systemGroovyChoiceListProvider {
                    scriptText('''
                          try {
                            def html = "https://raw.githubusercontent.com/Accenture/adop-platform-management/master/cartridges.txt".toURL().text
                            def catridges_list = []
                            html.split('\\n').each {
                                catridges_list.add("${it}")
                            };
                            def cartridges_file = new File("/var/jenkins_home/userContent/cartridges.txt")
                            cartridges_file.write html
                            return catridges_list;
                          }
                          catch (Exception e) {
                            try {
                              def cartridges_list = []
                              def cartridges_file = new File("/var/jenkins_home/userContent/cartridges.txt")
                              cartridges_file.readLines().each {
                                  cartridges_list.add("${it}");
                              }
                              return cartridges_list;
                            }
                            catch (Exception a) {
                              return [ a ];
                            }
                          }''')
                    defaultChoice('Top')
                    usePredefinedVariables(false)
                }
            }
            editable(true)
            description('Cartridge URL to load')
        }
        stringParam('CARTRIDGE_FOLDER', '', 'The folder within the project namespace where your cartridge will be loaded into.')
        stringParam('FOLDER_DISPLAY_NAME', '', 'Display name of the folder where the cartridge is loaded.')
        stringParam('FOLDER_DESCRIPTION', '', 'Description of the folder where the cartridge is loaded.')
    }
    definition
    {
        cps
        {
            script('''// Setup Workspace
                    build job: 'Workspace_Management/Generate_Workspace', parameters: [[$class: 'StringParameterValue', name: 'WORKSPACE_NAME', value: "${workspaceName}"]]

                    // Setup Faculty
                    build job: "${workspaceName}/Project_Management/Generate_Project", parameters: [[$class: 'StringParameterValue', name: 'PROJECT_NAME', value: "${projectName}"]]
                    retry(5)
                    {
                        build job: "${workspaceName}/${projectName}/Cartridge_Management/Load_Cartridge", parameters: [[$class: 'StringParameterValue', name: 'CARTRIDGE_FOLDER', value: "${CARTRIDGE_FOLDER}"], [$class: 'StringParameterValue', name: 'FOLDER_DISPLAY_NAME', value: "${FOLDER_DISPLAY_NAME}"], [$class: 'StringParameterValue', name: 'FOLDER_DESCRIPTION', value: "${FOLDER_DESCRIPTION}"], [$class: 'StringParameterValue', name: 'CARTRIDGE_CLONE_URL', value: "${CARTRIDGE_CLONE_URL}"]]
                    }''')
            sandbox()
        }
    }
}