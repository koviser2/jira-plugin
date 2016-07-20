package com.example.plugins.tutorial.jira.tabpanels;

import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceSpaceEntityType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.atlassian.applinks.api.application.jira.JiraApplicationType;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;

import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;

import  com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONException;


@Scanned
public class ConfluenceSpaceTabPanel extends AbstractIssueTabPanel implements IssueTabPanel
{

    private static final Logger log = LoggerFactory.getLogger(ConfluenceSpaceTabPanel.class);

    @ComponentImport private final EntityLinkService entityLinkService;
    @ComponentImport private final ApplicationLinkService applicationLinkService;
    @ComponentImport private final RemoteIssueLinkService remoteIssueLinkService;
    @ComponentImport private final ActiveObjects ao;

    public ConfluenceSpaceTabPanel(EntityLinkService entityLinkService, ApplicationLinkService applicationLinkService, RemoteIssueLinkService remoteIssueLinkService, ActiveObjects ao)
    {
        this.entityLinkService = entityLinkService;
        this.applicationLinkService = applicationLinkService;
        this.remoteIssueLinkService = remoteIssueLinkService;
        this.ao = ao;
    }

    public List getActions(Issue issue, ApplicationUser remoteUser)
    {
        // EntityLink entityLink = entityLinkService.getPrimaryEntityLink(issue.getProjectObject(), ConfluenceSpaceEntityType.class);
        // if (entityLink == null)
        // {
        //     return Collections.singletonList(new GenericMessageAction("No Link to a Confluence for this JIRA Project configured"));
        // }

        ApplicationLink appLink = applicationLinkService.getPrimaryApplicationLink(ConfluenceApplicationType.class);
        if (appLink == null)
        {
            return Collections.singletonList(new GenericMessageAction("No Link to a Confluence for this JIRA application configured"));
        }
        // System.out.println("\n issueLinkService : " + remoteIssueLinkService.getIssueLinks(remoteUser, issue).getLinkCollection().getLinkTypes());

        List<RemoteIssueLink> remoteLinks = remoteIssueLinkService.getRemoteIssueLinksForIssue(remoteUser, issue).getRemoteIssueLinks();

        for (int i = 0; i < remoteLinks.size(); i++)
        {
            System.out.println("\n\n\n\n remoteLinks[" + i + "]:");
            System.out.println("getUrl: " + remoteLinks.get(i).getUrl());
            System.out.println("getApplicationName: " + remoteLinks.get(i).getApplicationName());
            System.out.println("getApplicationType: " + remoteLinks.get(i).getApplicationType());
            System.out.println("getGlobalId: " + remoteLinks.get(i).getGlobalId());
            System.out.println("getQueryMap: " + getQueryMap(remoteLinks.get(i).getGlobalId()));
            System.out.println("getIconTitle: " + remoteLinks.get(i).getIconTitle());
            System.out.println("getIconUrl: " + remoteLinks.get(i).getIconUrl());
            System.out.println("getId: " + remoteLinks.get(i).getId());
            System.out.println("getIssueId: " + remoteLinks.get(i).getIssueId());
            System.out.println("getRelationship: " + remoteLinks.get(i).getRelationship());
            System.out.println("getStatusCategoryColorName: " + remoteLinks.get(i).getStatusCategoryColorName());
            System.out.println("getStatusCategoryKey: " + remoteLinks.get(i).getStatusCategoryKey());
            System.out.println("getStatusDescription: " + remoteLinks.get(i).getStatusDescription());
            System.out.println("getStatusIconLink: " + remoteLinks.get(i).getStatusIconLink());
            System.out.println("getStatusIconTitle: " + remoteLinks.get(i).getStatusIconTitle());
            System.out.println("getStatusIconUrl: " + remoteLinks.get(i).getStatusIconUrl());
            System.out.println("getStatusName: " + remoteLinks.get(i).getStatusName());
            System.out.println("getSummary: " + remoteLinks.get(i).getSummary());
            System.out.println("getTitle: " + remoteLinks.get(i).getTitle());
            System.out.println("hasStatusCategory: " + remoteLinks.get(i).hasStatusCategory());
            System.out.println("isResolved: " + remoteLinks.get(i).isResolved());
        }

        // ApplicationLinkRequestFactory requestFactory = entityLink.getApplicationLink().createAuthenticatedRequestFactory();

        ApplicationLinkRequestFactory requestFactory = appLink.createAuthenticatedRequestFactory();
        final String query = issue.getKey();
        String confluenceContentType = "page";
        // final String spaceKey = entityLink.getKey();

        System.out.println("\nquery : " + query);
        System.out.println("\napplicationLinkService : " + applicationLinkService.getPrimaryApplicationLink(ConfluenceApplicationType.class));
        // System.out.println("\nspaceKey : " + spaceKey);

        System.out.println("\nAO : "+ ao);
        // ao.executeInTransaction(new TransactionCallback<Todo>()
        // {
        //     @Override
        //     public Todo doInTransaction()
        //     {
        //         // final Todo todo = ao.create(Todo.class);
        //         // todo.setUserId("setUserId 3");
        //         // todo.setIssueId("setIssueId 3");
        //         // todo.setVersion("setVersion 3");
        //         // todo.save(); // (4)
        //         // // System.out.println("\n----------------------------------------\n");
        //         // for (Todo todo_find : ao.find(Todo.class)) // (2)
        //         // {
        //         //     System.out.println("\nAO UserId: "+ todo_find.getUserId());
        //         //     System.out.println("\nAO IssueId: "+ todo_find.getIssueId());
        //         //     System.out.println("\nAO Version: "+ todo_find.getVersion());
        //         // }
        //         // System.out.println("\n----------------------------------------\n");
        //         return ao.find(Todo.class);
        //     }
        // });
        // Todo[] todos = ao.find(Todo.class);
        // Todo todo = ao.create(Todo.class);
        // todo.setUserId("getDirectoryId " + remoteUser.getDirectoryId());
        // todo.setIssueId("getKey " + remoteUser.getKey());
        // todo.setVersion("setVersion " + (todos.length + 1));
        // todo.save();
        // System.out.println("\nAO TEST: "+ todos.length);
        // System.out.println("\n----------------------------------------\n");
        System.out.println("\nBEFORE DELETE\n");
        for (Todo todo_find : ao.find(Todo.class)) // (2)
        {
            System.out.println("\nAO UserId: "+ todo_find.getUserId());
            System.out.println("AO IssueId: "+ todo_find.getIssueId());
            System.out.println("AO Version: "+ todo_find.getVersion());
        }
        // ao.delete(ao.find(Todo.class));

        try
        {
            ApplicationLinkRequest request;
            String responseBody, url_issue;
            int iz = 0;
            JSONObject json;
            IssueAction searchResult;
            List<IssueAction> issueActions = new ArrayList<IssueAction>();
            for(RemoteIssueLink link : remoteLinks){
                request = requestFactory.createRequest(Request.MethodType.GET, "rest/api/content/" + getQueryMap(link.getGlobalId()).get("pageId") + "?extend=version");
                responseBody = request.execute(new ApplicationLinkResponseHandler<String>()
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
                json = new JSONObject(responseBody);
                Long version = json.getJSONObject("version").getLong("number");
                System.out.println("\n<-- VERSION -->\n" + version);
                // if (iz % 2 == 0){
                //     Todo todo = ao.create(Todo.class);
                //     todo.setUserId(remoteUser.getDirectoryId());
                //     todo.setIssueId(link.getGlobalId());
                //     todo.setVersion(version);
                //     todo.save();
                // }
                // iz++;

                Todo[] todos = ao.find(Todo.class, "user_id = ? AND issue_id = ?", remoteUser.getDirectoryId(), link.getGlobalId());
                if (todos.length > 0 && todos[0].getVersion() == version){
                    url_issue = link.getUrl();
                    searchResult = new GenericMessageAction(String.format("You have already seen this article: <a target=\"_new\" href=%1$s>%1$s</a>", url_issue));
                }else{
                    url_issue = link.getUrl();
                    searchResult = new GenericMessageAction(String.format("Article have changed or added: <a target=\"_new\" href=%1$s>%1$s</a>", url_issue));
                }
                issueActions.add(searchResult);
            }
            return issueActions;
        }
        catch (CredentialsRequiredException e)
        {
            final HttpServletRequest req = ExecutingHttpRequest.get();
            URI authorisationURI = e.getAuthorisationURI(URI.create(JiraUrl.constructBaseUrl(req) + "/browse/" + issue.getKey()));
            String message = "You have to authorise this operation first. <a target=\"_new\" href=%s>Please click here and login into the remote application.</a>";
            IssueAction credentialsRequired = new GenericMessageAction(String.format(message, authorisationURI));
            return Collections.singletonList(credentialsRequired);
        }
        catch (ResponseException e)
        {
            return Collections.singletonList(new GenericMessageAction("Response exception. Message: " + e.getMessage()));
        }
        catch (JSONException e){
            return Collections.singletonList(new GenericMessageAction("Failed to read response from Confluence." + e.getMessage()));
        }
    }

    public static Map<String, String> getQueryMap(String query)
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

    public boolean showPanel(Issue issue, ApplicationUser remoteUser)
    {
        return true;
    }

    private Document parseResponse(String body) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
        return db.parse(is);
    }
}
