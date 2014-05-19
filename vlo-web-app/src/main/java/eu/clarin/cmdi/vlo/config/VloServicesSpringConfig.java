/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.config;

import com.google.common.collect.Sets;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.service.UriResolver;
import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import eu.clarin.cmdi.vlo.service.handle.HandleClient;
import eu.clarin.cmdi.vlo.service.handle.impl.HandleRestApiClient;
import eu.clarin.cmdi.vlo.service.impl.DocumentParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.ExclusiveFieldFilter;
import eu.clarin.cmdi.vlo.service.impl.InclusiveFieldFilter;
import eu.clarin.cmdi.vlo.service.impl.QueryFacetsSelectionParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.ResourceStringConverterImpl;
import eu.clarin.cmdi.vlo.service.impl.ResourceTypeCountingServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.SearchContextParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.UriResolverImpl;
import eu.clarin.cmdi.vlo.service.impl.XmlTransformationServiceImpl;
import java.util.Properties;
import javax.inject.Inject;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.solr.common.SolrDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans for services used by the VLO web application (converters, resolvers,
 * filters)
 *
 * @author twagoo
 */
@Configuration
public class VloServicesSpringConfig {

    @Inject
    VloConfig vloConfig;

    @Bean
    public ResourceTypeCountingService resourceTypeCountingService() {
        return new ResourceTypeCountingServiceImpl(resourceStringConverter());
    }

    @Bean(name = "resourceStringConverter")
    public ResourceStringConverter resourceStringConverter() {
        return new ResourceStringConverterImpl();
    }

    @Bean(name = "resolvingResourceStringConverter")
    public ResourceStringConverter resolvingResourceStringConverter() {
        return new ResourceStringConverterImpl(uriResolver());
    }

    @Bean
    public UriResolver uriResolver() {
        return new UriResolverImpl(handleClient());
    }

    public HandleClient handleClient() {
        return new HandleRestApiClient();
    }

    @Bean(name = "queryParametersConverter")
    public PageParametersConverter<QueryFacetsSelection> queryParametersConverter() {
        return new QueryFacetsSelectionParametersConverter();
    }

    @Bean(name = "documentParamsConverter")
    public PageParametersConverter<SolrDocument> documentParamsConverter() {
        return new DocumentParametersConverter();
    }

    @Bean(name = "searchContextParamsConverter")
    public PageParametersConverter<SearchContext> searchContextParamsConverter() {
        return new SearchContextParametersConverter(queryParametersConverter());
    }

    @Bean
    public XmlTransformationService cmdiTransformationService() throws TransformerConfigurationException {
        final Source xsltSource = new StreamSource(getClass().getResourceAsStream("/cmdi2xhtml.xsl"));
        //TODO: Read properties from file??
        final Properties transformationProperties = new Properties();
        transformationProperties.setProperty(OutputKeys.METHOD, "html");
        transformationProperties.setProperty(OutputKeys.INDENT, "yes");
        transformationProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        return new XmlTransformationServiceImpl(xsltSource, transformationProperties);
    }

    @Bean(name = "basicPropertiesFilter")
    public FieldFilter basicPropertiesFieldFilter() {
        return new ExclusiveFieldFilter(Sets.union(
                vloConfig.getIgnoredFields(),
                vloConfig.getTechnicalFields()));
    }

    @Bean(name = "searchResultPropertiesFilter")
    public FieldFilter searchResultPropertiesFilter() {
        return new InclusiveFieldFilter(vloConfig.getSearchResultFields());
    }

    @Bean(name = "technicalPropertiesFilter")
    public FieldFilter technicalPropertiesFieldFilter() {
        return new InclusiveFieldFilter(
                vloConfig.getTechnicalFields());
    }
}
