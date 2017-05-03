var inst = $('[data-remodal-id=modal]').remodal();

$(document).ready(function(){
	$('#table').bootstrapTable({
		url: 'http://' + ip + '/admin-hub/v1/clients/all',
		search: true,
		pagination: true,
		columns: [{
			field: 'id',
			title: 'Codigo interno'
		}, {
			field: 'code',
			title: 'Codigo empresa'
		}, {
			field: 'company',
			title: 'Empresa'
		},{
			field: 'contact',
			title: 'Contacto'
		},{
			field: 'neighborhood',
			title: 'Barrio'
		},{
			field: 'address',
			title: 'Direccion'
		},{
			field: 'mail_address',
			title: 'Correo'
		},{
			field: 'nit',
			title: 'NIT'
		}
		],
		onClickRow: function (row, $element) {
			getData(row.id);
			inst.open();
		},
		onLoadSuccess: function(data){
			toggle();
		}, 
		onPreBody:function(data){
			getSession();
		}
	});
});

function getData(clientId){
	var params = {
		"client_id" : clientId
	};
	$.ajax({
		data:  params,
		url:   'http://' + ip + '/admin-hub/v1/clients/find/' + clientId,
		type:  'get',
		beforeSend: function() {
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
			var client = $.parseJSON(response);
			$('#company').val(client.company);
			$('#id').val(client.id);
			$('#code').val(client.code);
			$('#contact').val(client.contact);
			$('#neighborhood').val(client.neighborhood);
			$('#address').val(client.address);
			$('#mail').val(client.mail_address);
			$('#nit').val(client.nit);
			$('#phone-one').val(client.phone_one);
			$('#phone-two').val(client.phone_two);
			$('#phone-three').val(client.phone_three);
			Materialize.updateTextFields();
		}
	});
}

function empty(value){
	return (value == null || value === "" || !/\S/.test(value));
}

function updateClient(){

    var id = $('#id').val();
    var code = $('#code').val();
    var company = $('#company').val();
    var contact = $('#contact').val();
    var neighborhood = $('#neighborhood').val();
    var address = $('#address').val();
    var mail_address = $('#mail').val();
    var nit = $('#nit').val();
    var phone_one = $('#phone-one').val();
    var phone_two = $('#phone-two').val();
    var phone_three = $('#phone-three').val();

    if (empty(code) || empty(company) || empty(contact) || empty(neighborhood) || empty(address) || empty(mail_address) || empty(nit) || empty(phone_one)){
        alert('Debes llenar todos los campos');
    } else {
        var client = {
            'id' : id,
            'code' : code,
            'company' : company,
            'contact' : contact,
            'neighborhood' : neighborhood,
            'address' : address,
            'mail_address' : mail_address,
            'nit' : nit,
            'phone_one' : phone_one,
            'phone_two' : phone_two,
            'phone_three' : phone_three
        };

        var clientParam = JSON.stringify(client);
        var params = {
            "client" : clientParam
        };
        $.ajax({
            data:  params,
            url:   'http://' + ip + '/admin-hub/v1/clients/update',
            type:  'post',
            beforeSend: function () {
            },
            success:  function (response) {
                if(response.trim() == 'Actualizado'){
                    alert("Actualizado con exito");
                    location.reload();
                } else {
                    alert("Error en la actualizacion");
                }
            }
        });
    }
}

$(document).on('confirmation', '.remodal', function () {
	updateClient();
});

function getSession(){
	if (localStorage.getItem("role") == null) {
		window.location.replace("/admin-hub/v1");
	}
	if (localStorage.getItem("role") != 'Admin') {
        alert("Solo el administrador tiene acceso");
        window.location.replace("main");
    };
}