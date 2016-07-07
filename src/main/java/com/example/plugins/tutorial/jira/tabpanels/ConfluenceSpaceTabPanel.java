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

@Scanned
public class ConfluenceSpaceTabPanel extends AbstractIssueTabPanel implements IssueTabPanel
{

    private static final Logger log = LoggerFactory.getLogger(ConfluenceSpaceTabPanel.class);

    @ComponentImport private final EntityLinkService entityLinkService;
    @ComponentImport private final ApplicationLinkService applicationLinkService;
    @ComponentImport private final RemoteIssueLinkService remoteIssueLinkService;

    public ConfluenceSpaceTabPanel(EntityLinkService entityLinkService, ApplicationLinkService applicationLinkService, RemoteIssueLinkService remoteIssueLinkService)
    {
        this.entityLinkService = entityLinkService;
        this.applicationLinkService = applicationLinkService;
        this.remoteIssueLinkService = remoteIssueLinkService;
    }

    public List getActions(Issue issue, ApplicationUser remoteUser)
    {
        EntityLink entityLink = entityLinkService.getPrimaryEntityLink(issue.getProjectObject(), ConfluenceSpaceEntityType.class);
        if (entityLink == null)
        {
            return Collections.singletonList(new GenericMessageAction("No Link to a Confluence for this JIRA Project configured"));
        }

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
        final String spaceKey = entityLink.getKey();

        System.out.println("\nquery : " + query);
        System.out.println("\napplicationLinkService : " + applicationLinkService.getPrimaryApplicationLink(ConfluenceApplicationType.class));
        System.out.println("\nspaceKey : " + spaceKey);


        try
        {
            ApplicationLinkRequest request = requestFactory.createRequest(Request.MethodType.GET, "/rest/prototype/1/search?query=" + query + "&spaceKey=" + spaceKey + "&type=" + confluenceContentType);
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

            Document document = parseResponse(responseBody);
            NodeList results = document.getDocumentElement().getChildNodes();

            List<IssueAction> issueActions = new ArrayList<IssueAction>();

            for (int j = 0; j < results.getLength(); j++)
            {
                NodeList links = results.item(j).getChildNodes();
                for (int i = 0; i < links.getLength(); i++)
                {
                    Node linkNode = links.item(i);
                    if ("link".equals(linkNode.getNodeName()))
                    {
                        NamedNodeMap attributes = linkNode.getAttributes();
                        Node type = attributes.getNamedItem("type");
                        if (type != null && "text/html".equals(type.getNodeValue()))
                        {
                            Node href = attributes.getNamedItem("href");
                            URI uriToConfluencePage = URI.create(href.getNodeValue());

                            IssueAction searchResult = new GenericMessageAction(String.format("Reference to Issue found in Confluence page <a target=\"_new\" href=%1$s>%1$s</a>", uriToConfluencePage.toString()));
                            issueActions.add(searchResult);
                        }
                    }
                }
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
        catch (ParserConfigurationException e)
        {
            return Collections.singletonList(new GenericMessageAction("Failed to read response from Confluence." + e.getMessage()));
        }
        catch (SAXException e)
        {
            return Collections.singletonList(new GenericMessageAction("Failed to read response from Confluence." + e.getMessage()));
        }
        catch (IOException e)
        {
            return Collections.singletonList(new GenericMessageAction("Failed to read response from Confluence." + e.getMessage()));
        }
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
