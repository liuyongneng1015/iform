package tech.ascs.icity.iform.support.internal;

import java.util.Map;
import java.util.TreeMap;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class IFormDatabase extends Database {

	private Namespace implicitNamespace;

	private final Map<Namespace.Name,Namespace> namespaceMap = new TreeMap<Namespace.Name, Namespace>();

	public IFormDatabase(MetadataBuildingOptions buildingOptions) {
		this( buildingOptions, buildingOptions.getServiceRegistry().getService( JdbcEnvironment.class ) );
	}

	public IFormDatabase(MetadataBuildingOptions buildingOptions, JdbcEnvironment jdbcEnvironment) {
		super(buildingOptions, jdbcEnvironment);

		this.implicitNamespace = makeNamespace(
				new Namespace.Name(
						toIdentifier( buildingOptions.getMappingDefaults().getImplicitCatalogName() ),
						toIdentifier( buildingOptions.getMappingDefaults().getImplicitSchemaName() )
				)
		);
	}

	private Namespace makeNamespace(Namespace.Name name) {
		Namespace namespace;
		namespace = new IFormNamespace( this, name );
		namespaceMap.put( name, namespace );
		return namespace;
	}

    @Override
	public Iterable<Namespace> getNamespaces() {
		return namespaceMap.values();
	}

    @Override
	public Namespace getDefaultNamespace() {
		return implicitNamespace;
	}

    @Override
	public Namespace locateNamespace(Identifier catalogName, Identifier schemaName) {
		if ( catalogName == null && schemaName == null ) {
			return getDefaultNamespace();
		}

		final Namespace.Name name = new Namespace.Name( catalogName, schemaName );
		Namespace namespace = namespaceMap.get( name );
		if ( namespace == null ) {
			namespace = makeNamespace( name );
		}
		return namespace;
	}

    @Override
	public Namespace adjustDefaultNamespace(Identifier catalogName, Identifier schemaName) {
		final Namespace.Name name = new Namespace.Name( catalogName, schemaName );
		if ( implicitNamespace.getName().equals( name ) ) {
			return implicitNamespace;
		}

		Namespace namespace = namespaceMap.get( name );
		if ( namespace == null ) {
			namespace = makeNamespace( name );
		}
		implicitNamespace = namespace;
		return implicitNamespace;
	}
}
