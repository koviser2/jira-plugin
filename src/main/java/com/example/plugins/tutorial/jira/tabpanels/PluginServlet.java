package com.example.plugins.tutorial.jira.tabpanels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.applinks.api.ApplicationLinkService;
import java.util.HashMap;

import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

@Scanned
public class PluginServlet extends HttpServlet
{

  private static final Logger       log = LoggerFactory.getLogger(PluginServlet.class);
  private final ActiveObjects       ao;
  public VersionService             versionService;

  public PluginServlet(@ComponentImport ApplicationLinkService applicationLinkService, ActiveObjects ao, RemoteIssueLinkService remoteIssueLinkService)
  {
    this.ao             = ao;
    this.versionService = new VersionService(applicationLinkService, ao, remoteIssueLinkService);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    try
    {
      JSONObject json = new JSONObject();

      if (req.getRemoteUser() == null)
      {
        json.put("success", false).put("message", "Authorization has failed");
        resp.getWriter().write(json.toString());
        return;
      }

      if (req.getParameterValues("ids") == null)
      {
        json.put("success", false).put("message", "Bad attributes");
        resp.getWriter().write(json.toString());
        return;
      }

      json.put("success", true);
      json.put("ids", req.getParameterValues("ids"));
      json.put("list", versionService.getIssues(req.getParameterValues("ids")));
      json.put("remotes", versionService.getRemotes(versionService.getIssues(req.getParameterValues("ids"))));
      json.put("radIds", versionService.getChangesIds(req.getParameterValues("ids")));

      if(req.getParameterValues("globalId") != null){
        json.put("responseIds", versionService.getIssuesIds(req.getParameterValues("globalId")));
      }

      resp.getWriter().write(json.toString());
    }
    catch (JSONException e){
      System.out.println("\n\n\nJSONException");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException

  {
    try
    {
      JSONObject json = new JSONObject();
      resp.setContentType("text/x-json;charset=UTF-8");
      resp.setHeader("Cache-Control", "no-cache");

      if (req.getRemoteUser() == null)
      {
        json.put("success", false).put("message", "Authorization has failed");
        resp.getWriter().write(json.toString());
        return;
      }

      if (req.getParameterValues("id") == null || req.getParameterValues("checked") == null)
      {
        json.put("success", false).put("message", "Bad attributes");
        resp.getWriter().write(json.toString());
        return;
      }

      String id = req.getParameterValues("id")[0];
      HashMap<String, Object> map = versionService.getInfo(id);

      if (map.get("version") == null)
      {
        json.put("success", false).put("message", "Bad pageId");
        resp.getWriter().write(json.toString());
        return;
      }

      Long version = (Long)map.get("version");
      boolean checked = Boolean.parseBoolean(req.getParameterValues("checked")[0]);
      Todo[] todos = ao.find(Todo.class, "user_id = ? AND issue_id = ?", versionService.getUser().getDirectoryId(), id);

      if (todos.length == 0 && checked)
      {
        Todo todo = ao.create(Todo.class);
        todo.setUserId(versionService.getUser().getDirectoryId());
        todo.setIssueId(id);
        todo.setVersion(version);
        todo.save();
      } else if (todos.length > 0 && checked)
      {
        todos[0].setVersion(version);
        todos[0].save();
      } else if (todos.length > 0 && !checked)
      {
        ao.delete(todos);
      }

      json.put("success", true);
      if (req.getParameterValues("ids") != null){
        json.put("responseIds", versionService.getIssuesIds(req.getParameterValues("id"), req.getParameterValues("ids")));
      }
      resp.getWriter().write(json.toString());
    }
    catch (JSONException e){
      System.out.println("\n\n\nJSONException");
    }
  }

}
