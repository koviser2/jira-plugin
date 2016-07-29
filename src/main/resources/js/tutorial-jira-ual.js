function getStr(array){
  str = "";
  for(var i = 0; i < array.length; i++){
    str += "div[data-issue-id=" + array[i] + "], ";
  }
  return str;
}

function getArray(){
  var array = [];
  $(".ghx-issue").map(function(){
    array.push($(this).data("issue-id"));
  });
  return array;
}

function restartClasses(){
  var elements = $(".ghx_remote_finali_link");
  elements.removeClass("ghx_remote_finali_link");
  elements.width();
  elements.addClass("ghx_remote_finali_link");

  var elements2 = $(".remote_finali_link");
  elements2.removeClass("remote_finali_link");
  elements2.width();
  elements2.addClass("remote_finali_link");
}

AJS.$(document).ready(function () {
  AJS.$(".aui-page-panel").bind("DOMNodeInserted", function(){
    var array = getArray();
    if (array.length > 0 && !$(this).hasClass("ghx_remote_finali_link_flag")){
      $(this).addClass("ghx_remote_finali_link_flag");
      // AJS.$(".aui-page-panel").unbind("DOMNodeInserted DOMNodeRemoved");
      $.ajax({
        url:       "../plugins/servlet/request",
        type:      "GET",
        dataType:  "json",
        data:      {ids: array},
        success:  function(response)
                  {
                    if(response.success){
                      str = getStr(response.radIds);
                      $(str).addClass("ghx_remote_finali_link");
                    }
                  },
      });
    } else if(array.length == 0 && $(this).hasClass("ghx_remote_finali_link_flag")){
      $(this).removeClass("ghx_remote_finali_link_flag");
    }
  });

  AJS.$(".issue-container, .aui-page-panel").on("change", ".links-list .render_issue_link_for_confluence input[type='checkbox']", function(){

    var self = $(this);
    var hash = {
          checked: self.prop("checked"),
          id:      self.data("id"),
          ids:     getArray()
        };

    $.ajax({
      url:      self.data("url"),
      type:     "POST",
      dataType: "json",
      data:     hash,
      success:  function(response)
                {
                  if(response.success){
                      if (self.prop("checked")){
                          self.closest(".remote_find_class").removeClass("remote_finali_link");
                      } else {
                          self.closest(".remote_find_class").addClass("remote_finali_link");
                      }
                      if(response.responseIds){
                        $(getStr(response.responseIds.radIds)).addClass("ghx_remote_finali_link");
                        $(getStr(response.responseIds.normalIds)).removeClass("ghx_remote_finali_link");
                      }
                      restartClasses();
                  }
                }

    });
  });
});
