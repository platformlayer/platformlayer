package org.platformlayer.gwt.client.job;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.JobLog;
import org.platformlayer.gwt.client.api.platformlayer.JobLogLine;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.stores.JobStore;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

public class JobViewImpl extends AbstractApplicationPage implements JobView {
	static final Logger log = Logger.getLogger(JobViewImpl.class.getName());

	interface ViewUiBinder extends UiBinder<HTMLPanel, JobViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public JobViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	private JobActivity activity;

	@Inject
	JobStore jobStore;

	@UiField
	SpanElement labelSpan;

	@Override
	public void start(JobActivity activity) {
		this.activity = activity;

		Job job = activity.getJobState();

		SafeHtml html = SafeHtmlUtils.fromString(job.getTargetId() + " " + job.getAction().getName());
		labelSpan.setInnerSafeHtml(html);

		OpsProject project = activity.getProject();
		String jobId = job.getJobId();
		jobStore.getJobLog(project, jobId, new AsyncCallback<JobLog>() {

			@Override
			public void onSuccess(JobLog result) {
				List<JobLogLine> lines = result.getLines();
				addDataDisplay(jobLogList, lines);
			}

			@Override
			public void onFailure(Throwable caught) {
				log.log(Level.WARNING, "Error reading job log", caught);
			}
		});
	}

	@UiField
	CellList<JobLogLine> jobLogList;

	@UiFactory
	CellList<JobLogLine> makeJobLogList() {
		CellList<JobLogLine> table = new CellList<JobLogLine>(new JobLogListCell(this));

		// final SingleSelectionModel<OpsProject> selectionModel = new SingleSelectionModel<Product>();
		// table.setSelectionModel(selectionModel);
		// selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
		// @Override
		// public void onSelectionChange(SelectionChangeEvent event) {
		// OpsProject selected = selectionModel.getSelectedObject();
		//
		// if (selectionModel.isSelected(selected)) {
		// Room room = activity.getPlace().getRoom();
		//
		// selectionModel.setSelected(selected, false);
		// activity.addItem(room, selected);
		// }
		// }
		// });

		return table;
	}

}