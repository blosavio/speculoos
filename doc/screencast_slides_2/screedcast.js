$(document).ready(function() {

    duration = 777;

    function toggle_opacity(e) {
	if (e.css("opacity") == 0){
	    e.animate({"opacity": 1.0}, duration);
	} else {
	    e.animate({"opacity": 0}, duration);
	}
    }

    $("code.form").click(function(event) {
	sibling = $(this).siblings("code.eval")
	toggle_opacity(sibling);});

    $("div.panel-footer").click(function(event) {
	$("html, body").animate({scrollTop: $(this).parent().next().offset().top}, 1000)});

    $("div.panel-header").click(function(event) {
	$("html, body").animate({scrollTop: $(this).parent().prev().offset().top}, 1000)});

    $("#page-footer").click(function(event) {
	toggle_opacity($("code.eval"));
	$(".note").toggle(duration);
    });

    $(".highlightable").mouseenter(function(event) {$(this).addClass("highlight")})
    $(".highlightable").mouseleave(function(event) {$(this).removeClass("highlight")})

});
