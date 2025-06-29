/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.rest.util;

import static org.operaton.bpm.engine.rest.util.EngineUtil.getProcessEngineProvider;

import org.operaton.bpm.engine.impl.ManagementServiceImpl;
import org.operaton.bpm.engine.impl.diagnostics.PlatformDiagnosticsRegistry;
import org.operaton.bpm.engine.rest.spi.ProcessEngineProvider;

public class WebApplicationUtil {

  private WebApplicationUtil() {
  }

  public static void setApplicationServer(String serverInfo) {
    if ((serverInfo != null && !serverInfo.isEmpty())
            && PlatformDiagnosticsRegistry.getApplicationServer() == null) {
      // set the application server info globally for all engines in the container
      PlatformDiagnosticsRegistry.setApplicationServer(serverInfo);
    }
  }

  /**
   * Adds the web application name to the telemetry data of the engine.
   *
   * @param engineName
   *          the engine for which the web application usage should be indicated
   * @param webapp
   *          the web application that is used with the engine
   * @return whether the web application was successfully added or not
   */
  public static boolean setWebapp(String engineName, String webapp) {
    ProcessEngineProvider processEngineProvider = getProcessEngineProvider();
    ManagementServiceImpl managementService = (ManagementServiceImpl) processEngineProvider.getProcessEngine(engineName).getManagementService();
    return managementService.addWebappToTelemetry(webapp);
  }
}
