package tech.ascs.icity.iform.support.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.jboss.logging.Logger;

import tech.ascs.icity.iform.support.IFormPostgreSQLDialect;

public class IFormTable extends Table {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger( IFormTable.class );

	public IFormTable() {
		super();
	}

	public IFormTable(Identifier catalog, Identifier schema, Identifier physicalTableName, boolean isAbstract) {
		super(catalog, schema, physicalTableName, isAbstract);
	}

	public IFormTable(Namespace namespace, Identifier physicalTableName, boolean isAbstract) {
		super(namespace, physicalTableName, isAbstract);
	}

	public IFormTable(Namespace namespace, Identifier physicalTableName, String subselect, boolean isAbstract) {
		super(namespace, physicalTableName, subselect, isAbstract);
	}

	public IFormTable(Namespace namespace, String subselect, boolean isAbstract) {
		super(namespace, subselect, isAbstract);
	}

	public IFormTable(String name) {
		super(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Iterator sqlAlterStrings(Dialect dialect, Metadata metadata, TableInformation tableInfo,
			String defaultCatalog, String defaultSchema) throws HibernateException {
		
		final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();

		final String tableName = jdbcEnvironment.getQualifiedObjectNameFormatter().format(
				tableInfo.getName(),
				dialect
		);

		StringBuilder root = new StringBuilder( dialect.getAlterTableString( tableName ) )
				.append( ' ' )
				.append( dialect.getAddColumnString() );

		Iterator iter = getColumnIterator();
		List results = new ArrayList();
		
		while ( iter.hasNext() ) {
			final Column column = (Column) iter.next();
			final ColumnInformation columnInfo = tableInfo.getColumn( Identifier.toIdentifier( column.getName(), column.isQuoted() ) );

			if ( columnInfo == null ) {
				// the column doesnt exist at all.
				StringBuilder alter = new StringBuilder( root.toString() )
						.append( ' ' )
						.append( column.getQuotedName( dialect ) )
						.append( ' ' )
						.append( column.getSqlType( dialect, metadata ) );

				String defaultValue = column.getDefaultValue();
				if ( defaultValue != null ) {
					alter.append( " default " ).append( defaultValue );
				}

				if ( column.isNullable() ) {
					alter.append( dialect.getNullColumnString() );
				}
				else {
					alter.append( " not null" );
				}

				if ( column.isUnique() ) {
					String keyName = Constraint.generateName( "UK_", this, column );
					UniqueKey uk = getOrCreateUniqueKey( keyName );
					uk.addColumn( column );
					alter.append( dialect.getUniqueDelegate()
							.getColumnDefinitionUniquenessFragment( column ) );
				}

				if ( column.hasCheckConstraint() && dialect.supportsColumnCheck() ) {
					alter.append( " check(" )
							.append( column.getCheckConstraint() )
							.append( ")" );
				}

				String columnComment = column.getComment();
				if ( columnComment != null ) {
					alter.append( dialect.getColumnComment( columnComment ) );
				}

				alter.append( dialect.getAddColumnSuffixString() );

				results.add( alter.toString() );
			} else if (shouldAlterColumn(dialect, metadata, column, columnInfo)) {
				if (columnTypeChanged(dialect, metadata, column, columnInfo)) {
					StringBuilder alter = new StringBuilder(dialect.getAlterTableString(tableName))
							.append(" alter column ").append(column.getQuotedName(dialect))
							.append(" set data type ").append(column.getSqlType(dialect,metadata));
					alter.append(getTypeCastingString(dialect, metadata, column, columnInfo));

					results.add(alter.toString());
				}

				if (nullableChanged(column, columnInfo)) {
					StringBuilder alter = new StringBuilder(dialect.getAlterTableString(tableName))
							.append(" alter column ").append(column.getQuotedName(dialect));
					if (column.isNullable()) {
						alter.append(" drop not null");
					} else {
						alter.append(" set not null");
					}

					results.add(alter.toString());
				}

//				if (defaultValueChanged(column, columnInfo)) {
					StringBuilder alter = new StringBuilder(dialect.getAlterTableString(tableName))
							.append(" alter column ").append(column.getQuotedName(dialect));
					String defaultValue = column.getDefaultValue();
					if (defaultValue != null && !defaultValue.equalsIgnoreCase("null")) {
						alter.append(" set default ").append(defaultValue);
					} else {
						alter.append(" drop default");
					}

					results.add(alter.toString());
//				}
			}

		}

		if ( results.isEmpty() ) {
			log.debugf( "No alter strings for table : %s", getQuotedName() );
		}
		results.forEach(n -> log.debug(n));

		return results.iterator();
	}

	protected boolean shouldAlterColumn(Dialect dialect, Metadata metadata, Column column, ColumnInformation columnInfo) {
		return columnTypeChanged(dialect, metadata, column, columnInfo)
				|| nullableChanged(column, columnInfo)
				|| defaultValueChanged(column, columnInfo);
	}

	protected boolean columnTypeChanged(Dialect dialect, Metadata metadata, Column column, ColumnInformation columnInfo) {
		if (column.getValue() instanceof SimpleValue && "string".equals(((SimpleValue)column.getValue()).getTypeName())) {
			return !column.getSqlType(dialect, metadata).startsWith(columnInfo.getTypeName()) || column.getLength() != columnInfo.getColumnSize();
		} else {
			return !column.getSqlType(dialect, metadata).equals(columnInfo.getTypeName());
		}
	}

	protected boolean nullableChanged(Column column, ColumnInformation columnInfo) {
		return column.isNullable() != columnInfo.getNullable().toBoolean(true);
	}

	protected boolean defaultValueChanged(Column column, ColumnInformation columnInfo) {
		// TODO columnInfo中不包含字段默认值信息，无法判断是否默认值有变化
		return false;
	}

	protected String getTypeCastingString(Dialect dialect, Metadata metadata, Column column, ColumnInformation columnInfo) {
		if (column.getValue() instanceof SimpleValue && dialect instanceof IFormPostgreSQLDialect) {
			String type = ((SimpleValue)column.getValue()).getTypeName();
			if (type != null) {
				String prefix = " USING " + column.getQuotedName(dialect) + "::";
				switch(type) {
					case "integer": return prefix + "integer";
					case "long": return prefix + "bigint";
					case "float": return prefix + "real";
					case "double": return prefix + "double precision";
					case "boolean": return prefix + "boolean";
					case "timestamp": return prefix + "timestamp without time zone";
					case "date": return prefix + "date";
					case "time": return prefix + "time without time zone";
				}
			}
		}
		return "";
	} 
}
