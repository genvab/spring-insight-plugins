package com.springsource.insight.plugin.webflow;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.webflow.config.FlowDefinitionResource;
import org.springframework.webflow.config.FlowDefinitionResourceFactory;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowBuilderContext;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

public class WebFlowExecutionTest extends AbstractXmlFlowExecutionTests {

	@Override
	protected FlowDefinitionResource getResource(FlowDefinitionResourceFactory resourceFactory) {
		return resourceFactory.createFileResource("target/test-classes/webflow-test.xml");
	}

	@Override
	protected void configureFlowBuilderContext(MockFlowBuilderContext builderContext) {
		builderContext.registerBean("personBean", new DummyPersonBean());
		builderContext.registerBean("personDao", new DummyPersonDaoBean());
	}

	@Test
	public void testState() {
		MockExternalContext ctx = new MockExternalContext();
		setCurrentState("dummy1");
		getFlowScope().put("person", new DummyPersonBean());
		ctx.setEventId("*");
		resumeFlow(ctx);

		assertFlowExecutionActive();
		assertCurrentStateEquals("dummy2");
	}

	@Test
	public void testAction() {
		MockExternalContext ctx = new MockExternalContext();
		MutableAttributeMap input = new LocalAttributeMap();
		input.put("id", "1");
		startFlow(input, ctx);

		assertFlowExecutionActive();
		assertCurrentStateEquals("personForm");
		Object person=getFlowAttribute("person");
		assertTrue(person instanceof DummyPersonBean);
		assertTrue(((DummyPersonBean)person).name.equals("person1"));
	}

	@Test
	public void testFullFlow() {
		MockExternalContext ctx = new MockExternalContext();
		MutableAttributeMap input = new LocalAttributeMap();
		input.put("id", "1");
		startFlow(input, ctx);

		ctx.setEventId("cancel");
		resumeFlow(ctx);

		assertFlowExecutionEnded();
	}

	@Test
	public void testTransition() {
		MockExternalContext ctx = new MockExternalContext();
		setCurrentState("personForm");
		getFlowScope().put("person", new DummyPersonBean());
		ctx.setEventId("cancel");
		resumeFlow(ctx);

		assertFlowExecutionEnded();
	}

	public class DummyPersonBean {
		public String name;

		public DummyPersonBean() {
			name="new";
		}

		public DummyPersonBean(String name) {
			this.name=name;
		}
	}

	public class DummyPersonDaoBean {
		public DummyPersonBean findPersonById(Integer id) {
			return new DummyPersonBean("person"+id);
		}

		public DummyPersonBean save(DummyPersonBean person) {
			return person;
		}

		public Collection<DummyPersonBean> findPersons() {
			return new ArrayList<DummyPersonBean>();
		}
	}
}
