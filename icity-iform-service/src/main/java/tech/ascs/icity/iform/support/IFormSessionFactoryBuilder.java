package tech.ascs.icity.iform.support;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.commons.io.IOUtils;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tool.schema.Action;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.ColumnReferenceEntity;
import tech.ascs.icity.iform.model.DataModelEntity;

@Service
public class IFormSessionFactoryBuilder {

	@PersistenceUnit
	private EntityManagerFactory entityMangerFactory;

	private StandardServiceRegistry serviceRegistry;

	private Map<DataModelEntity, SessionFactory> sessionFactories = new HashMap<DataModelEntity, SessionFactory>();

	public SessionFactory getSessionFactory(DataModelEntity dataModel) throws MappingException {
		return getSessionFactory(dataModel, false);
	}

	public SessionFactory getSessionFactory(DataModelEntity dataModel, boolean forceNewInstance) throws MappingException {
		if (forceNewInstance || sessionFactories.get(dataModel) == null) {
			SessionFactory sessionFactory = createNewSessionFactory(dataModel);
			sessionFactories.put(dataModel, sessionFactory);
		}
		
		return sessionFactories.get(dataModel);
	}

	protected SessionFactory createNewSessionFactory(DataModelEntity dataModel) throws MappingException {
		Metadata metadata;
		try {
			metadata = new MetadataSources(standardRegistry())
					.addInputStream(IOUtils.toInputStream(generateHibernateMapping(dataModel), "UTF-8"))
					.getMetadataBuilder().build();
		} catch (IOException e) {
			throw new MappingException(e);
		}
		SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
		return sessionFactoryBuilder.build();
	}

	protected StandardServiceRegistry standardRegistry() {
		if (serviceRegistry == null) {
			SessionFactory sessionFactory = entityMangerFactory.unwrap(SessionFactory.class);
			serviceRegistry = new StandardServiceRegistryBuilder()
					.applySetting(AvailableSettings.DATASOURCE, SessionFactoryUtils.getDataSource(sessionFactory))
					.applySetting(AvailableSettings.DIALECT, ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService( JdbcServices.class ).getDialect().getClass())
					.applySetting(AvailableSettings.DEFAULT_ENTITY_MODE, EntityMode.MAP.getExternalName())
					.applySetting(AvailableSettings.HBM2DDL_AUTO, Action.UPDATE)
					.build();
		}
		return serviceRegistry;
	}

	public String generateHibernateMapping(DataModelEntity dataModel) throws IOException {
		setReferenceDataModel(dataModel);
		return generateHibernateMappingFreeMarker(dataModel);
	}

	private void setReferenceDataModel(DataModelEntity dataModel){
		//行
		List<DataModelEntity> referencesDataModel = dataModel.getReferencesDataModel();
		List<ColumnModelEntity> columnModelEntities = dataModel.getColumns();
		for(ColumnModelEntity columnModelEntity : columnModelEntities) {
			for (ColumnReferenceEntity entity : columnModelEntity.getColumnReferences()){
				DataModelEntity dataModelEntity = entity.getToColumn().getDataModel();
				if(referencesDataModel.contains(dataModelEntity)) {
					continue;
				}
				referencesDataModel.add(dataModelEntity);
				setReferenceDataModel(dataModelEntity);
			}
		}
	}

	protected String generateHibernateMappingThymeleaf(DataModelEntity dataModel) {
        TemplateEngine templateEngine = new SpringTemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.XML);
        templateEngine.setTemplateResolver(templateResolver);
        Context context = new Context();
        context.setVariable("dataModel", dataModel);
        StringWriter stringWriter = new StringWriter();
        templateEngine.process("mapping.xml", context, stringWriter);
        return stringWriter.toString();
	}



	private Configuration cfg;

	protected String generateHibernateMappingFreeMarker(DataModelEntity dataModel) throws IOException {
		Configuration cfg = getConfiguration();
		Template template = cfg.getTemplate("mapping.ftl");
		Map<String, Object> root = new HashMap<String, Object>();
        root.put("dataModel", dataModel);
        StringWriter stringWriter = new StringWriter();
        try {
			template.process(root, stringWriter);
		} catch (TemplateException e) {
			throw new IOException(e);
		}
        return stringWriter.toString();
	}

	private Configuration getConfiguration() {
		if (cfg == null) {
			cfg = new Configuration(Configuration.VERSION_2_3_28);
			cfg.setClassForTemplateLoading(IFormSessionFactoryBuilder.class, "/");
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		}
		return cfg;
	}
}
