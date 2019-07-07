package tech.ascs.icity.iform.support.internal;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.CfgXmlAccessService;
import org.hibernate.boot.cfgxml.spi.MappingReference;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataImplementor;

public class IFormMetadataBuilderImpl extends MetadataBuilderImpl {

	private final MetadataSources sources;

	public IFormMetadataBuilderImpl(MetadataSources sources) {
		super(sources);
		this.sources = sources;
	}

	public IFormMetadataBuilderImpl(MetadataSources sources, StandardServiceRegistry serviceRegistry) {
		super(sources, serviceRegistry);
		this.sources = sources;
	}

	@Override
	public MetadataImplementor build() {
		final CfgXmlAccessService cfgXmlAccessService = getMetadataBuildingOptions().getServiceRegistry().getService( CfgXmlAccessService.class );
		if ( cfgXmlAccessService.getAggregatedConfig() != null ) {
			if ( cfgXmlAccessService.getAggregatedConfig().getMappingReferences() != null ) {
				for ( MappingReference mappingReference : cfgXmlAccessService.getAggregatedConfig().getMappingReferences() ) {
					mappingReference.apply( sources );
				}
			}
		}

		return IFormMetadataBuildingProcess.build( sources, getMetadataBuildingOptions() );
	}

}
