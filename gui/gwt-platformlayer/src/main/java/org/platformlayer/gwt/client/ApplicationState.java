package org.platformlayer.gwt.client;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.api.login.Authentication;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.project.ProjectPlace;
import org.platformlayer.gwt.client.stores.JobStore;
import org.platformlayer.gwt.client.widgets.Alert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.view.client.ListDataProvider;

@Singleton
public class ApplicationState {
	private static ApplicationState INSTANCE;

	@Inject
	PlaceController placeController;

	@Inject
	JobStore jobStore;

	@Inject
	PlaceHistoryMapper placeHistoryMapper;

	public ApplicationState() {
		assert INSTANCE == null;
		INSTANCE = this;
	}

	private Authentication authentication;

	private final ListDataProvider<OpsProject> projectsProvider = new ListDataProvider<OpsProject>();

	private Alert flash;

	private OpsProject userProject;

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String username, Authentication authentication, List<String> projects) {
		this.authentication = authentication;

		String platformlayerUrl = getPlatformLayerBaseUrl();

		userProject = null;
		List<OpsProject> projectList = getProjects();
		projectList.clear();
		for (String project : projects) {
			OpsProject opsProject = new OpsProject(platformlayerUrl, project, authentication);
			projectList.add(opsProject);

			// Every user should have a per-user project with this magic name
			if (project.equals("user@@" + username.toLowerCase())) {
				userProject = opsProject;
			}
		}
	}

	public OpsProject findProject(String projectKey) {
		for (OpsProject project : getProjects()) {
			if (project.getProjectName().equals(projectKey)) {
				return project;
			}
		}

		return null;
	}

	private String getPlatformLayerBaseUrl() {
		// TODO: Get from GWT?? Get from auth??
		String url;

		if (GWT.isProdMode()) {
			// TODO: Derive from current path?
			url = "https://ops.platformlayer.net:8082/api/v0/";
		} else {
			url = "https://dev.platformlayer.net:8082/api/v0/";
		}
		assert url.endsWith("/");
		return url;
	}

	public String getAuthBaseUrl() {
		if (GWT.isProdMode()) {
			return "https://auth.platformlayer.net:5001/v2.0/";
		} else {
			return "https://dev.platformlayer.net:5001/v2.0/";
		}
	}

	public ListDataProvider<OpsProject> getProjectsProvider() {
		// roomsProvider = new ListDataProvider<Room>(rooms);
		return projectsProvider;
	}

	public List<OpsProject> getProjects() {
		return projectsProvider.getList();
	}

	public JobStore getJobStore() {
		return jobStore;
	}

	public OpsProject findProject(ApplicationPlace place) {
		ProjectPlace project = place.getProject();
		if (project == null) {
			return null;
		}

		return findProject(project.getProjectKey());
	}

	public String getToken(ApplicationPlace place) {
		return placeHistoryMapper.getToken(place);
	}

	public static ApplicationState get() {
		return INSTANCE;
	}

	public void flash(Alert flash) {
		this.flash = flash;
	}

	public Alert getFlash(boolean clear) {
		Alert ret = this.flash;
		if (clear) {
			this.flash = null;
		}
		return ret;
	}

	public OpsProject getUserProject() {
		return userProject;
	}

}
