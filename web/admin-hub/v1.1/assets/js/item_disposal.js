$(document).ready(function(){
    $.ajax({
            url:   'http://' + ip + '/admin-hub/v1.1/items/all',
            type:  'get',
            beforeSend: function(){
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
                var items = JSON.parse(response);
                $.each(items, function(key,value) {
                  $('#items').append('<option value=' + value.id + '>' + value.name + '</option>');
                }); 
            }
        });
    getSession();
});

function checkValids(units, concept){
    return $.isNumeric(units) && concept != null && concept;
}

function sendData(){

    var units = $('#units').val();
    var concept = $('#concept').val();
    var itemId = $('#items').val();

    if (checkValids(units, concept)) {

        var disposal = {
            'units' : units,
            'concept' : concept,
            'item_id': itemId
        };

        var params = {
            'disposal' : JSON.stringify(disposal)
        };
        
        $.ajax({
            url:   'http://' + ip + '/admin-hub/v1.1/disposal/create',
            data: params,
            type:  'post',
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
                if (response == 'Creado') {
                    $('#units').val('');
                    $('#concept').val('');
                    alert('Salida creada con exito');
                } else {
                    alert("Error creando la salida");
                }
            }
        });

    } else {
        alert('Debes agregar todos los campos');
    }

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