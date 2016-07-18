package com.example.plugins.tutorial.jira.tabpanels;

import com.atlassian.jira.plugin.link.remotejira.RemoteJiraIssueLinkRenderer;
import com.atlassian.jira.plugin.issuelink.IssueLinkRenderer;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.viewissue.issuelink.DefaultIssueLinkRenderer;
import java.util.Map;
import java.util.HashMap;
import com.atlassian.jira.plugin.link.remotejira.JiraRemoteIssueLinkDecoratingService;
import com.atlassian.jira.plugin.link.remotejira.RemoteJiraGlobalIdFactory;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.activeobjects.external.ActiveObjects;
// import com.google.common.collect.RegularImmutableMap;
@Scanned
public class CustomIssueLinkRenderer extends DefaultIssueLinkRenderer implements IssueLinkRenderer
{
	// @ComponentImport private final JiraRemoteIssueLinkDecoratingService jiraRemoteIssueLinkDecoratingService;
    // private final VersionService versionService;
    public VersionService versionService;

	public CustomIssueLinkRenderer(@ComponentImport ApplicationLinkService applicationLinkService, ActiveObjects ao){
        System.out.println("\n\n\nCustomIssueLinkRenderer");
        this.versionService = new VersionService(applicationLinkService, ao);
	}

	@Override
    public boolean requiresAsyncLoading(RemoteIssueLink remoteIssueLink)
    {
    	System.out.println("\nrequiresAsyncLoading\n");
        // all weather links need to fetch the current weather information
        // so we always require async rendering
        return true;
    }

    @Override
    public Map<String, Object> getFinalContext(RemoteIssueLink remoteIssueLink, Map<String, Object> context) {
        // fetch the weather
            // RemoteIssueLink updatedLinkObject = new RemoteIssueLinkBuilder()
            //     .issueId(remoteIssueLink.getIssueId())
            //     .applicationName(remoteIssueLink.getApplicationName())
            //     .applicationType(remoteIssueLink.getApplicationType())
            //     .globalId(remoteIssueLink.getGlobalId())
            //     .title("New title of link")
            //     .summary("versionService.getInform()")
            //     .url("Some Url")
            //     .build();    
        HashMap<String, Object> result = new HashMap<String, Object>(getInitialContext(remoteIssueLink, context));
        result.put("newVersion", versionService.newVersion(remoteIssueLink));
        return result;
    }
}