/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.controllers.sessions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import psiprobe.controllers.AbstractTomcatContainerController;

/**
 * Expires a list of sessionIDs. Accepts a list of sid_webapp parameters that are expected to be in
 * a form of "sid;webapp"
 */
@Controller
public class ExpireSessionsController extends AbstractTomcatContainerController {

  @GetMapping(path = "/app/expire_list.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    for (String sidWebApp : ServletRequestUtils.getStringParameters(request, "sid_webapp")) {
      if (sidWebApp != null) {
        String[] ss = sidWebApp.split(";", -1);
        if (ss.length != 2) {
          return new ModelAndView("errors/paramerror");
        }
        String sessionId = ss[0];
        String appName = ss[1];
        Context context = getContainerWrapper().getTomcatContainer().findContext(appName);
        if (context == null) {
          return new ModelAndView("errors/paramerror");
        }
        Manager manager = context.getManager();
        Session session = manager.findSession(sessionId);
        if (session != null && session.isValid()) {
          session.expire();
        }
      }
    }
    return new ModelAndView(new InternalResourceView(getViewName()));
  }

  @Value("/sessions.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
