$(document).ready(function(){
	$('#back-btn').click(function(){
		window.location.replace("/admin-hub/v1.1/main");
	});
	getSession();
});

function sendDataAndPrint(){
	$('#print-btn').hide();
	$('#back-btn').hide();
	window.print();
	$('#print-btn').show();
	$('#back-btn').hide();
	var params = {
		"order_id" : id,
		"code" : 2
	};
	$.ajax({
		data:  params,
		url:   'http://' + ip + '/admin-hub/v1.1/hub_update',
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
		}
	});
}

function getSession(){
    if (localStorage.getItem("role") == null) {
        window.location.replace("/admin-hub/v1.1/");
    }
    if (localStorage.getItem("role") != 'Admin' && localStorage.getItem("role") != 'Facturacion') {
        alert("Solo el administrador tiene acceso");
		window.location.replace("/admin-hub/v1.1/");
    };
}
