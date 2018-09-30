package tech.ascs.icity.iform.support;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.commons.io.IOUtils;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Service;

@Service
public class IFormSessionFactoryBuilder {

	@PersistenceUnit
	private EntityManagerFactory entityMangerFactory;

	private StandardServiceRegistry serviceRegistry;

	public SessionFactory getSessionFactory() throws IOException {
		Metadata metadata = new MetadataSources(standardRegistry())
				.addInputStream(IOUtils.toInputStream("", "UTF-8"))
				.getMetadataBuilder().build();
		SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
		return sessionFactoryBuilder.build();
	}

	protected StandardServiceRegistry standardRegistry() {
		if (serviceRegistry == null) {
			SessionFactory sessionFactory = entityMangerFactory.unwrap(SessionFactory.class);
			serviceRegistry = new StandardServiceRegistryBuilder()
					.applySetting(AvailableSettings.DATASOURCE, SessionFactoryUtils.getDataSource(sessionFactory))
					.applySetting(AvailableSettings.DIALECT, ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService( JdbcServices.class ).getDialect().getClass())
					.applySetting(AvailableSettings.DEFAULT_ENTITY_MODE, EntityMode.MAP)
					.build();
		}
		
		return serviceRegistry;
	}
}
