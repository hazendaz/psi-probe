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
package psiprobe.controllers.connectors;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.Connector;

/**
 * The Class BaseGetConnectorController.
 */
public class BaseGetConnectorController extends AbstractTomcatContainerController {

  /** The container listener bean. */
  @Inject
  private ContainerListenerBean containerListenerBean;

  /**
   * Gets the container listener bean.
   *
   * @return the container listener bean
   */
  public ContainerListenerBean getContainerListenerBean() {
    return containerListenerBean;
  }

  /**
   * Sets the container listener bean.
   *
   * @param containerListenerBean the new container listener bean
   */
  public void setContainerListenerBean(ContainerListenerBean containerListenerBean) {
    this.containerListenerBean = containerListenerBean;
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletRequestBindingException, MalformedObjectNameException,
      AttributeNotFoundException, InstanceNotFoundException, IntrospectionException,
      ReflectionException, MBeanException, IOException, URISyntaxException, GeoIp2Exception {
    String connectorName = ServletRequestUtils.getStringParameter(request, "cn");
    Connector connector = null;

    if (connectorName != null) {
      List<Connector> connectors = containerListenerBean.getConnectors(false);
      for (Connector conn : connectors) {
        if (connectorName.equals(conn.getProtocolHandler())) {
          connector = conn;
          break;
        }
      }
    }

    return new ModelAndView(getViewName(), "connector", connector);
  }

}
