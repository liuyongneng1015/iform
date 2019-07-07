package tech.ascs.icity.iform.support.internal;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.mapping.DenormalizedTable;
import org.hibernate.mapping.Table;

public class IFormNamespace extends Namespace {
	private final Database database;
	private Map<Identifier, Table> tables = new TreeMap<Identifier, Table>();

	public IFormNamespace(Database database, Name name) {
		super(database, name);
		this.database = database;
	}

    @Override
	public Collection<Table> getTables() {
		return tables.values();
	}

    @Override
	public Table locateTable(Identifier logicalTableName) {
		return tables.get( logicalTableName );
	}

    @Override
	public Table createTable(Identifier logicalTableName, boolean isAbstract) {
		final Table existing = tables.get( logicalTableName );
		if ( existing != null ) {
			return existing;
		}

		final Identifier physicalTableName = database.getPhysicalNamingStrategy().toPhysicalTableName( logicalTableName, database.getJdbcEnvironment() );
		Table table = new IFormTable( this, physicalTableName, isAbstract );
		tables.put( logicalTableName, table );
		return table;
	}

    @Override
	public DenormalizedTable createDenormalizedTable(Identifier logicalTableName, boolean isAbstract, Table includedTable) {
		final Table existing = tables.get( logicalTableName );
		if ( existing != null ) {
			// for now assume it is
			return (DenormalizedTable) existing;
		}

		final Identifier physicalTableName = database.getPhysicalNamingStrategy().toPhysicalTableName( logicalTableName, database.getJdbcEnvironment() );
		DenormalizedTable table = new DenormalizedTable( this, physicalTableName, isAbstract, includedTable );
		tables.put( logicalTableName, table );
		return table;
	}
}
