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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.codebox.bean.JavaBeanTester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.naming.NamingException;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import psiprobe.TomcatContainer;
import psiprobe.model.ApplicationParam;
import psiprobe.model.ApplicationResource;
import psiprobe.model.DataSourceInfo;
import psiprobe.model.FilterInfo;
import psiprobe.model.FilterMapping;
import psiprobe.model.jsp.Summary;

/**
 * The Class ContainerWrapperBeanTest.
 */
class ContainerWrapperBeanTest {

  private ContainerWrapperBean bean;

  @BeforeEach
  void setUp() {
    bean = new ContainerWrapperBean();
  }

  /**
   * Javabean tester.
   */
  @Test
  void javabeanTester() {
    JavaBeanTester.builder(ContainerWrapperBean.class).loadData().test();
  }

  @Test
  void testForceFirstAdapterProperty() {
    assertFalse(bean.isForceFirstAdapter());
    bean.setForceFirstAdapter(true);
    assertTrue(bean.isForceFirstAdapter());
  }

  @Test
  void testSetAndGetAdapterClasses() {
    List<String> adapters = Arrays.asList("foo", "bar");
    bean.setAdapterClasses(adapters);
    assertEquals(adapters, bean.getAdapterClasses());
  }

  @Test
  void testSetAndGetResourceResolvers() {
    Map<String, ResourceResolver> resolvers = new HashMap<>();
    bean.setResourceResolvers(resolvers);
    assertEquals(resolvers, bean.getResourceResolvers());
  }

  @Test
  void testGetResourceResolver_Default() {
    ResourceResolver defaultResolver = mock(ResourceResolver.class);
    Map<String, ResourceResolver> resolvers = new HashMap<>();
    resolvers.put("default", defaultResolver);
    bean.setResourceResolvers(resolvers);

    // Should use default resolver if no jboss property
    System.clearProperty("jboss.server.name");
    assertSame(defaultResolver, bean.getResourceResolver());
  }

  @Test
  void testGetResourceResolver_Jboss() {
    ResourceResolver jbossResolver = mock(ResourceResolver.class);
    Map<String, ResourceResolver> resolvers = new HashMap<>();
    resolvers.put("jboss", jbossResolver);
    bean.setResourceResolvers(resolvers);

    System.setProperty("jboss.server.name", "test");
    try {
      assertSame(jbossResolver, bean.getResourceResolver());
    } finally {
      System.clearProperty("jboss.server.name");
    }
  }

  @Test
  void testSetWrapper_AdapterSelection() throws Exception {
    // Prepare a fake adapter class
    String adapterClassName = "psiprobe.beans.FakeTomcatContainer";
    List<String> adapters = Collections.singletonList(adapterClassName);
    bean.setAdapterClasses(adapters);

    // Register the fake class
    try (MockedStatic<Class> classMock =
        Mockito.mockStatic(Class.class, Mockito.CALLS_REAL_METHODS)) {
      classMock.when(() -> Class.forName(adapterClassName))
          .thenReturn((Class<?>) FakeTomcatContainer.class);

      Wrapper wrapper = mock(Wrapper.class);
      bean.setWrapper(wrapper);

      TomcatContainer container = bean.getTomcatContainer();
      assertNotNull(container);
      assertTrue(container instanceof FakeTomcatContainer);
      assertSame(wrapper, ((FakeTomcatContainer) container).wrapper);
    }
  }

  @Test
  void testSetWrapper_UnregistersAdapter() {
    // Setup with a fake adapter
    String adapterClassName = "psiprobe.beans.FakeTomcatContainer";
    bean.setAdapterClasses(Collections.singletonList(adapterClassName));
    try (MockedStatic<Class> classMock =
        Mockito.mockStatic(Class.class, Mockito.CALLS_REAL_METHODS)) {
      classMock.when(() -> Class.forName(adapterClassName))
          .thenReturn((Class<?>) FakeTomcatContainer.class);

      Wrapper wrapper = mock(Wrapper.class);
      bean.setWrapper(wrapper);

      // Now unregister
      bean.setWrapper(null);
      TomcatContainer container = bean.getTomcatContainer();
      assertNotNull(container);
      assertNull(((FakeTomcatContainer) container).wrapper);
    }
  }

  @Test
  void testGetPrivateDataSources_FiltersByDataSourceInfo() throws Exception {
    TomcatContainer container = mock(TomcatContainer.class);
    bean.setAdapterClasses(Collections.emptyList());
    // Set tomcatContainer via reflection (since setWrapper is complex)
    java.lang.reflect.Field f = ContainerWrapperBean.class.getDeclaredField("tomcatContainer");
    f.setAccessible(true);
    f.set(bean, container);

    ResourceResolver resolver = mock(ResourceResolver.class);
    when(resolver.supportsPrivateResources()).thenReturn(true);
    bean.setResourceResolvers(Map.of("default", resolver));
    java.lang.reflect.Field rr = ContainerWrapperBean.class.getDeclaredField("resourceResolver");
    rr.setAccessible(true);
    rr.set(bean, resolver);

    Context ctx = mock(Context.class);
    when(container.findContexts()).thenReturn(List.of(ctx));
    ApplicationResource res1 = mock(ApplicationResource.class);
    when(res1.getDataSourceInfo()).thenReturn(new DataSourceInfo());
    ApplicationResource res2 = mock(ApplicationResource.class);
    when(res2.getDataSourceInfo()).thenReturn(null);
    when(resolver.getApplicationResources(ctx, bean)).thenReturn(List.of(res1, res2));

    List<ApplicationResource> result = bean.getPrivateDataSources();
    assertEquals(1, result.size());
    assertSame(res1, result.get(0));
  }

  @Test
  void testGetGlobalDataSources_FiltersByDataSourceInfo() throws Exception {
    ResourceResolver resolver = mock(ResourceResolver.class);
    when(resolver.supportsGlobalResources()).thenReturn(true);
    bean.setResourceResolvers(Map.of("default", resolver));
    java.lang.reflect.Field rr = ContainerWrapperBean.class.getDeclaredField("resourceResolver");
    rr.setAccessible(true);
    rr.set(bean, resolver);

    ApplicationResource res1 = mock(ApplicationResource.class);
    when(res1.getDataSourceInfo()).thenReturn(new DataSourceInfo());
    ApplicationResource res2 = mock(ApplicationResource.class);
    when(res2.getDataSourceInfo()).thenReturn(null);
    when(resolver.getApplicationResources()).thenReturn(List.of(res1, res2));

    List<ApplicationResource> result = bean.getGlobalDataSources();
    assertEquals(1, result.size());
    assertSame(res1, result.get(0));
  }

  @Test
  void testGetDataSources_CombinesPrivateAndGlobal() throws Exception {
    ContainerWrapperBean spyBean = spy(bean);
    ApplicationResource priv = mock(ApplicationResource.class);
    ApplicationResource glob = mock(ApplicationResource.class);
    doReturn(List.of(priv)).when(spyBean).getPrivateDataSources();
    doReturn(List.of(glob)).when(spyBean).getGlobalDataSources();

    List<ApplicationResource> result = spyBean.getDataSources();
    assertEquals(2, result.size());
    assertTrue(result.contains(priv));
    assertTrue(result.contains(glob));
  }

  // --- Helper class for adapter mocking ---
  public static class FakeTomcatContainer implements TomcatContainer {
    public Wrapper wrapper;

    @Override
    public void setWrapper(Wrapper wrapper) {
      this.wrapper = wrapper;
    }

    @Override
    public boolean canBoundTo(String serverInfo) {
      return true;
    }

    @Override
    public List<Context> findContexts() {
      return Collections.emptyList();
    }

    @Override
    public Context findContext(String name) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String formatContextName(String name) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String formatContextFilename(String contextName) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<Connector> findConnectors() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void stop(String name) throws Exception {
      // TODO Auto-generated method stub

    }

    @Override
    public void start(String name) throws Exception {
      // TODO Auto-generated method stub

    }

    @Override
    public void remove(String name) throws Exception {
      // TODO Auto-generated method stub

    }

    @Override
    public void installWar(String name) throws Exception {
      // TODO Auto-generated method stub

    }

    @Override
    public File getAppBase() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public File getConfigFile(Context context) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getConfigBase() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean installContext(String contextName) throws Exception {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void listContextJsps(Context context, Summary summary, boolean compile) {
      // TODO Auto-generated method stub

    }

    @Override
    public void recompileJsps(Context context, Summary summary, List<String> names) {
      // TODO Auto-generated method stub

    }

    @Override
    public void discardWorkDir(Context context) {
      // TODO Auto-generated method stub

    }

    @Override
    public String getHostName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getServletFileNameForJsp(Context context, String jspName) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<FilterMapping> getApplicationFilterMaps(Context context) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean getAvailable(Context context) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void addContextResource(Context context, List<ApplicationResource> resourceList) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addContextResourceLink(Context context, List<ApplicationResource> resourceList) {
      // TODO Auto-generated method stub

    }

    @Override
    public List<FilterInfo> getApplicationFilters(Context context) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<ApplicationParam> getApplicationInitParams(Context context) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean resourceExists(String name, Context context) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public InputStream getResourceStream(String name, Context context) throws IOException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Long[] getResourceAttributes(String name, Context context) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void bindToContext(Context context) throws NamingException {
      // TODO Auto-generated method stub

    }

    @Override
    public void unbindFromContext(Context context) throws NamingException {
      // TODO Auto-generated method stub

    }
  }
}

