package org.camunda.bpm.unittest.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.SetProcessDefinitionVersionCmd;

public class MigrateVersion implements JavaDelegate {
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    SetProcessDefinitionVersionCmd command = new SetProcessDefinitionVersionCmd(execution.getProcessInstanceId(), 2);
    processEngineConfiguration.getCommandExecutorTxRequired().execute(command);

    execution.getActivityInstanceId();

  }
}
