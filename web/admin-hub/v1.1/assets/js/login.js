$(document).ready(function(){
    getSession();
});

function isBlank( data ) {
	return ( $.trim(data).length == 0 );
}

function getSession(){
    if (localStorage.getItem("role") != null) {
        window.location.replace("main");
    }
}

$(document).keypress(function (e) {
  if (e.which == 13) {
  	var username = $('#username').val();
  	var password = $('#password').val();
    sendData(username, password);
  }
});


function sendData(username, password){

	$("#add_err").css('display', 'none', 'important');
	$("#add_err").html("Credenciales incorrectas");

	if (isBlank(username) || isBlank(password)) {
		$("#add_err").html("Llene todos los campos");
	} else {
		var params = {
			"username" : username,
			"password" : password
		};
		$.ajax({
			data:  params,
			url:   'http://' + ip + '/admin-hub/v1.1/basic/login',
			type:  'post',
			beforeSend: function () {
				$.showLoading({
			        name: 'square-flip'
			    });
			},
			error: function (jqXHR, exception) {
				$.hideLoading();
		        alert('Error, intente de nuevo');
		    },
			success:  function (response) {
				$.hideLoading();
				var jsonResponse = JSON.parse(response);
				if (jsonResponse.success.trim() === 'true') {
					localStorage.setItem("role", jsonResponse.data.role);
					localStorage.setItem("username", jsonResponse.data.username);
					window.location.replace("main");
				} else {
					$("#add_err").css('display', 'inline', 'important');
					$("#add_err").html("Credenciales incorrectas");
				};
			}
		});
	}
}
