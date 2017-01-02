package psiprobe;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

public class ProbeSitemeshFilter extends ConfigurableSiteMeshFilter {

    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
      builder.addDecoratorPath("/", "/WEB-INF/jsp/decorators/decorator.htm") 
             .addDecoratorPath("/*.htm", "/WEB-INF/jsp/decorators/decorator.htm")
             .addDecoratorPath("/*system.jsp", "/WEB-INF/jsp/decorators/system.jsp")
             .addDecoratorPath("/*application.jsp", "/WEB-INF/jsp/decorators/application.jsp")
             .addExcludedPath("/*.xml.htm")
             .addExcludedPath("/*.ajax*")
             .addExcludedPath("/WEB-INF/*");
    }

}
