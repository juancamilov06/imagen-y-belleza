
function listClick(){
	window.location.replace("items");
}

function addClick(){
	window.location.replace("entering");
}

function disposalClick(){
	window.location.replace("disposal");
}

function createClick(){
    window.location.replace("items/create");
}

function getSession(){
    if (localStorage.getItem("role") == null) {
        window.location.replace("/admin-hub/v1.1/");
    }
    if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    };
}