package com.example.plugins.tutorial.jira.tabpanels;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import com.atlassian.jira.issue.link.RemoteIssueLink;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import  com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.sal.api.net.Request;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.applinks.api.CredentialsRequiredException;

import java.util.Map;
import java.util.HashMap;

@ExportAsService
public class VersionService
{
	private JiraAuthenticationContext     authContext;
    private ApplicationUser               currentUser;
    private final ApplicationLinkService  applicationLinkService;
    private final ActiveObjects 		  ao;
	@Autowired
	public VersionService(ApplicationLinkService applicationLinkService, ActiveObjects ao) {
		this.authContext            = ComponentAccessor.getJiraAuthenticationContext();
        this.currentUser            = this.authContext.getLoggedInUser();
        this.applicationLinkService = applicationLinkService;
        this.ao                     = ao;
    }

    public ApplicationUser getUser(){
    	return currentUser;
    }

    public Boolean newVersion(RemoteIssueLink remoteIssueLink){
    	ApplicationLink appLink = applicationLinkService.getPrimaryApplicationLink(ConfluenceApplicationType.class);
        if (appLink == null)
        {
        	System.out.println("\n\n\nappLink == null\n\n\n");
            return false;
        }

        ApplicationLinkRequestFactory requestFactory = appLink.createAuthenticatedRequestFactory();

        try
        {           
            ApplicationLinkRequest request = requestFactory.createRequest(Request.MethodType.GET, "rest/api/content/" + getQueryMap(remoteIssueLink.getGlobalId()).get("pageId") + "?extend=version");
            String responseBody = request.execute(new ApplicationLinkResponseHandler<String>()
                {
                    public String credentialsRequired(final Response response) throws ResponseException
                    {
                        return response.getResponseBodyAsString();
                    }

                    public String handle(final Response response) throws ResponseException
                    {
                        return response.getResponseBodyAsString();
                    }
                });
    
            JSONObject json = new JSONObject(responseBody);
            Long version = json.getJSONObject("version").getLong("number");
            Todo[] todos = ao.find(Todo.class, "user_id = ? AND issue_id = ?", currentUser.getDirectoryId(), remoteIssueLink.getGlobalId());
            if (todos.length > 0 && todos[0].getVersion() == version){
                return false;
            }else{
                return true;
            }
        }      
        catch (CredentialsRequiredException e)
        {
        	System.out.println("\n\n\nCredentialsRequiredException\n\n\n");
            return false;
        }  
        catch (ResponseException e)
        {
        	System.out.println("\n\n\nResponseException\n\n\n");
            return false;
        }
        catch (JSONException e){
        	System.out.println("\n\n\nJSONException");
        

            return false;
        }
    }

    private Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}