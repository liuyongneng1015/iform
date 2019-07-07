package tech.ascs.icity.iform.support.internal;

import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.type.TypeResolver;

public class IFormInFlightMetadataCollectorImpl extends InFlightMetadataCollectorImpl {

	private Database database;

	public IFormInFlightMetadataCollectorImpl(MetadataBuildingOptions options, TypeResolver typeResolver) {
		super(options, typeResolver);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Database getDatabase() {
		// important to delay this instantiation until as late as possible.
		if ( database == null ) {
			this.database = new IFormDatabase( getMetadataBuildingOptions() );
		}
		return database;
	}

}
