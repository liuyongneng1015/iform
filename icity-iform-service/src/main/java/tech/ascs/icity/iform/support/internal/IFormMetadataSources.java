package tech.ascs.icity.iform.support.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuilderFactory;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.service.ServiceRegistry;

public class IFormMetadataSources extends MetadataSources {

	private static final long serialVersionUID = 1L;

	public IFormMetadataSources() {
		super();
	}

	public IFormMetadataSources(ServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	/**
	 * Get a builder for metadata where non-default options can be specified.
	 *
	 * @return The built metadata.
	 */
	public MetadataBuilder getMetadataBuilder() {
		MetadataBuilderImpl defaultBuilder = new IFormMetadataBuilderImpl( this );
		return getCustomBuilderOrDefault( defaultBuilder );
	}

	/**
	 * Get a builder for metadata where non-default options can be specified.
	 *
	 * @return The built metadata.
	 */
	public MetadataBuilder getMetadataBuilder(StandardServiceRegistry serviceRegistry) {
		MetadataBuilderImpl defaultBuilder = new IFormMetadataBuilderImpl( this, serviceRegistry );
		return getCustomBuilderOrDefault( defaultBuilder );
	}

	/**
	 * In case a custom {@link MetadataBuilderFactory} creates a custom builder, return that one, otherwise the default
	 * builder.
	 */
	private MetadataBuilder getCustomBuilderOrDefault(MetadataBuilderImpl defaultBuilder) {
		final ClassLoaderService cls = getServiceRegistry().getService( ClassLoaderService.class );
		final java.util.Collection<MetadataBuilderFactory> discoveredBuilderFactories = cls.loadJavaServices( MetadataBuilderFactory.class );

		MetadataBuilder builder = null;
		List<String> activeFactoryNames = null;

		for ( MetadataBuilderFactory discoveredBuilderFactory : discoveredBuilderFactories ) {
			final MetadataBuilder returnedBuilder = discoveredBuilderFactory.getMetadataBuilder( this, defaultBuilder );
			if ( returnedBuilder != null ) {
				if ( activeFactoryNames == null ) {
					activeFactoryNames = new ArrayList<String>();
				}
				activeFactoryNames.add( discoveredBuilderFactory.getClass().getName() );
				builder = returnedBuilder;
			}
		}

		if ( activeFactoryNames != null && activeFactoryNames.size() > 1 ) {
			throw new HibernateException(
					"Multiple active MetadataBuilder definitions were discovered : " +
							StringHelper.join( ", ", activeFactoryNames )
			);
		}

		return builder != null ? builder : defaultBuilder;
	}
}
