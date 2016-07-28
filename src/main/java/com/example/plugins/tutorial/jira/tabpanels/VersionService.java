package com.example.plugins.tutorial.jira.tabpanels;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.sal.api.net.Request;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.applinks.api.CredentialsRequiredException;

import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


@ExportAsService
public class VersionService
{
    private JiraAuthenticationContext     authContext;
    private ApplicationUser               currentUser;
    private final ApplicationLinkService  applicationLinkService;
    private final ActiveObjects           ao;
    private final RemoteIssueLinkService  remoteIssueLinkService;

    @Autowired
    public VersionService(ApplicationLinkService applicationLinkService, ActiveObjects ao, RemoteIssueLinkService remoteIssueLinkService) {
        this.authContext            = ComponentAccessor.getJiraAuthenticationContext();
        this.currentUser            = this.authContext.getLoggedInUser();
        this.applicationLinkService = applicationLinkService;
        this.remoteIssueLinkService = remoteIssueLinkService;
        this.ao                     = ao;
    }


    public Map<String, Object> getIssuesIds(String[] globalId, String[] ids){
        Set<Long> idsAll = getRemoteIds(globalId);
        Set<Long> collection = convertStringToSet(ids);
        idsAll.retainAll(collection);
        return getIssuesIdsHelp(idsAll);

    }

    public Map<String, Object> getIssuesIds(String[] globalId){
        Set<Long> ids = getRemoteIds(globalId);
        return getIssuesIdsHelp(ids);
    }

    private Map<String, Object> getIssuesIdsHelp(Set<Long> ids){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<Long> radIds = getChangesIds(ids);

        map.put("radIds", radIds);
        ids.removeAll(radIds);
        map.put("normalIds", ids);

        return map;
    }

    private Set<Long> getRemoteIds(String[] globalId){
        List<String> glob = new ArrayList<String>(Arrays.asList(globalId));
        Set<Long> ids = new HashSet<Long>();
         for (RemoteIssueLink remoteIssue : remoteIssueLinkService.findRemoteIssueLinksByGlobalId(currentUser, glob).getRemoteIssueLinks()){
            ids.add(remoteIssue.getIssueId());
        }
        return ids;
    }

    public Set<Long> getChangesIds(Set<Long> ids){
        Map<String, List<Long>> remotes = getRemotes(getIssues(ids));

        return getChangesIdsHelp(remotes);
    }

    public Set<Long> getChangesIds(String[] ids){
        Map<String, List<Long>> remotes = getRemotes(getIssues(ids));

        return getChangesIdsHelp(remotes);
    }

    private Set<Long> getChangesIdsHelp(Map<String, List<Long>> remotes){
        Set<Long> list = new HashSet<Long>();

        for (Map.Entry<String, List<Long>> remote : remotes.entrySet()){
            if((Boolean) getInfo(remote.getKey()).get("newVersion")){
                list.addAll(remote.getValue());
            }
        }
        return list;
    }

    public List<Issue> getIssues(String[] ids){
        Set<Long> collection = convertStringToSet(ids);
        return ComponentAccessor.getIssueManager().getIssueObjects(collection);
    }

    private Set<Long> convertStringToSet(String[] ids){
        Set<Long> list = new HashSet<Long>();

        for(String id : ids){
            list.add(Long.valueOf(id));
        }
        return list;
    }

    public List<Issue> getIssues(Set<Long> ids){
        return ComponentAccessor.getIssueManager().getIssueObjects(ids);
    }


    public Map<String, List<Long>> getRemotes(List<Issue> issues){
        Map<String, List<Long>> map = new HashMap<String, List<Long>>();
        for (Issue issue : issues){
            for (RemoteIssueLink remoteIssue : remoteIssueLinkService.getRemoteIssueLinksForIssue(currentUser, issue).getRemoteIssueLinks()){
                if (map.containsKey(remoteIssue.getGlobalId())){
                    map.get(remoteIssue.getGlobalId()).add(issue.getId());
                }else{
                    List<Long> list = new ArrayList<Long>();
                    list.add(issue.getId());
                    map.put(remoteIssue.getGlobalId(), list);
                }
            }
        }
        return map;
    }

    public ApplicationUser getUser(){
        return currentUser;
    }

    public String getBaseUrl()
    {
        return ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
    }

    public HashMap<String, Object> getInfo(String globalId)
    {
        return helpInfo(globalId);
    }

    public HashMap<String, Object> getInfo(RemoteIssueLink remoteIssueLink){
        return helpInfo(remoteIssueLink.getGlobalId());
    }

    private HashMap<String, Object> helpInfo(String globalId)
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        ApplicationLink appLink = applicationLinkService.getPrimaryApplicationLink(ConfluenceApplicationType.class);
        if (appLink == null)
        {
            System.out.println("\n\n\nappLink == null\n\n\n");
            map.put("newVersion", false);
            return map;
        }

        if (getQueryMap(globalId).get("pageId") == null)
        {
            System.out.println("\n\n\nglobalId == null\n\n\n");
            map.put("newVersion", false);
            return map;
        }

        ApplicationLinkRequestFactory requestFactory = appLink.createAuthenticatedRequestFactory();

        try
        {
            ApplicationLinkRequest request = requestFactory.createRequest(Request.MethodType.GET, "rest/api/content/" + getQueryMap(globalId).get("pageId") + "?extend=version");
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
            Todo[] todos = ao.find(Todo.class, "user_id = ? AND issue_id = ?", currentUser.getDirectoryId(), globalId);
            if (todos.length > 0 && todos[0].getVersion() == version){
                System.out.println("\n\n\n False after if in VersionService");
                System.out.println("todos.length: " + todos.length);
                System.out.println("todos[0].getVersion(): " + todos[0].getVersion());
                System.out.println("version: " + version);
                map.put("newVersion", false);
            }else{
                map.put("newVersion", true);
            }
            map.put("title",json.getString("title"));
            map.put("version", version);
            map.put("baseUrl", getBaseUrl()+"/plugins/servlet/request");
        }
        catch (CredentialsRequiredException e)
        {
            System.out.println("\n\n\nCredentialsRequiredException\n\n\n");
            map.put("newVersion", false);
            return map;
        }
        catch (ResponseException e)
        {
            System.out.println("\n\n\nResponseException\n\n\n");
            map.put("newVersion", false);
            return map;
        }
        catch (JSONException e){
            System.out.println("\n\n\nJSONException");
            map.put("newVersion", false);
            return map;
        }
        return map;
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
