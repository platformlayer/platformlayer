import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.platformlayer.core.model.ConfigureAction;
import org.platformlayer.service.schedule.model.ScheduledTask;
import org.platformlayer.xml.JaxbHelper;

public class WtfIsXml {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(WtfIsXml.class);

	@Test
	public void test() throws JAXBException {
		ScheduledTask task = new ScheduledTask();
		task.action = new ConfigureAction();
		// task.action.name = "actionname";

		System.out.println(JaxbHelper.toXml(task, true));
	}
}
