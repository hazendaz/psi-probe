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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codebox.bean.JavaBeanTester;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
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

import psiprobe.TomcatContainer;
import psiprobe.model.ApplicationResource;
import psiprobe.model.DataSourceInfo;

/**
 * The Class ContainerWrapperBeanTest.
 */
class ContainerWrapperBeanTest {

  public static class RecordingTomcatContainer implements TomcatContainer {

    private static Object lastWrapper;

    private static int setWrapperCalls;

    @Override
    public void setWrapper(org.apache.catalina.Wrapper wrapper) {
      lastWrapper = wrapper;
      setWrapperCalls++;
    }

    @Override
    public boolean canBoundTo(String binding) {
      return true;
    }

    @Override
    public org.apache.catalina.Context findContext(String name) {
      return null;
    }

    @Override
    public String formatContextName(String name) {
      return name;
    }

    @Override
    public String formatContextFilename(String contextName) {
      return contextName;
    }

    @Override
    public java.util.List<org.apache.catalina.Context> findContexts() {
      return List.of();
    }

    @Override
    public java.util.List<org.apache.catalina.connector.Connector> findConnectors() {
      return List.of();
    }

    @Override
    public void stop(String name) {
      // no-op
    }

    @Override
    public void start(String name) {
      // no-op
    }

    @Override
    public void remove(String name) {
      // no-op
    }

    @Override
    public void installWar(String name) {
      // no-op
    }

    @Override
    public java.io.File getAppBase() {
      return null;
    }

    @Override
    public java.io.File getConfigFile(org.apache.catalina.Context context) {
      return null;
    }

    @Override
    public String getConfigBase() {
      return null;
    }

    @Override
    public boolean installContext(String contextName) {
      return false;
    }

    @Override
    public void listContextJsps(org.apache.catalina.Context context,
        psiprobe.model.jsp.Summary summary, boolean compile) {
      // no-op
    }

    @Override
    public void recompileJsps(org.apache.catalina.Context context,
        psiprobe.model.jsp.Summary summary, java.util.List<String> names) {
      // no-op
    }

    @Override
    public void discardWorkDir(org.apache.catalina.Context context) {
      // no-op
    }

    @Override
    public String getHostName() {
      return null;
    }

    @Override
    public String getName() {
      return "Catalina";
    }

    @Override
    public String getServletFileNameForJsp(org.apache.catalina.Context context, String jspName) {
      return null;
    }

    @Override
    public java.util.List<psiprobe.model.FilterMapping> getApplicationFilterMaps(
        org.apache.catalina.Context context) {
      return List.of();
    }

    @Override
    public boolean getAvailable(org.apache.catalina.Context context) {
      return true;
    }

    @Override
    public void addContextResource(org.apache.catalina.Context context,
        java.util.List<psiprobe.model.ApplicationResource> resourceList) {
      // no-op
    }

    @Override
    public void addContextResourceLink(org.apache.catalina.Context context,
        java.util.List<psiprobe.model.ApplicationResource> resourceList) {
      // no-op
    }

    @Override
    public java.util.List<psiprobe.model.FilterInfo> getApplicationFilters(
        org.apache.catalina.Context context) {
      return List.of();
    }

    @Override
    public java.util.List<psiprobe.model.ApplicationParam> getApplicationInitParams(
        org.apache.catalina.Context context) {
      return List.of();
    }

    @Override
    public boolean resourceExists(String name, org.apache.catalina.Context context) {
      return false;
    }

    @Override
    public java.io.InputStream getResourceStream(String name, org.apache.catalina.Context context) {
      return null;
    }

    @Override
    public Long[] getResourceAttributes(String name, org.apache.catalina.Context context) {
      return new Long[] {0L, 0L};
    }

    @Override
    public void bindToContext(org.apache.catalina.Context context) {
      // no-op
    }

    @Override
    public void unbindFromContext(org.apache.catalina.Context context) {
      // no-op
    }
  }

  /**
   * Javabean tester.
   */
  @Test
  void javabeanTester() {
    JavaBeanTester.builder(ContainerWrapperBean.class).loadData().test();
  }

  @Test
  void getResourceResolverSelectsDefaultAndJbossResolvers() {
    ResourceResolver defaultResolver = mock(ResourceResolver.class);
    ResourceResolver jbossResolver = mock(ResourceResolver.class);

    ContainerWrapperBean defaultBean = new ContainerWrapperBean();
    defaultBean.setResourceResolvers(Map.of("default", defaultResolver, "jboss", jbossResolver));
    assertSame(defaultResolver, defaultBean.getResourceResolver());
    assertSame(defaultResolver, defaultBean.getResourceResolver());

    String previous = System.getProperty("jboss.server.name");
    try {
      System.setProperty("jboss.server.name", "node1");
      ContainerWrapperBean jbossBean = new ContainerWrapperBean();
      jbossBean.setResourceResolvers(Map.of("default", defaultResolver, "jboss", jbossResolver));
      assertSame(jbossResolver, jbossBean.getResourceResolver());
    } finally {
      if (previous == null) {
        System.clearProperty("jboss.server.name");
      } else {
        System.setProperty("jboss.server.name", previous);
      }
    }
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
  void getDataSourcesAggregatesPrivateAndGlobalDataSources() throws Exception {
    ContainerWrapperBean bean = new ContainerWrapperBean();
    ResourceResolver resolver = mock(ResourceResolver.class);
    Context context = mock(Context.class);
    TomcatContainer tomcatContainer = mock(TomcatContainer.class);

    bean.setResourceResolvers(Map.of("default", resolver));
    setTomcatContainer(bean, tomcatContainer);

    when(resolver.supportsPrivateResources()).thenReturn(true);
    when(resolver.supportsGlobalResources()).thenReturn(true);
    when(tomcatContainer.findContexts()).thenReturn(List.of(context));

    ApplicationResource privateResource = resourceWithDataSource("jdbc/private");
    ApplicationResource privateNonDatasource = new ApplicationResource();
    when(resolver.getApplicationResources(context, bean))
        .thenReturn(List.of(privateResource, privateNonDatasource));

    ApplicationResource globalResource = resourceWithDataSource("jdbc/global");
    when(resolver.getApplicationResources()).thenReturn(List.of(globalResource));

    List<ApplicationResource> dataSources = bean.getDataSources();

    assertEquals(2, dataSources.size());
    assertTrue(
        dataSources.stream().anyMatch(resource -> "jdbc/private".equals(resource.getName())));
    assertTrue(dataSources.stream().anyMatch(resource -> "jdbc/global".equals(resource.getName())));
  }

  @Test
  void setWrapperInitializesAndUnregistersSelectedAdapter() {
    ContainerWrapperBean bean = new ContainerWrapperBean();
    RecordingTomcatContainer.lastWrapper = null;
    RecordingTomcatContainer.setWrapperCalls = 0;

    bean.setAdapterClasses(List.of(RecordingTomcatContainer.class.getName()));
    bean.setForceFirstAdapter(true);

    org.apache.catalina.Wrapper wrapper = mock(org.apache.catalina.Wrapper.class);
    bean.setWrapper(wrapper);

    assertTrue(bean.getTomcatContainer() instanceof RecordingTomcatContainer);
    assertSame(wrapper, RecordingTomcatContainer.lastWrapper);

    bean.setWrapper(null);

    assertEquals(2, RecordingTomcatContainer.setWrapperCalls);
    assertSame(null, RecordingTomcatContainer.lastWrapper);
  }

  private static ApplicationResource resourceWithDataSource(String name) {
    ApplicationResource resource = new ApplicationResource();
    resource.setName(name);
    resource.setDataSourceInfo(new DataSourceInfo());
    return resource;
  }

  private static void setTomcatContainer(ContainerWrapperBean bean, TomcatContainer container)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = ContainerWrapperBean.class.getDeclaredField("tomcatContainer");
    field.setAccessible(true);
    field.set(bean, container);
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

}
