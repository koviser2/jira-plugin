package com.developex.plugins.confluence.jira.tabpanels;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface Todo extends Entity
{
  Long getUserId();
  void setUserId(Long user_id);

  String getIssueId();
  void setIssueId(String issue_id);

  Long getVersion();
  void setVersion(Long version);
}
