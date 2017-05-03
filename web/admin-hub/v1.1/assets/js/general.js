function toggle() {
    $("#wrapper").toggleClass("toggled");
}

function closeSession(){
	localStorage.clear();
	window.location.replace("/admin-hub/v1.1/");
}