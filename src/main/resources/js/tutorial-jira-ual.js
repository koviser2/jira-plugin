AJS.$(document).ready(function () {
    AJS.$(".links-list").on("change", ".render_issue_link_for_confluence input[type='checkbox']", function(){

    	var self = $(this);
    	var hash = {
    		checked: self.prop("checked"),
    		id: self.data("id")
    	};

    	$.ajax({
		    url: self.data("url"),
		    type: "GET",
		    dataType: "json",
		    contentType: "application/json",
		    data: hash,
		    success : function(response)
		    {
		    	console.log(response);
		        if (self.prop("checked")){
		        	self.closest(".remote_find_class").removeClass("remote_finali_link");
		        } else {
		        	self.closest(".remote_find_class").addClass("remote_finali_link");
		        }
		        console.log(response.success);
		    },
		    error : function(jqXHR, textStatus, errorThrown)
		    {
		        console.log(textStatus + " : " + errorThrown);
		    }
		   });
    });
});