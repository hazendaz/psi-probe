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
package psiprobe.controllers.cluster;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import psiprobe.tools.TimeExpression;

/**
 * The Class ClusterStatsController.
 */
@Controller
public class ClusterStatsController extends BaseClusterStatsController {

  @Value("false")
  @Override
  public void setLoadMembers(boolean loadMembers) {
    super.setLoadMembers(loadMembers);
  }

  /**
   * Sets the collection period.
   *
   * @param collectionPeriod the new collection period
   */
  @Value("${psiprobe.beans.stats.collectors.cluster.period}")
  public void setCollectionPeriod(String collectionPeriod) {
    super.setCollectionPeriod(TimeExpression.inSeconds(collectionPeriod));
  }

  @GetMapping(path = "/cluster.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Value("cluster")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
