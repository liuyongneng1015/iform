package tech.ascs.icity.iform.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.service.DataModelService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IFormSessionFactoryBuilderTest {

	@Autowired
	IFormSessionFactoryBuilder sessionFactoryBuilder;

	@Autowired
	private DataModelService dataModelService;

	@Test
	@Transactional
	public void testGetSessionFactory() {
		DataModelEntity dataModel = dataModelService.get("2c9280836619855301661a756c87011a");
		try {
			if (dataModel != null) {
				System.out.println(sessionFactoryBuilder.generateHibernateMapping(dataModel));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@Transactional
	public void testGetHibernateMapping() {
		DataModelEntity dataModel = dataModelService.get("2c9280836619855301661a756c87011a");
		try {
			if (dataModel != null) {
				System.out.println(sessionFactoryBuilder.generateHibernateMapping(dataModel));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
