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
package psiprobe.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.NamingException;

import org.apache.catalina.Context;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.w3c.dom.*;

import psiprobe.model.ApplicationResource;
import psiprobe.model.DataSourceInfo;

class JBossResourceResolverBeanTest {

  @Test
  void testGetMBeanServerReturnsJbossDomain() {
    MBeanServer jbossServer = mock(MBeanServer.class);
    when(jbossServer.getDefaultDomain()).thenReturn("jboss");
    List<MBeanServer> servers = List.of(jbossServer);

    try (MockedStatic<MBeanServerFactory> factoryMock =
        Mockito.mockStatic(MBeanServerFactory.class)) {
      factoryMock.when(() -> MBeanServerFactory.findMBeanServer(null)).thenReturn(servers);

      JBossResourceResolverBean bean = new JBossResourceResolverBean();
      assertSame(jbossServer, bean.getMBeanServer());
    }
  }

  @Test
  void testGetMBeanServerReturnsNullIfNoJboss() {
    MBeanServer otherServer = mock(MBeanServer.class);
    when(otherServer.getDefaultDomain()).thenReturn("other");
    List<MBeanServer> servers = List.of(otherServer);

    try (MockedStatic<MBeanServerFactory> factoryMock =
        Mockito.mockStatic(MBeanServerFactory.class)) {
      factoryMock.when(() -> MBeanServerFactory.findMBeanServer(null)).thenReturn(servers);

      JBossResourceResolverBean bean = new JBossResourceResolverBean();
      assertNull(bean.getMBeanServer());
    }
  }

  @Test
  void testSupportsMethods() {
    JBossResourceResolverBean bean = new JBossResourceResolverBean();
    assertFalse(bean.supportsPrivateResources());
    assertTrue(bean.supportsGlobalResources());
    assertFalse(bean.supportsDataSourceLookup());
  }

  @Test
  void testGetApplicationResourcesReturnsResources() throws Exception {
    MBeanServer server = mock(MBeanServer.class);
    ObjectName poolName = new ObjectName("jboss.jca:service=ManagedConnectionPool,name=TestDS");
    Set<ObjectName> dsNames = Set.of(poolName);

    when(server.getDefaultDomain()).thenReturn("jboss");
    when(server.queryNames(any(ObjectName.class), isNull())).thenReturn(dsNames);
    when(server.getAttribute(poolName, "Criteria")).thenReturn("ByApplication");
    when(server.getAttribute(poolName, "MaxSize")).thenReturn(10);
    when(server.getAttribute(poolName, "ConnectionCount")).thenReturn(5);
    when(server.getAttribute(poolName, "InUseConnectionCount")).thenReturn(2L);

    ObjectName factoryName =
        new ObjectName("jboss.jca:service=ManagedConnectionFactory,name=TestDS");
    Element elm = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    Node node = mock(Node.class);
    NamedNodeMap attrs = mock(NamedNodeMap.class);
    Node nameAttr = mock(Node.class);
    Node valueNode = mock(Node.class);

    when(server.getAttribute(factoryName, "ManagedConnectionFactoryProperties")).thenReturn(elm);
    when(elm.getChildNodes()).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getAttributes()).thenReturn(attrs);
    when(attrs.getNamedItem("name")).thenReturn(nameAttr);
    when(nameAttr.getNodeValue()).thenReturn("ConnectionURL");
    when(node.getFirstChild()).thenReturn(valueNode);
    when(valueNode.getNodeValue()).thenReturn("jdbc:test");

    try (MockedStatic<MBeanServerFactory> factoryMock =
        Mockito.mockStatic(MBeanServerFactory.class)) {
      factoryMock.when(() -> MBeanServerFactory.findMBeanServer(null)).thenReturn(List.of(server));

      JBossResourceResolverBean bean = new JBossResourceResolverBean();
      List<ApplicationResource> resources = bean.getApplicationResources();
      assertEquals(1, resources.size());
      ApplicationResource res = resources.get(0);
      assertEquals("TestDS", res.getName());
      assertEquals("jboss", res.getType());
      assertEquals("Application", res.getAuth());
      DataSourceInfo info = res.getDataSourceInfo();
      assertNotNull(info);
      assertEquals(10, info.getMaxConnections());
      assertEquals(5, info.getEstablishedConnections());
      assertEquals(2, info.getBusyConnections());
      assertEquals("jdbc:test", info.getJdbcUrl());
      assertTrue(info.isResettable());
    }
  }

  @Test
  void testGetApplicationResourcesHandlesException() throws Exception {
    MBeanServer server = mock(MBeanServer.class);
    when(server.getDefaultDomain()).thenReturn("jboss");
    when(server.queryNames(any(ObjectName.class), isNull()))
        .thenThrow(new RuntimeException("fail"));

    try (MockedStatic<MBeanServerFactory> factoryMock =
        Mockito.mockStatic(MBeanServerFactory.class)) {
      factoryMock.when(() -> MBeanServerFactory.findMBeanServer(null)).thenReturn(List.of(server));

      JBossResourceResolverBean bean = new JBossResourceResolverBean();
      List<ApplicationResource> resources = bean.getApplicationResources();
      assertNotNull(resources);
      assertTrue(resources.isEmpty());
    }
  }

  @Test
  void testGetApplicationResourcesContextReturnsEmptyList() throws NamingException {
    JBossResourceResolverBean bean = new JBossResourceResolverBean();
    Context ctx = mock(Context.class);
    assertTrue(bean.getApplicationResources(ctx).isEmpty());
  }

  @Test
  void testGetApplicationResourcesContextContainerThrows() {
    JBossResourceResolverBean bean = new JBossResourceResolverBean();
    Context ctx = mock(Context.class);
    ContainerWrapperBean container = mock(ContainerWrapperBean.class);
    assertThrows(UnsupportedOperationException.class,
        () -> bean.getApplicationResources(ctx, container));
  }

  @Test
  void testResetResourceSuccess() throws Exception {
    MBeanServer server = mock(MBeanServer.class);
    when(server.getDefaultDomain()).thenReturn("jboss");
    try (MockedStatic<MBeanServerFactory> factoryMock =
        Mockito.mockStatic(MBeanServerFactory.class)) {
      factoryMock.when(() -> MBeanServerFactory.findMBeanServer(null)).thenReturn(List.of(server));

      JBossResourceResolverBean bean = new JBossResourceResolverBean();
      Context ctx = mock(Context.class);
      ContainerWrapperBean container = mock(ContainerWrapperBean.class);

      assertTrue(bean.resetResource(ctx, "TestDS", container));
      verify(server).invoke(any(ObjectName.class), eq("stop"), isNull(), isNull());
      verify(server).invoke(any(ObjectName.class), eq("start"), isNull(), isNull());
    }
  }

  @Test
  void testResetResourceHandlesException() throws Exception {
    MBeanServer server = mock(MBeanServer.class);
    when(server.getDefaultDomain()).thenReturn("jboss");
    doThrow(new RuntimeException("fail")).when(server).invoke(any(ObjectName.class), eq("stop"),
        isNull(), isNull());

    try (MockedStatic<MBeanServerFactory> factoryMock =
        Mockito.mockStatic(MBeanServerFactory.class)) {
      factoryMock.when(() -> MBeanServerFactory.findMBeanServer(null)).thenReturn(List.of(server));

      JBossResourceResolverBean bean = new JBossResourceResolverBean();
      Context ctx = mock(Context.class);
      ContainerWrapperBean container = mock(ContainerWrapperBean.class);

      assertFalse(bean.resetResource(ctx, "TestDS", container));
    }
  }

  @Test
  void testResetResourceMalformedObjectNameThrowsNamingException() {
    JBossResourceResolverBean bean = new JBossResourceResolverBean();
    Context ctx = mock(Context.class);
    ContainerWrapperBean container = mock(ContainerWrapperBean.class);

    assertThrows(NamingException.class, () -> bean.resetResource(ctx, "bad:name", container));
  }

  @Test
  void testLookupDataSourceThrows() {
    JBossResourceResolverBean bean = new JBossResourceResolverBean();
    Context ctx = mock(Context.class);
    ContainerWrapperBean container = mock(ContainerWrapperBean.class);

    assertThrows(UnsupportedOperationException.class,
        () -> bean.lookupDataSource(ctx, "TestDS", container));
  }
}
