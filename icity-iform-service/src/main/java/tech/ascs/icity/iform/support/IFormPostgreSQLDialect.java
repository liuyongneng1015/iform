package tech.ascs.icity.iform.support;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class IFormPostgreSQLDialect extends PostgreSQL95Dialect {

	public IFormPostgreSQLDialect() {
    	super();
    	registerColumnType(Types.BLOB, "bytea");
    	registerColumnType(Types.BOOLEAN, "bool");
    	registerColumnType(Types.TIMESTAMP_WITH_TIMEZONE, "timestamptz");
	}

	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		if (sqlTypeDescriptor.getSqlType() == Types.BLOB) {
			return BinaryTypeDescriptor.INSTANCE;
		}
		return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
	}

	@Override  
	public boolean useInputStreamToInsertBlob() {
		return true;
	}
}
